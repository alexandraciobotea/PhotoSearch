package ro.upt.cs.photosearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client class for the Wikimapia API
 */
public class WikimapiaClient {
    public String apiKey;

    public WikimapiaClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public WikimapiaApi getApi() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit adapter = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("http://api.wikimapia.org")
                .build();

        return adapter.create(WikimapiaApi.class);
    }

    public WikimapiaRequest getNearest(double longitude, double latitude) {
        return new GetNearestRequest(this, longitude, latitude);
    }

    protected void search(WikimapiaRequest request, Callback<SearchResult> callback) {
        WikimapiaApi api = getApi();
        // returns a call task
        Call<SearchResult> aux = api.search(apiKey, request.getFunctionName(), request.getParameters());
        //enqueue method for when API sends response
        aux.enqueue(callback);
    }
}
