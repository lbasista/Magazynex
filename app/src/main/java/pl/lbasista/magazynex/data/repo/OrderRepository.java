package pl.lbasista.magazynex.data.repo;

import androidx.lifecycle.LiveData;

import java.util.List;

import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.Product;

public interface OrderRepository {
    List<Order> getAllOrders();
    long insertOrder(Order order);
    LiveData<List<Product>> getProductsForOrder(int orderId);
    LiveData<List<Order>> getOrdersForProduct(int productId);
    boolean addProductToOrder(int orderId, int productId, int count);
}