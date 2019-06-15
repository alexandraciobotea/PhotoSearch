package ro.upt.cs.photosearch;

import android.support.annotation.NonNull;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Wikimapia api interface
 */
// API Service
interface WikimapiaApi {
    /**
     * Asynchronous search method
     *
     * @param apiKey
     * @param functionName
     * @param parameters
     * @param callback
     */
    @GET("/")
    // "/" absolute path to host
    // HTTP annotation @GET
    // SearchResult class, where results are passed by Retrofit
    Call<SearchResult> search(@Query("key") String apiKey, @Query("function") String functionName, @QueryMap Map<String, String> parameters);
    //void search(@Query("key") String apiKey, @Query("function") String functionName, @QueryMap Map<String, String> parameters, @Query("callback") Callback<SearchResult> callback);
}
