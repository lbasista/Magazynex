package pl.lbasista.magazynex.data.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.OrderProduct;
import pl.lbasista.magazynex.data.Product;

public class RemoteOrderRepository implements OrderRepository{
    private final Context context;
    private final String apiUrl;

    public RemoteOrderRepository(Context context, String apiUrl) {
        this.context = context.getApplicationContext();
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Order> getAllOrders() {
        String url = apiUrl + "?action=orders";
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, future, future);
        queue.add(request);
        try {
            JSONArray ordersJson = future.get(10, TimeUnit.SECONDS);
            return parseOrders(ordersJson);
        } catch (Exception e) {
            Log.e("RemoteOrderRepo", "Błąd pobierania list: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Order> parseOrders(JSONArray response) {
        List<Order> list = new ArrayList<>();
        Log.d("REMOTE_ORDER_REPO", "parseOrders: pobrano " + response.length() + " zamówień");

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                int id = obj.optInt("id", 0);
                String name = obj.optString("name", "");
                int quantity = obj.optInt("quantity", 0);
                Log.d("REMOTE_ORDER_REPO", "Order id=" + id + ", name=" + name + ", qty=" + quantity);
                Order orderItem = new Order(name, quantity);

                try {orderItem.setId(id);} catch (Throwable noSetters) {orderItem.id = id;}

                list.add(orderItem);
            } catch (JSONException e) {Log.e("RemoteOrderRepo", "parseOrders: błąd JSON przy i=" + i, e);}
        }
        return list;
    }

