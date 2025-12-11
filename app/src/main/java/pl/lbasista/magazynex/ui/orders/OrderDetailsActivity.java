package pl.lbasista.magazynex.ui.orders;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.OrderDao;
import pl.lbasista.magazynex.data.OrderProduct;
import pl.lbasista.magazynex.data.OrderProductDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.repo.OrderRepository;
import pl.lbasista.magazynex.data.repo.RemoteOrderRepository;
import pl.lbasista.magazynex.data.repo.RemoteProductRepository;
import pl.lbasista.magazynex.data.repo.RoomOrderRepository;
import pl.lbasista.magazynex.ui.user.RoleChecker;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class OrderDetailsActivity extends AppCompatActivity {
    private int currentOrderId;
    private TextView textEmptyProducts;
    private LinearLayout llProducts;
    private AppDatabase db;
    private OrderDao orderDao;
    private OrderProductDao orderProductDao;
    private TextInputLayout productListLayout, quantityLayout;
    private MaterialToolbar toolbar;
    private OrderRepository repository;
    private RemoteOrderRepository remoteRepo;
    private RoomOrderRepository roomRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        SessionManager session = new SessionManager(this);
        boolean isRemote = session.isRemoteMode();
        if (isRemote) {
            remoteRepo = new RemoteOrderRepository(this, session.getApiUrl());
            repository = remoteRepo;
        } else {
            roomRepo = new RoomOrderRepository(this);
            repository = roomRepo;
        }

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

        if (RoleChecker.isViewer(new SessionManager(this))) {
            toolbar.getMenu().findItem(R.id.orderEdit).setVisible(false);
            toolbar.getMenu().findItem(R.id.orderAddProduct).setVisible(false);
            toolbar.getMenu().findItem(R.id.orderRemove).setVisible(false);
        }

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

                    for (int i = 0; i < layoutProducts.getChildCount(); i++) {
                        View row = layoutProducts.getChildAt(i);
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
                            }
                        } catch (NumberFormatException e) {
                            inputCountLayout.setError("Nieprawidłowa wartość");
                            hasError = true;
                        }
                    }
                    if (hasError) return;

                    new Thread(() -> {
                        List<OrderProduct> newProducts = new ArrayList<>();
                        int sumQuantity = 0;

                        for (int i = 0; i < layoutProducts.getChildCount(); i++) {
                            View row = layoutProducts.getChildAt(i);
                            int productId = (int) row.getTag();
                            TextInputEditText inputCount = row.findViewById(R.id.inputEditCount);
                            String val = inputCount.getText().toString().trim();
                            int qty = Integer.parseInt(inputCount.getText().toString().trim());

                            if (qty > 0) {
                                newProducts.add(new OrderProduct(currentOrderId, productId, qty));
                                sumQuantity += qty;
                            }
                        }
                        boolean okHeader = repository.updateOrderHeader(currentOrderId, newName, sumQuantity);
                        boolean okProducts = repository.replaceOrderProduct(currentOrderId, newProducts);
                        boolean finalOk = okHeader && okProducts;

                        runOnUiThread(() -> {
                            if (finalOk) {
                                Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                                ((TextView) findViewById(R.id.tvOrderName)).setText(newName);
                                loadProducts();
                                dialog.dismiss();
                            } else Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                });
                return true;
            } else if (id == R.id.orderAddProduct) {
                new Thread(() -> {
                    List<Product> products;
                    if (isRemote) {
                        RemoteProductRepository prodRepo = new RemoteProductRepository(this, session.getApiUrl());
                        prodRepo.fetchAllProductsFromApi();
                        try {Thread.sleep(500);} catch (InterruptedException ignored) {}
                        List<Product> val = prodRepo.getAllProducts().getValue();
                        products = (val != null) ? val : new ArrayList<>();
                    } else {products = AppDatabase.getInstance(this).productDao().getAllSync();}

                    String[] names = new String[products.size()];
                    for (int i = 0; i < products.size(); i++) names[i] = products.get(i).name;

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

                        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                        dropdownProductList.setAdapter(orderAdapter);

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
                                boolean ok = repository.addProductToOrder(currentOrderId, finalProductId, finalQuantity);
                                runOnUiThread(() -> {
                                    if (!ok) Toast.makeText(this, "Nie udało się dodać produktu", Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(this, "Dodano produkt do listy", Toast.LENGTH_SHORT).show();
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
                new AlertDialog.Builder(this)
                        .setTitle("Usuwanie listy")
                        .setMessage("Czy chcesz usunąć listę z bazy?")
                        .setPositiveButton("Tak", (dialog, which) -> {
                            new Thread(() -> {
                                boolean ok;
                                if (isRemote) {
                                    ok = remoteRepo.deleteOrder(currentOrderId);
                                } else {
                                    Order local = db.orderDao().getById(currentOrderId);
                                    ok = (local != null);
                                    if (ok) db.orderDao().delete(local);
                                }
                                boolean finalOk = ok;

                                runOnUiThread(() -> {
                                    if (finalOk) {
                                        Toast.makeText(this, "Usunięto listę", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else Toast.makeText(this, "Błąd usuwania", Toast.LENGTH_SHORT).show();
                                });
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
        repository.getProductsForOrder(currentOrderId).observe(this, products -> {
            llProducts.removeAllViews();
            if (products == null || products.isEmpty()) {
                textEmptyProducts.setVisibility(View.VISIBLE);
            } else {
                textEmptyProducts.setVisibility(View.GONE);
                for (Product p : products) {
                    TextView tv = new TextView(this);
                    tv.setText("• " + p.quantity + "szt. " + p.name);
                    tv.setTextSize(16);
                    llProducts.addView(tv);
                }
            }
        });
    }
}
