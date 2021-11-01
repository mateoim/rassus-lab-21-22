package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.model.RegistrationForm;
import hr.fer.tel.rassus.client.retrofit.RegistrationApi;
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
        final RegistrationApi registrationApi = retrofit.create(RegistrationApi.class);
        final RegistrationForm form = new RegistrationForm(latitude, longitude, ip, port);

        try {
            Response<Void> response = registrationApi.register(form).execute();
            String location = response.headers().get("Location");
            return Integer.parseInt(location.substring(location.lastIndexOf('/')+1));
        } catch (IOException | NullPointerException e) {
            System.out.println("Failed to register sensor.");
            return -1;
        }
    }

    public static void main(String[] args) {
        Client cli = new Client();
    }
}
