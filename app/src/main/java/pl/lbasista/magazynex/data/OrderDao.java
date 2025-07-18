package pl.lbasista.magazynex.data;

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
}
