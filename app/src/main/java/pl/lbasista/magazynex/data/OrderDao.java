package pl.lbasista.magazynex.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insert(Order order);

    @Query("SELECT * FROM orders")
    List<Order> getAllOrders();

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    Order getById(int id);

    @Update
    void update(Order order);

    @Delete
    void delete(Order order);

    @Query("UPDATE orders SET quantity = quantity + :count WHERE id = :orderId")
    void addProductToOrder(int orderId, int count);

    @Query("SELECT p.id, p.name, p.producer, p.barcode, p.description, p.favourite, p.applicationCategoryId, p.imageUri, " + //Pola produktu
            "op.count AS quantity " + //Nadpisanie ilości
            "FROM product p INNER JOIN order_product op ON p.id = op.productId WHERE op.orderId = :orderId")
    LiveData<List<Product>> getProductsForOrder(int orderId);

    @Query("SELECT o.id, o.name, op.count AS quantity " +
            "FROM orders o INNER JOIN order_product op ON o.id = op.orderId WHERE op.productId = :productId")
    LiveData<List<Order>> getOrdersForProduct(int productId);
}