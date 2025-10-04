package pl.lbasista.magazynex.data.repo;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pl.lbasista.magazynex.data.ApplicationCategory;

public class RemoteCategoryRepository implements CategoryRepository {
    private final Context context;
    private final String apiUrl;

    public RemoteCategoryRepository(Context context, String apiUrl) {
        this.context = context.getApplicationContext();
        this.apiUrl = apiUrl;
    }

    @Override
    public List<ApplicationCategory> getAllCategories() {
        String url = apiUrl + "?action=categories";
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, future, future);
        queue.add(request);

        try {
            JSONArray array = future.get(10, TimeUnit.SECONDS);
            return parseCategoriesArray(array);
        } catch (Exception e) {
            Log.e("RemoteCategoryRepo", "getAllCategories error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<ApplicationCategory> parseCategoriesArray(JSONArray array) {
        List<ApplicationCategory> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                list.add(parseCategoryObject(obj));
            } catch (JSONException e) {Log.e("RemoteCategoryRepo", "parseCategoriesArray JSON error at i=" + i, e);}
        }
        return list;
    }

    @Override
    public ApplicationCategory getById(int id) {
        String url = apiUrl + "?action=category&id=" + id;
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, future, future);
        queue.add(request);

        try {
            JSONObject obj = future.get(10, TimeUnit.SECONDS);
            return parseCategoryObject(obj);
        } catch (Exception e) {
            Log.e("RemoteCategoryRepo", "getById error: " + e.getMessage());
            return null;
        }
    }

    private ApplicationCategory parseCategoryObject(JSONObject obj) {
        int id = obj.optInt("id", 0);
        String name = obj.optString("name", "");
        ApplicationCategory cat = new ApplicationCategory();
        cat.id = id;
        cat.name = name;
        return cat;
    }

    @Override
    public long insertCategory(ApplicationCategory category) {
        String url = apiUrl + "?action=insertCategory";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject body = new JSONObject();

        try {
            body.put("name", category.name);
        } catch (JSONException e) {
            Log.e("RemoteCategoryRepo", "JSON build error: " + e.getMessage());
        }

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public byte[] getBody() {return body.toString().getBytes();}
            @Override
            public String getBodyContentType() {return "application/json; charset=utf-8";}
        };

        queue.add(request);

        try {
            String resp = future.get(10, TimeUnit.SECONDS);
            try {
                JSONObject obj = new JSONObject(resp);
                return obj.optLong("id", 0L);
            } catch (JSONException ignore) {
                try {return Long.parseLong(resp.trim());}
                catch (NumberFormatException nf) {
                    Log.w("RemoteCategoryRepo", "Insert resp bez ID: " + resp);
                    return 0L;
                }
            }
        } catch (Exception e) {
            Log.e("RemoteCategoryRepo", "insertCategory error: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean updateCategory(ApplicationCategory category) {
        String url = apiUrl + "?action=updateCategory";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject body = new JSONObject();

        try {
            body.put("id", category.id);
            body.put("name", category.name);
        } catch (JSONException e) {
            Log.e("RemoteCategoryRepo", "JSON build error: " + e.getMessage());
            return false;
        }

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public byte[] getBody() {return body.toString().getBytes();}
            @Override
            public String getBodyContentType() {return "application/json; charset=utf-8";}
        };

        queue.add(request);

        try {
            String resp = future.get(10, TimeUnit.SECONDS);
            if (resp == null) return false;
            String trimmed = resp.trim().toLowerCase();

            if ("ok".equals(trimmed) || "true".equals(trimmed) || "1".equals(trimmed)) return true;

            try {
                JSONObject obj = new JSONObject(resp);
                return obj.optBoolean("success", false);
            } catch (JSONException ignore) {
                return !trimmed.isEmpty();
            }
        } catch (Exception e) {
            Log.e("RemoteCategoryRepo", "updateCategory error: " + e.getMessage());
            return false;
        }
    }
}