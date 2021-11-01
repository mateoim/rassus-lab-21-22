package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.model.Sensor;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SensorApi {

    @PUT("/sensors")
    Call<Void> register(@Body Sensor form);

    @GET("/sensors/closest/{id}")
    Call<Sensor> getClosest(@Path("id") long id);
}
