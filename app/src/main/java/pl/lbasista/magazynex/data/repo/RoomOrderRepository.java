package pl.lbasista.magazynex.data.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.OrderProduct;
import pl.lbasista.magazynex.data.OrderProductDao;
import pl.lbasista.magazynex.data.Product;

public class RoomOrderRepository implements OrderRepository {
    private final Context context;

    public RoomOrderRepository(Context context) {this.context = context;}

    @Override
    public List<Order> getAllOrders() {return AppDatabase.getInstance(context).orderDao().getAllOrders();}

    @Override
    public long insertOrder(Order order) {return AppDatabase.getInstance(context).orderDao().insert(order);}

    @Override
    public LiveData<List<Product>> getProductsForOrder(int orderId) {return AppDatabase.getInstance(context).orderDao().getProductsForOrder(orderId);}

    @Override
    public LiveData<List<Order>> getOrdersForProduct(int productId) {return AppDatabase.getInstance(context).orderDao().getOrdersForProduct(productId);}

    @Override
    public boolean addProductToOrder(int orderId, int productId, int count) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            OrderProductDao opDao = db.orderProductDao();
            OrderProduct existing = opDao.getByOrderAndProduct(orderId, productId);
            if (existing != null) {
                existing.count += count;
                opDao.update(existing);
            } else {
                OrderProduct rel = new OrderProduct(orderId, productId, count);
                opDao.insert(rel);
            }
            db.orderDao().addProductToOrder(orderId, count);
            return true;
        } catch (Throwable t) {
            Log.e("RoomOrderRepo", "addProductToOrder error", t);
            return false;
        }
    }

    @Override
    public boolean updateOrderHeader(int orderId, String newName, int newTotalQuantity) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            Order order = db.orderDao().getById(orderId);
            if (order == null) return false;

            order.name = newName;
            order.quantity = newTotalQuantity;
            db.orderDao().update(order);
            return true;
        } catch (Throwable t) {
            Log.e("RoomOrderRepo", "updateOrderHeader error", t);
            return false;
        }
    }

    @Override
    public boolean replaceOrderProduct(int orderId, List<OrderProduct> newProducts) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            OrderProductDao opDao = db.orderProductDao();
            opDao.deleteAllByOrderId(orderId);

            for (OrderProduct p : newProducts) opDao.insert(p);
            return true;
        } catch (Throwable t) {
            Log.e("RoomOrderRepo", "replaceOrderHeader error", t);
            return false;
        }
    }
}