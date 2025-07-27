package pl.lbasista.magazynex.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OrderProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(OrderProduct orderProduct);

    @Update
    void update(OrderProduct orderProduct);

    @Query("SELECT * FROM order_product WHERE productId = :productId")
    List<OrderProduct> getByProductId(int productId);

    @Query("SELECT * FROM order_product Where orderId = :orderId")
    List<OrderProduct> getByOrderId(int orderId);

    @Query("DELETE FROM order_product WHERE orderId = :orderId AND productId = :productId")
    void delete(int orderId, int productId);

    @Query("DELETE FROM order_product WHERE orderId = :orderId")
    void deleteAllByOrderId(int orderId);

    @Query("SELECT SUM(count) FROM order_product WHERE productId = :id")
    int getTotalCountForProduct(int id);

    @Query("SELECT * FROM order_product WHERE orderId = :orderId AND productId = :productId LIMIT 1")
    OrderProduct getByOrderAndProduct(int orderId, int productId);
}
