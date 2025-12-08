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

import pl.lbasista.magazynex.data.User;

public class RemoteUserRepository implements UserRepository {
    private static final String TAG = "RemoteUserRepo";
    private static final int TIMEOUT = 10;

    private final Context context;
    private final String apiUrl;

    public RemoteUserRepository(Context context, String apiUrl) {
        this.context = context.getApplicationContext();
        this.apiUrl = apiUrl;
    }

    @Override
    public List<User> getAllUsers() {
        String url = apiUrl + "?action=users";
        RequestQueue queue = Volley.newRequestQueue(context);

        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, future, future);
        queue.add(request);

        try {
            JSONArray array = future.get(TIMEOUT, TimeUnit.SECONDS);
            List<User> out = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) out.add(parseUser(obj));
            }
            return out;
        } catch (Exception e) {
            Log.e(TAG, "getAllUsers error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public User getById(int id) {
        String url = apiUrl + "?action=user&id=" + id;
        RequestQueue queue = Volley.newRequestQueue(context);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, future, future);
        queue.add(request);

        try {
            JSONObject obj = future.get(TIMEOUT, TimeUnit.SECONDS);
            return parseUser(obj);
        } catch (Exception e) {
            Log.e(TAG, "getById error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public User getByLogin(String login) {
        String url = apiUrl + "?action=user&login=" + login;
        RequestQueue queue = Volley.newRequestQueue(context);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, future, future);
        queue.add(request);

        try {
            JSONObject obj = future.get(TIMEOUT, TimeUnit.SECONDS);
            return parseUser(obj);
        } catch (Exception e) {
            Log.e(TAG, "getByLogin error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public long insertUser(User user) {
        String url = apiUrl + "?action=insertUser";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject body = toJson(user, false);

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json; charset=utf-8"; }
        };
        queue.add(request);

        try {
            String resp = future.get(TIMEOUT, TimeUnit.SECONDS);
            try {
                JSONObject obj = new JSONObject(resp);
                return obj.optLong("id", 0L);
            } catch (JSONException notJson) {
                return Long.parseLong(resp.trim());
            }
        } catch (Exception e) {
            Log.e(TAG, "insertUser error: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean updateUser(User user) {
        String url = apiUrl + "?action=updateUser";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject body = toJson(user, true);

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json; charset=utf-8"; }
        };
        queue.add(request);

        try {
            String resp = future.get(TIMEOUT, TimeUnit.SECONDS);
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
            Log.e(TAG, "updateUser error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteUser(int id) {
        if (id <= 0) return false;
        String url = apiUrl + "?action=deleteUser";
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject body = new JSONObject();
        try { body.put("id", id); } catch (JSONException ignore) {}

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json; charset=utf-8"; }
        };
        queue.add(request);

        try {
           String resp = future.get(TIMEOUT, TimeUnit.SECONDS);
           if (resp == null) return false;
           try {
               JSONObject json = new JSONObject(resp);
               boolean ok = json.optBoolean("success", false);
               if (!ok) ok = json.optInt("affected", 0) > 0;
               return ok;
           } catch (JSONException notJson) {
               return "ok".equalsIgnoreCase(resp.trim());
           }
        } catch (Exception e) {
            Log.e(TAG, "deleteUser error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void fetchAllUsersFromApi() {
        try {
            getAllUsers();
        } catch (Exception ignored) {}
    }

    private static User parseUser(JSONObject obj) throws JSONException {
        if (obj == null) return null;
        User user = new User();
        user.id = obj.optInt("id", 0);
        user.name = obj.optString("name", "");
        user.surname = obj.optString("surname", "");
        user.login = obj.optString("login", "");
        user.password = obj.optString("password", "");
        user.role = obj.optString("role", User.ROLE_VIEWER);
        return user;
    }

    private static JSONObject toJson(User user, boolean includeId) {
        JSONObject obj = new JSONObject();
        try {
            if (includeId) obj.put("id", user.id);
            obj.put("name", user.name);
            obj.put("surname", user.surname);
            obj.put("login", user.login);
            obj.put("password", user.password);
            obj.put("role", user.role);
        } catch (JSONException ignored) {}
        return obj;
    }
}
