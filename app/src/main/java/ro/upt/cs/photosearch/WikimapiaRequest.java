package ro.upt.cs.photosearch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Callback;

/**
 * Class for making search requests to wikimapia
 */
abstract public class WikimapiaRequest {
    private static final String FORMAT = "format";
    private static final String FORMAT_JSON = "json";
    private static final String PAGE = "page";
    private static final String PACK = "pack";
    private static final String CATEGORY = "category";
    private static final String COUNT = "count";

    private WikimapiaClient client;
    private Map<String, String> parameters = new HashMap<String, String>();

    protected WikimapiaRequest(WikimapiaClient client) {
        this.client = client;
        parameters.put(FORMAT, FORMAT_JSON);
    }

    protected void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public WikimapiaRequest setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("The page number must be more than 0");
        }
        parameters.put(PAGE, Integer.toString(page));
        return this;
    }

    public WikimapiaRequest setCount(int count) {
        parameters.put(COUNT, Integer.toString(count));
        return this;
    }

//    public WikimapiaRequest pack(ResponsePack pack) {
//        addParameter(PACK, pack.name().toLowerCase());
//        return this;
//    }
//
//    public WikimapiaRequest category(long categoryId) {
//        addParameter(CATEGORY, Long.toString(categoryId));
//        return this;
//    }
//
//    public WikimapiaRequest categoriesOr(Collection<Long> categoryIds) {
//        String categories = "";
//        for (Long l : categoryIds) {
//            categories += l.toString();
//        }
//        addParameter("categories_or", categories);
//        return this;
//    }

    protected abstract String getFunctionName();

    protected Map<String, String> getParameters() {
        return parameters;
    }

    //execute call synchronously ; block current thread while transfering data ;
    //after call executed, retrieve the body of the response, already on user object thanks to GsonConverterFactory
    public void execute(Callback<SearchResult> callback) {
        client.search(this, callback);
    }
}
