package org.altbeacon.Retrofit;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UpdateSqlApi {

    @POST("update")
    Call<SqlResponse> postUpdate(@Header("Authorization: Bearer" ) String accessToken, @Body DatabaseEntry dbe);
}
