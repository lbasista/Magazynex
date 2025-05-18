package pl.lbasista.magazynex.ui.orders;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.OrderProduct;
import pl.lbasista.magazynex.data.Product;

public class OrderDetailsActivity extends AppCompatActivity {
    private int currentOrderId;
    private TextView textEmptyProducts;
    private LinearLayout llProducts;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        db = AppDatabase.getInstance(this);

        currentOrderId = getIntent().getIntExtra("orderId", -1);

        if (currentOrderId < 0) {
            Toast.makeText(this, "Błąd: nieznane ID listy", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvBack = findViewById(R.id.tvBack);
        TextView tvEdit = findViewById(R.id.tvEdit);
        TextView tvName = findViewById(R.id.tvOrderName);
        textEmptyProducts = findViewById(R.id.textEmptyProducts);
        llProducts = findViewById(R.id.llProducts);
        Button btnAdd = findViewById(R.id.buttonAdd);

        String orderName = getIntent().getStringExtra("orderName");
        tvName.setText(orderName);

        tvBack.setOnClickListener(v -> finish());

        tvEdit.setOnClickListener(v -> Toast.makeText(this, "Edycja [WIP]", Toast.LENGTH_SHORT).show());

        textEmptyProducts.setVisibility(TextView.VISIBLE);

        btnAdd.setOnClickListener(v -> {
            new Thread(() -> {
                List<Product> products = db.productDao().getAllSync();

                String[] names = new String[products.size()];
                for (int i = 0; i < products.size(); i++) {
                    names[i] = products.get(i).name;
                }

                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Wybierz produkt do dodania")
                            .setItems(names, (dialog, which) -> {
                                Product chosen = products.get(which);
                                new Thread(() -> {
                                    long result = db.orderProductDao().insert(new OrderProduct(currentOrderId, chosen.id));

                                    if (result != -1) {
                                        db.orderDao().addProductToOrder(currentOrderId);
                                    }
                                    runOnUiThread(() -> {
                                        if (result == -1) {
                                            Toast.makeText(this, "Produkt już jest na tej liście", Toast.LENGTH_SHORT).show();
                                        } else {
                                            textEmptyProducts.setVisibility(View.GONE);
                                            loadProducts();
                                        }
                                    });
                                }).start();
                            })
                            .setNegativeButton("Anuluj", null)
                            .show();
                });
            }).start();
        });

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        new Thread(() -> {
            List<Product> products = db.orderProductDao().getProductsForOrder(currentOrderId);

            runOnUiThread(() -> {
                llProducts.removeAllViews();
                if (products.isEmpty()) {
                    textEmptyProducts.setVisibility(View.VISIBLE);
                } else {
                    textEmptyProducts.setVisibility(View.GONE);
                    for (Product p : products) {
                        TextView tv = new TextView(this);
                        tv.setText("• " + p.name);
                        tv.setTextSize(16);
                        llProducts.addView(tv);
                    }
                }
            });
        }).start();
    }
}
