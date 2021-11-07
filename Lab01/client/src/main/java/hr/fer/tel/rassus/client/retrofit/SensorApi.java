package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.model.Sensor;
import retrofit2.Call;
import retrofit2.http.*;

public interface SensorApi {

    @POST("/sensors")
    Call<Void> register(@Body Sensor form);

    @GET("/sensors/closest/{id}")
    Call<Sensor> getClosest(@Path("id") long id);
}
