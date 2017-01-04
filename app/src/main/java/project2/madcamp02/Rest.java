package project2.madcamp02;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * Created by q on 2017-01-03.
 */

public class Rest {
    private String url = "http://143.248.234.144:8080";
    private String method;
    private String body;

    public Rest(String url, String method, String body) {
        this.url += url;
        this.method = method;
        this.body = body;
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    public String Get() throws IOException{
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String Post() throws IOException{
        okhttp3.RequestBody bodyinpost = okhttp3.RequestBody.create(JSON, body);
        okhttp3.Request request = new okhttp3.Request.Builder() .url(url) .post(bodyinpost) .build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }
    public String Delete() throws IOException{
        okhttp3.RequestBody bodyindelete = okhttp3.RequestBody.create(JSON, body);
        okhttp3.Request request = new okhttp3.Request.Builder() .url(url) .delete(bodyindelete) .build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }
    public String Put() throws IOException{
        okhttp3.RequestBody bodyinput = okhttp3.RequestBody.create(JSON, body);
        okhttp3.Request request = new okhttp3.Request.Builder() .url(url) .put(bodyinput) .build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }
}