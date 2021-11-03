package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.model.Reading;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReadingApi {

    @PUT("/readings/sensor/{id}")
    Call<Void> saveReading(@Path("id") long id, @Body Reading reading);
}
