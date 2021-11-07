package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.grpc.ReadingService;
import hr.fer.tel.rassus.client.model.Reading;
import hr.fer.tel.rassus.client.model.Sensor;
import hr.fer.tel.rassus.client.retrofit.ReadingApi;
import hr.fer.tel.rassus.client.retrofit.SensorApi;
import hr.fer.tel.rassus.examples.ReadingGrpc;
import hr.fer.tel.rassus.examples.ReadingRequest;
import hr.fer.tel.rassus.examples.ReadingResponse;
import io.grpc.*;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Client {

    private static final double MIN_LON = 15.87;

    private static final double MAX_LON = 16.00;

    private static final double MIN_LAT = 45.75;

    private static final double MAX_LAT = 45.85;

    private static final String server = "http://localhost:8090";

    private final long id;

    private final double longitude;

    private final double latitude;

    private final String ip = "127.0.0.1";

    private int port;

    private final long startTime;

    private final Retrofit retrofit;

    private Reading latestReading = new Reading(0, 0, 0, 0, 0, 0);

    private final List<String> readings;

    private Server grpcServer;

    private ManagedChannel channel = null;

    private ReadingGrpc.ReadingBlockingStub readingBlockingStub = null;

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private static final int MAX_TIMEOUTS = 5;

    private int timeoutCounter = 0;

    public Client() {
        this.startTime = System.currentTimeMillis() / 1000;

        Random rand = new Random();
        this.latitude = MIN_LAT + (MAX_LAT - MIN_LAT) * rand.nextDouble();
        this.longitude = MIN_LON + (MAX_LON - MIN_LON) * rand.nextDouble();

        startServer();
        this.readings = loadReadings();

        this.retrofit = new Retrofit.Builder().baseUrl(server)
                .addConverterFactory(JacksonConverterFactory.create()).build();
        this.id = register();
    }

    private List<String> loadReadings() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try {
            return Files.readAllLines(Paths.get(Objects.requireNonNull(
                    classloader.getResource("readings[2].csv")).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Error loading readings.");
        }
    }

    private void startServer() {
        try {
            grpcServer = ServerBuilder.forPort(0)
                    .addService(new ReadingService(this))
                    .build().start();

            this.port = grpcServer.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  Clean shutdown of server in case of JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server since JVM is shutting down");
            try {
                grpcServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                if (channel != null && !channel.isTerminated()) {
                    channel.awaitTermination(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.out.println("Server shut down");
        }));
    }

    private int register() {
        final SensorApi sensorApi = retrofit.create(SensorApi.class);
        final Sensor registration = new Sensor(latitude, longitude, ip, port);

        try {
            Response<Void> response = sensorApi.register(registration).execute();
            String location = response.headers().get("Location");
            return Integer.parseInt(location.substring(location.lastIndexOf('/')+1));
        } catch (IOException | NullPointerException e) {
            logger.info("Failed to register sensor.");
            return -1;
        }
    }

    private void start() {
        Sensor neighbour = null;
        final Random rand = new Random();
        final ReadingApi readingApi = retrofit.create(ReadingApi.class);

        while (true) {
            latestReading = generateReading();
            Reading currentReading = latestReading;

            if (neighbour == null) {
                neighbour = findClosest();
            }

            if (neighbour != null && channel == null) {
                channel = ManagedChannelBuilder.forAddress(neighbour.getIp(), neighbour.getPort()).usePlaintext().build();
                readingBlockingStub = ReadingGrpc.newBlockingStub(channel);
            }

            if (channel != null && !channel.isTerminated()) {
                currentReading = calibrateReading();
            }

            try {
                readingApi.saveReading(this.id, currentReading).execute();
                logger.info("Reading " + currentReading + " saved.");
            } catch (IOException | NullPointerException e) {
                logger.info("Failed to save the reading.");
            }

            try {
                Thread.sleep(rand.nextInt(5000));
            } catch (InterruptedException ignored) {}
        }
    }

    private Reading generateReading() {
        String line = readings.get((int) (System.currentTimeMillis() / 1000 - this.startTime) % 100 + 1);
        String[] parts = line.split(",");

        return new Reading(
                parts[0].equals("") ? 0 : Double.parseDouble(parts[0]),
                parts[1].equals("") ? 0 : Double.parseDouble(parts[1]),
                parts[2].equals("") ? 0 : Double.parseDouble(parts[2]),
                parts[3].equals("") ? 0 : Double.parseDouble(parts[3]),
                parts[4].equals("") ? 0 : Double.parseDouble(parts[4]),
                parts.length == 5 || parts[5].equals("") ? 0 : Double.parseDouble(parts[5]));
    }

    private Sensor findClosest() {
        final SensorApi sensorApi = retrofit.create(SensorApi.class);

        try {
            return sensorApi.getClosest(id).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Reading calibrateReading() {
        final ReadingRequest request = ReadingRequest.newBuilder().setId(this.id).build();

        logger.info("Sending gRPC request.");

        try {
            ReadingResponse response = readingBlockingStub.requestReading(request);
            logger.info("Response received.");
            timeoutCounter = 0;
            return calculateCalibration(this.getLatestReading(), response);
        } catch (StatusRuntimeException e) {
            timeoutCounter++;
            logger.info("RPC failed, timeout counter: " + timeoutCounter + "/" + MAX_TIMEOUTS + ".");

            if (timeoutCounter == MAX_TIMEOUTS) {
                channel.shutdown();
                logger.info("Timeout reached, channel shut down.");
            }
        }

        return this.latestReading;
    }

    private Reading calculateCalibration(Reading reading, ReadingResponse response) {
        return new Reading(
                reading.getTemperature() + response.getTemperature() /
                        (Math.min(reading.getTemperature(), response.getTemperature()) <= 0 ? 1 : 2),
                reading.getPressure() + response.getPressure() /
                        (Math.min(reading.getPressure(), response.getPressure()) <= 0 ? 1 : 2),
                reading.getHumidity() + response.getHumidity() /
                        (Math.min(reading.getHumidity(), response.getHumidity()) <= 0 ? 1 : 2),
                reading.getCo() + response.getCo() /
                        (Math.min(reading.getCo(), response.getCo()) <= 0 ? 1 : 2),
                reading.getNo2() + response.getNo2() /
                        (Math.min(reading.getNo2(), response.getNo2()) <= 0 ? 1 : 2),
                reading.getSo2() + response.getSo2() /
                        (Math.min(reading.getSo2(), response.getSo2()) <= 0 ? 1 : 2)
        );
    }

    public Reading getLatestReading() {
        return latestReading;
    }

    public static void main(String[] args) {
        Client cli = new Client();
        cli.start();
    }
}
