package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.model.Sensor;
import hr.fer.tel.rassus.client.retrofit.SensorApi;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Random;

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

    private final int port = 8080;

    private final long startTime;

    private final Retrofit retrofit;

    public Client() {
        Random rand = new Random();

        this.latitude = MIN_LAT + (MAX_LAT - MIN_LAT) * rand.nextDouble();
        this.longitude = MIN_LON + (MAX_LON - MIN_LON) * rand.nextDouble();
        this.startTime = System.currentTimeMillis();

        this.retrofit = new Retrofit.Builder().baseUrl(server)
                .addConverterFactory(JacksonConverterFactory.create()).build();
        this.id = register();
    }

    private int register() {
        final SensorApi sensorApi = retrofit.create(SensorApi.class);
        final Sensor registration = new Sensor(latitude, longitude, ip, port);

        try {
            Response<Void> response = sensorApi.register(registration).execute();
            String location = response.headers().get("Location");
            return Integer.parseInt(location.substring(location.lastIndexOf('/')+1));
        } catch (IOException | NullPointerException e) {
            System.out.println("Failed to register sensor.");
            return -1;
        }
    }

    private Sensor findClosest() {
        final SensorApi sensorApi = retrofit.create(SensorApi.class);

        try {
            return sensorApi.getClosest(id).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Client cli = new Client();
        System.out.println(cli.findClosest());
    }
}
