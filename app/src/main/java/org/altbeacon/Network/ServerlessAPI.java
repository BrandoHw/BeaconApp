package org.altbeacon.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.api.tokens.IdentityToken;

import org.altbeacon.WorkTracking.R;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Handles requests to the web service to perform cloud functions
 */
public class ServerlessAPI {

  public static final MediaType JSON =
    MediaType.parse("application/json; charset=utf-8");

  private static final String createDbUrl = "https://00ed78f9.us-south.apigw.appdomain.cloud/cdb/createdb";
  private static final String getCredUrl =  "https://00ed78f9.us-south.apigw.appdomain.cloud/gc/getcredentials";

  private static final OkHttpClient client = new OkHttpClient();


  /**
   * Retrieves credentials from the web service which are use to synchronize local database with cloud database
   * Credentials are also used to view database if user has "MANAGER" permissions
   */
  public static URI getCredentials(AccessToken accessToken, String db_name, String role) throws Exception {
    Log.i(ServerlessAPI.class.getName(), "Retrieving credentials for database: " + db_name);
    Map<String, Object> bodyContent = new HashMap<String, Object>();
    bodyContent.put("role", role);
    bodyContent.put("db_name", db_name);
    RequestBody body = RequestBody.create(JSON, new Gson().toJson(bodyContent));
    Request request = new Request.Builder()
      .url(getCredUrl)
      .addHeader("Authorization", "Bearer " + accessToken.getRaw())
      .post(body)
      .build();
    Response response = client.newCall(request).execute();
    Log.i(ServerlessAPI.class.getName(), " Received Credentials: " + response.body().string());
    return new URI(response.body().string());
  }


  /**
   * Requests the web service to create a database in the cloud
   */
  public void createDatabase(AccessToken accessToken, String db_name) throws Exception {
    Log.i(ServerlessAPI.class.getName(), "Create database: " + db_name);
    Map<String, Object> database_name = new HashMap<String, Object>();
    database_name.put("dbname", db_name);
    RequestBody body = RequestBody.create(JSON, new Gson().toJson(database_name));
    Request request = new Request.Builder()
            .url(createDbUrl)
            .addHeader("Authorization", "Bearer " + accessToken.getRaw())
            .post(body)
            .build();
    Response response = client.newCall(request).execute();
    Log.i(ServerlessAPI.class.getName(), "Feedback sent: " + response.body().string());
  }
}
