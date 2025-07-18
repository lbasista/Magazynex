package pl.lbasista.magazynex.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "order_product",
        primaryKeys = {"orderId", "productId"},
        foreignKeys = {
                @ForeignKey(entity = Order.class, parentColumns = "id", childColumns = "orderId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("orderId"),
                @Index("productId")
        }
)

public class OrderProduct {
    public final int orderId;
    public final int productId;
    public int count;

    @Ignore
    public OrderProduct(int orderId, int productId) {
        this.orderId = orderId;
        this.productId = productId;
    }

    public OrderProduct(int orderId, int productId, int count) {
        this.orderId = orderId;
        this.productId = productId;
        this.count = count;
    }
}
