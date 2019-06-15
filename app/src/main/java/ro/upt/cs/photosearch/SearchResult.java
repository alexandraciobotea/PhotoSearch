package ro.upt.cs.photosearch;

import java.util.LinkedList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ro.upt.cs.photosearch.entities.Place;

/**
 * Represents a set of results returned by the wikimapia API
 */
public class SearchResult {
    private int page = 0;
    private int count = 0;
    private int found = 0;
    private String language = "";
    private List<Place> places = new LinkedList<Place>();

    protected SearchResult() {
    }

    public int getPage() {
        return page;
    }

    public String getLanguage() {
        return language;
    }

    public int getCount() {
        return count;
    }

    public List<Place> getPlaces() {
        return places;
    }
}