    @Override
    public long insertOrder(Order order) {
            String url = apiUrl + "?action=insertOrder";
        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject body = new JSONObject();
        try {
            body.put("name", order.getName());
            body.put("quantity", order.getQuantity());
        } catch (JSONException e) {Log.e("RemoteOrderRepo", "Błąd JSON: " + e.getMessage());}

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
                    Log.w("RemoteOrderRepo", "Insert resp bez ID: " + resp);
                    return 0L;
                }
            }
        } catch (Exception e) {
            Log.e("RemoteOrderRepo", "Błąd dodawania zamówienia: " + e.getMessage());
            return 0L;
        }
    }

    public boolean deleteOrder(int orderId) {
        String url = apiUrl + "?action=deleteOrder";
        JSONObject body = new JSONObject();
        try {
            body.put("id", orderId);
        } catch (JSONException e) {
            Log.e("RemoteOrderRepo", "Błąd JSON: " + e.getMessage());
            return false;
        }

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest req = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public byte[] getBody() {return body.toString().getBytes();}

            @Override
            public String getBodyContentType() {return "application/json; charset=utf-8";}
        };

        Volley.newRequestQueue(context).add(req);
        try {
            String resp = future.get(10, TimeUnit.SECONDS);
            if (resp == null) return false;

            resp = resp.trim().toLowerCase();
            if ("ok".equals(resp) || "true".equals(resp) || "1".equals(resp)) return true;

            try {
                JSONObject obj = new JSONObject(resp);
                return obj.optBoolean("success", obj.optInt("deleted", 0) > 0);
            } catch (JSONException e) {
                return !resp.isEmpty();
            }
        } catch (Exception e) {
            Log.e("RemoteOrderRepo", "Błąd usuwania zamówienia: " + e.getMessage());
            return false;
        }
    }

    @Override
    public LiveData<List<Product>> getProductsForOrder(int orderId) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        String url = apiUrl + "?action=orderProducts&orderId=" + orderId;
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            List<Product> parsed = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject obj = response.getJSONObject(i);
                    int id = obj.optInt("id", 0);
                    String name = obj.optString("name", "");
                    String producer = obj.optString("producer", "");
                    String barcode = obj.optString("barcode", "");
                    int quantity = obj.optInt("quantity", 0);
                    String description = obj.optString("description", "");
                    boolean favourite = obj.optBoolean("favourite", false);

                    Product p = new Product(barcode, name, quantity, producer, favourite, 0, description, "");
                    p.id = id;
                    parsed.add(p);
                } catch (JSONException e) {Log.e("RemoteOrderRepo", "Błąd JSON: " + e.getMessage());}
            }
            data.setValue(parsed);
        }, error -> {
            Log.e("RemoteOrderRepo", "Błąd pobierania produktów dla zamówienia: " + error.getMessage());
            data.setValue(new ArrayList<>());
        });
        queue.add(request);
        return data;
    }

    public Map<Integer, Integer> getProductUsageMap() {
        Map<Integer, Integer> result = new HashMap<>();
        String url = apiUrl + "?action=productUsage";
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, future, future);
        Volley.newRequestQueue(context).add(request);

        try {
            JSONArray arr = future.get(10, TimeUnit.SECONDS);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int productId = obj.optInt("productId", 0);
                int used = obj.optInt("used", 0);
                result.put(productId, used);
            }
        } catch (Exception e) {
            Log.e("RemoteOrderRepo", "Błąd pobierania map: " + e.getMessage());
        }
        return result;
    }

    @Override
    public LiveData<List<Order>> getOrdersForProduct(int productId) {
        MutableLiveData<List<Order>> data = new MutableLiveData<>();
        String url = apiUrl + "?action=productOrders&productId=" + productId;
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            Log.d("RemoteOrderRepo", "productOrders URL=" + url + ", response size=" + response.length());
            List<Order> parsed = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject obj = response.getJSONObject(i);
                    int id = obj.optInt("id", 0);
                    String name = obj.optString("name", "");
                    int quantity = obj.optInt("quantity", 0);

                    Order o = new Order(name, quantity);
                    try {o.setId(id);}
                    catch (Throwable t) {o.id = id;}
                    parsed.add(o);
                } catch (JSONException e) {Log.e("RemoteOrderRepo", "Błąd JSON: " + e.getMessage());}
            }
            data.setValue(parsed);
        }, error -> {
            Log.e("RemoteOrderRepo", "Błąd pobierania list dla produktu: " + error.getMessage());
            data.setValue(new ArrayList<>());
        });
        queue.add(request);
        return data;
    }

    @Override
    public boolean addProductToOrder(int orderId, int productId, int count) {
        String url = apiUrl + "?action=addProductToOrder";
        Log.d("API", "addProductToOrder → URL=" + url + " orderId=" + orderId + " productId=" + productId + " count=" + count);

        JSONObject body = new JSONObject();
        try {
            body.put("orderId", orderId);
            body.put("productId", productId);
            body.put("count", count);
        } catch (JSONException e) {
            Log.e("RemoteOrderRepo", "JSON build error: " + e.getMessage());
            return false;
        }

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public byte[] getBody() {
                Log.d("API", "Request body=" + body.toString());
                return body.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {return "application/json; charset=utf-8";}
        };

        Volley.newRequestQueue(context).add(request);

        try {
            String resp = future.get(10, TimeUnit.SECONDS);
            Log.d("API", "Response raw=" + resp);
            if (resp == null) return false;
            String trimmed = resp.trim().toLowerCase();
            if ("ok".equals(trimmed) || "true".equals(trimmed) || "1".equals(trimmed)) return true;
            try {
                JSONObject obj = new JSONObject(resp);
                return obj.optBoolean("success", obj.optInt("updated", 0) > 0 || obj.optInt("inserted", 0) > 0);
            } catch (JSONException ignore) {
                return !trimmed.isEmpty();
            }
        } catch (Exception e) {
            Log.e("RemoteOrderRepo", "API addProductToOrder error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateOrderHeader(int orderId, String newName, int newTotalQuantity) {
        String url = apiUrl + "?action=updateOrderHeader";
        JSONObject body = new JSONObject();
        try {
            body.put("id", orderId);
            body.put("name", newName);
            body.put("quantity", newTotalQuantity);
        } catch (JSONException e) {
            Log.e("RemoteOrderRepo", "Błąd JSON: " + e.getMessage());
            return false;
        }

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest req = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            public byte[] getBody() {return body.toString().getBytes();}
            @Override
            public String getBodyContentType() {return "application/json; charset=utf-8";}
        };

        Volley.newRequestQueue(context).add(req);
        try {
            String resp = future.get(10, TimeUnit.SECONDS);
            return resp != null && resp.trim().equalsIgnoreCase("ok");
        } catch (Exception e) {
            Log.e("RemoteOrderRepo", "Błąd aktualizacji zamówienia: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean replaceOrderProduct(int orderId, List<OrderProduct> newProducts) {
        return false;
    }
}
