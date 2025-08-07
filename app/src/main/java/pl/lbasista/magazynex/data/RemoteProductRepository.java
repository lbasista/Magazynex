package pl.lbasista.magazynex.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteProductRepository implements ProductRepository {
    public interface ProductCallback {
        void onResult(List<Product> products);
    }
    private final Context context;
    private final String apiUrl;
    public RemoteProductRepository(Context context, String apiUrl) {
        this.context = context;
        this.apiUrl = apiUrl;
    }

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> favourites = new MutableLiveData<>(new ArrayList<>());

    public void loadFromApi() {
        if (apiUrl == null || apiUrl.isEmpty()) {
            Log.e("API", "Brak adresu API - nie nastąpi wywołanie zapytania");
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, apiUrl, null, response -> {
            Log.d("API", "Pobrano z API: " + response.length() + " elementów");
            products.setValue(parseProducts(response));
        }, error -> {
            Log.e("API", "Błąd API: " + error.getMessage());
        });
        queue.add(request);
    }

    private List<Product> parseProducts(JSONArray response) {
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                int id = obj.optInt("id", 0);
                String name = obj.optString("name", "");
                String producer = obj.optString("manufacturer", "");
                String barcode = obj.optString("barcode", "");
                int quantity = obj.optInt("quantity", 0);
                String description = obj.optString("description", "");
                boolean favourite = obj.optBoolean("favourite", false);

                Product p = new Product(barcode, name, quantity, producer, favourite, 0, description, "");
                p.id = id;
                list.add(p);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public LiveData<List<Product>> getAllProducts() {
        return products;
    }

    @Override
    public LiveData<List<Product>> searchProducts(String query) {
        List<Product> source = products.getValue();
        MutableLiveData<List<Product>> result = new MutableLiveData<>();
        if (source == null) {
            result.setValue(Collections.emptyList());
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : source) {
                if (p.name != null && p.name.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(p);
                }
            }
            result.setValue(filtered);
        }
        return result;
    }

    @Override
    public LiveData<List<Product>> getFavourites() {return favourites;}
    public void fetchFavouritesFromApi() {
        String url = apiUrl + "?action=favourites";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<Product> parsed = parseProducts(response);
                    Log.d("REMOTE_REPO", "fetchFavouritesFromApi: pobrano " + parsed.size() + " produktów");
                    for (Product p : parsed) {
                        Log.d("REMOTE_REPO", "Product id=" + p.id + ", fav=" + p.favourite);
                    }
                    favourites.setValue(parseProducts(response));
                },
                error -> Log.e("API", "Błąd pobierania ulubionych: " + error.getMessage()));
        queue.add(request);
    }

    public void fetchAllProductsFromApi() {
        if (apiUrl == null || apiUrl.isEmpty()) return;
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, apiUrl, null,
                resonse -> products.setValue(parseProducts(resonse)),
                error -> Log.e("RemoteRepo", "Błąd fetchowania wszystkich produktów"));
        queue.add(request);
    }

    @Override
    public void toggleFavourite(Product product) {
        boolean newVal = !product.favourite;
        Log.d("TOGGLE", "ID: " + product.id + ", current fav: " + product.favourite + ", new fav: " + newVal);

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = apiUrl + "?action=toggleFavourite";
        JSONObject body = new JSONObject();
        try {
            body.put("id", product.id);
            body.put("favourite", newVal);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("API", "Zaktualizowano produkt: " + product.id);
                    fetchAllProductsFromApi();
                    fetchFavouritesFromApi();
                },error -> Log.e("API", "Błąd zmiany ulubionych: ", error)
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        queue.add(request);
    }

    @Override
    public void reloadFavourites(ProductCallback callback) {
        String url = apiUrl + "?action=favourites";
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            List<Product> favourites = parseProducts(response);
            Log.d("API", "reloadFavourites – ulubione: " + favourites.size());
            callback.onResult(favourites);
        }, error -> {
            Log.e("API", "Błąd reloadFavourites: " + error.getMessage());
            callback.onResult(Collections.emptyList());
        });
        queue.add(request);
    }
}