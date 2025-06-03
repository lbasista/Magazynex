package pl.lbasista.magazynex.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(OrderProduct orderProduct);

    @Query("SELECT * FROM order_product WHERE productId = :productId")
    List<OrderProduct> getByProductId(int productId);

    @Query("SELECT p.* FROM Product p INNER JOIN order_product op ON p.id = op.productId WHERE op.orderId = :orderId")
    List<Product> getProductsForOrder(int orderId);

    @Query("DELETE FROM order_product WHERE orderId = :orderId AND productId = :productId")
    void delete(int orderId, int productId);
}
