package pl.lbasista.magazynex.ui.orders;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.OrderDao;
import pl.lbasista.magazynex.data.OrderProduct;
import pl.lbasista.magazynex.data.OrderProductDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;

public class OrderDetailsActivity extends AppCompatActivity {
    private int currentOrderId;
    private TextView textEmptyProducts;
    private LinearLayout llProducts;
    private AppDatabase db;
    private OrderDao orderDao;
    private OrderProductDao orderProductDao;
    private TextInputLayout productListLayout, quantityLayout;
    private MaterialToolbar toolbar;

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

        TextView tvName = findViewById(R.id.tvOrderName);
        textEmptyProducts = findViewById(R.id.textEmptyProducts);
        llProducts = findViewById(R.id.llProducts);
        toolbar = findViewById(R.id.appBarOrderDetails);

        String orderName = getIntent().getStringExtra("orderName");
        tvName.setText(orderName);

        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.orderEdit) {
                View dialogEdit = getLayoutInflater().inflate(R.layout.dialog_edit_order, null);
                //Automatyczne uzupełnienie pola z istniejącą nazwą
                TextInputEditText inputOrderName = dialogEdit.findViewById(R.id.inputOrderName);
                String currentName = ((TextView) findViewById(R.id.tvOrderName)).getText().toString();
                inputOrderName.setText(currentName);

                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Edytuj listę").setView(dialogEdit).create();
                dialog.show();

                //Lista produktów
                new Thread(() -> {
                    List<OrderProduct> relations = db.orderProductDao().getByOrderId(currentOrderId);
                    List<Product> products = new ArrayList<>();
                    for (OrderProduct rel : relations) {
                        Product p = db.productDao().getById(rel.productId);
                        if (p != null) {
                            p.quantity = rel.count;
                            products.add(p);
                        }
                    }

                    runOnUiThread(() -> {
                        LinearLayout layoutProducts = dialogEdit.findViewById(R.id.layoutProductsEdit);
                        for (Product product : products) {
                            View row = getLayoutInflater().inflate(R.layout.row_edit_product, null);

                            TextView tvProdName = row.findViewById(R.id.tvProductName);
                            TextInputEditText inputCount = row.findViewById(R.id.inputEditCount);
                            ImageButton btnDelete = row.findViewById(R.id.btnDeleteProduct);

                            tvProdName.setText(product.name);
                            inputCount.setText(String.valueOf(product.quantity));
                            row.setTag(product.id);
                            layoutProducts.addView(row);

                            btnDelete.setOnClickListener(v -> {
                                new AlertDialog.Builder(this)
                                        .setTitle("Usuń produkt z listy")
                                        .setMessage("Czy na pewno chcesz usunąć wybrany przedmiot z tej listy?")
                                        .setPositiveButton("Tak", (dialogAsk, i) -> layoutProducts.removeView(row))
                                        .setNegativeButton("Nie", null)
                                        .show();
                            });
                        }
                    });
                }).start();
                dialogEdit.findViewById(R.id.buttonCancelEditOrder).setOnClickListener(v -> dialog.dismiss());
                dialogEdit.findViewById(R.id.buttonSaveEditOrder).setOnClickListener(v -> {
                    TextInputLayout inputOrderNameLayout = dialogEdit.findViewById(R.id.inputOrderNameLayout);
                    String newName = inputOrderName.getText().toString().trim();

                    if (newName.isEmpty()) {
                        inputOrderNameLayout.setError("Nazwa jest wymagana");
                        return;
                    } else {
                        inputOrderNameLayout.setError(null);
                    }

                    LinearLayout layoutProducts = dialogEdit.findViewById(R.id.layoutProductsEdit);
                    boolean hasError = false;
                    final int[] sumQuantity = {0};

                    for (int i = 0; i < layoutProducts.getChildCount(); i++) {
                        View row = layoutProducts.getChildAt(i);
                        int productId = (int) row.getTag();
                        TextInputEditText inputCount = row.findViewById(R.id.inputEditCount);
                        TextInputLayout inputCountLayout = row.findViewById(R.id.inputEditCountLayout);
                        String val = inputCount.getText().toString().trim();

                        if (val.isEmpty()) {
                            inputCountLayout.setError("Pole wymagane");
                            hasError = true;
                            continue;
                        } else {
                            inputCountLayout.setError(null);
                        }

                        try {
                            int qty = Integer.parseInt(val);
                            if (qty < 0) {
                                inputCountLayout.setError("Musi być większe od 0");
                                hasError = true;
                            } else if (qty > 0) {
                                //
                            }
                        } catch (NumberFormatException e) {
                            inputCountLayout.setError("Nieprawidłowa wartość");
                            hasError = true;
                        }
                    }
                    if (hasError) return;

                    new Thread(() -> {
                        Order order = db.orderDao().getById(currentOrderId);
                        order.name = newName;

                        db.orderProductDao().deleteAllByOrderId(currentOrderId);
                        for (int i = 0; i < layoutProducts.getChildCount(); i++) {
                            View row = layoutProducts.getChildAt(i);
                            int productId = (int) row.getTag();
                            TextInputEditText inputCount = row.findViewById(R.id.inputEditCount);
                            String val = inputCount.getText().toString().trim();
                            int qty = Integer.parseInt(val);

                            if (qty > 0) {
                                OrderProduct rel = new OrderProduct(currentOrderId, productId, qty);
                                db.orderProductDao().insert(rel);
                                sumQuantity[0] += qty;
                            }
                            order.quantity = sumQuantity[0];
                            db.orderDao().update(order);
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                            ((TextView) findViewById(R.id.tvOrderName)).setText(newName);
                            loadProducts();
                            dialog.dismiss();
                        });
                    }).start();
                });
                return true;
            } else if (id == R.id.orderAddProduct) {
                orderDao = AppDatabase.getInstance(this).orderDao();
                orderProductDao = AppDatabase.getInstance(this).orderProductDao();

                new Thread(() -> {
                    List<Product> products = db.productDao().getAllSync();

                    String[] names = new String[products.size()];
                    for (int i = 0; i < products.size(); i++) {
                        names[i] = products.get(i).name;
                    }

                    runOnUiThread(() -> {
                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_order, null);
                        AutoCompleteTextView dropdownProductList = dialogView.findViewById(R.id.dropdownProductList);
                        dropdownProductList.setInputType(InputType.TYPE_NULL);
                        dropdownProductList.setOnClickListener(dp -> dropdownProductList.showDropDown());
                        dropdownProductList.setOnFocusChangeListener((dp, hasFocus) -> {
                            if (hasFocus) dropdownProductList.showDropDown();
                        });
                        TextInputEditText inputQuantity = dialogView.findViewById(R.id.inputQuantity);
                        dialogView.findViewById(R.id.dropdownOrderList).setVisibility(View.GONE);

                        new Thread(() -> {
                            ProductDao productDao = AppDatabase.getInstance(this).productDao();
                            List<Product> productList = productDao.getAllSync();
                            String[] productNames = new String[productList.size()];
                            for (int i = 0; i < productList.size(); i++) {
                                productNames[i] = productList.get(i).name;
                            }

                            runOnUiThread(() -> {
                                ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productNames);
                                dropdownProductList.setAdapter(orderAdapter);
                            });
                        }).start();

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        AlertDialog dialog = builder.setTitle("Dodaj do listy").setView(dialogView).create();
                        dialog.show();

                        Button buttonAdd = dialogView.findViewById(R.id.buttonSave);
                        buttonAdd.setOnClickListener(dp -> {
                            String selectedProduct = dropdownProductList.getText().toString().trim();
                            String quantity = inputQuantity.getText().toString().trim();

                            productListLayout = dialogView.findViewById(R.id.dropdownProductListLayout);
                            quantityLayout = dialogView.findViewById(R.id.inputQuantityLayout);

                            boolean hasError = false;

                            if (selectedProduct.isEmpty()) {
                                productListLayout.setError("Wybierz produkt");
                                hasError = true;
                            } else {
                                productListLayout.setError(null);
                            }
                            if (quantity.isEmpty()) {
                                quantityLayout.setError("Podaj ilość");
                                hasError = true;
                            } else {
                                quantityLayout.setError(null);
                            }

                            int selectedProductId = -1;
                            for (Product product : products) {
                                if (product.name.equals(selectedProduct)) {
                                    selectedProductId = product.id;
                                    break;
                                }
                            }
                            if (selectedProductId == -1) {
                                productListLayout.setError("Nie znaleziono produktu");
                                hasError = true;
                            }

                            int selectedQuantity = 1;
                            try {
                                selectedQuantity = Integer.parseInt(quantity);
                                if (selectedQuantity <= 0) {
                                    quantityLayout.setError("Liczba musi być większa od 0");
                                    hasError = true;
                                }
                            } catch (NumberFormatException e) {
                                quantityLayout.setError("Nieprawidłowa wartość");
                                hasError = true;
                            }

                            if (hasError) return;

                            int finalProductId = selectedProductId;
                            int finalQuantity = selectedQuantity;
                            new Thread(() -> {
                                OrderProduct newRelation = new OrderProduct(currentOrderId, finalProductId, finalQuantity);
                                long result = orderProductDao.insert(newRelation);

                                if (result != -1)
                                    orderDao.addProductToOrder(currentOrderId, finalQuantity);

                                runOnUiThread(() -> {
                                    if (result == -1)
                                        Toast.makeText(this, "Produkt już jest na liście", Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(this, "Produkt dodano", Toast.LENGTH_SHORT).show();
                                        loadProducts();
                                        dialog.dismiss();
                                    }
                                });
                            }).start();
                        });

                        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
                        buttonCancel.setOnClickListener(dp -> dialog.dismiss());
                    });
                }).start();
                return true;
            } else if (id == R.id.orderRemove) {
                orderDao = AppDatabase.getInstance(this).orderDao();
                new AlertDialog.Builder(this)
                        .setTitle("Usuwanie listy")
                        .setMessage("Czy chcesz usunąć listę z bazy?")
                        .setPositiveButton("Tak", (dialog, which) -> {

                            new Thread(() -> {
                                Order toDelete = orderDao.getById(currentOrderId);
                                if (toDelete != null) {
                                    orderDao.delete(toDelete);

                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Lista usunięta", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }
                            }).start();
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
                return true;
            }
            return false;
        });

        textEmptyProducts.setVisibility(TextView.VISIBLE);
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        new Thread(() -> {
            List<OrderProduct> relations = db.orderProductDao().getByOrderId(currentOrderId);
            List<String> productLines = new ArrayList<>();

            for (OrderProduct rel : relations) {
                Product p = db.productDao().getById(rel.productId);
                if (p != null) {
                    productLines.add("• " + rel.count + "szt. " + p.name);
                }
            }

            runOnUiThread(() -> {
                llProducts.removeAllViews();
                if (productLines.isEmpty()) {
                    textEmptyProducts.setVisibility(View.VISIBLE);
                }
                else {
                    textEmptyProducts.setVisibility(View.GONE);
                    for (String line : productLines) {
                        TextView tv = new TextView(this);
                        tv.setText(line);
                        tv.setTextSize(16);
                        llProducts.addView(tv);
                    }
                }
            });
        }).start();
    }
}
