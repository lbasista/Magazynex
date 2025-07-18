package pl.lbasista.magazynex.ui.product;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.OrderDao;
import pl.lbasista.magazynex.data.OrderProduct;
import pl.lbasista.magazynex.data.OrderProductDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;

public class ProductDetailsActivity extends AppCompatActivity {
    private TextView textProductName, textProducer, textBarcode, textQuantity, textCategory, textDescription, textOrderLists;
    private TextInputLayout orderListLayout, quantityLayout;
    private ImageView imageViewProduct;
    private ProductDao productDao;
    private OrderDao orderDao;
    private OrderProductDao orderProductDao;
    private ApplicationCategoryDao applicationCategoryDao;
    private MaterialToolbar toolbar;
    private int currentProductId = -1;
    private  Product currentProduct;
    private String name, producer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        //Inicjalizacja DAO
        productDao = AppDatabase.getInstance(this).productDao();
        orderDao = AppDatabase.getInstance(this).orderDao();
        orderProductDao = AppDatabase.getInstance(this).orderProductDao();
        applicationCategoryDao = AppDatabase.getInstance(this).applicationCategoryDao();

        //Inicjalizacja pól widoków
        textProductName = findViewById(R.id.textProductName);
        textProducer = findViewById(R.id.textProducer);
        textBarcode = findViewById(R.id.textBarcode);
        textQuantity = findViewById(R.id.textQuantity);
        textDescription = findViewById(R.id.textDescription);
        imageViewProduct = findViewById(R.id.imageProduct);
        textCategory = findViewById(R.id.textCategory);
        textOrderLists = findViewById(R.id.textOrderLists);
        toolbar = findViewById(R.id.appBarProductDetails);
        name = getIntent().getStringExtra("name");
        producer = getIntent().getStringExtra("producer");

        //Powrót do listy produktów
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.prodEdit) { //Edycja
                //Dane z Intent
                Bundle args = new Bundle();
                args.putString("barcode", getIntent().getStringExtra("barcode"));
                args.putString("name", getIntent().getStringExtra("name"));
                args.putString("producer", getIntent().getStringExtra("producer"));
                args.putInt("quantity", getIntent().getIntExtra("quantity", 0));
                args.putString("description", getIntent().getStringExtra("description"));
                args.putString("imageUri", getIntent().getStringExtra("imageUri"));

                EditProductBottomSheet sheet = new EditProductBottomSheet();
                sheet.setArguments(args);

                //Aktualizowanie obiektu po edycji
                sheet.setOnProductUpdatedListener(updatedProduct -> {
                    //Nowe wartości
                    textProductName.setText(updatedProduct.name);
                    textProducer.setText(updatedProduct.producer);
                    textBarcode.setText(updatedProduct.barcode);
                    textQuantity.setText(String.valueOf(updatedProduct.quantity));
                    textDescription.setText(updatedProduct.description);
                    String newImageUri = updatedProduct.imageUri;
                    if (newImageUri != null && !newImageUri.isEmpty()) {
                        imageViewProduct.setImageURI(Uri.parse(newImageUri));
                        imageViewProduct.setVisibility(View.VISIBLE);
                    } else {
                        imageViewProduct.setVisibility(View.GONE);
                    }
                });
                sheet.show(getSupportFragmentManager(), "editProduct");
                return true;
            } else if (id == R.id.prodAddList) { //Dodaj do listy
                new Thread(() -> {
                    List<Order> orders = orderDao.getAllOrders();

                    runOnUiThread(() -> {
                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_order, null);
                        AutoCompleteTextView dropdownOrderList = dialogView.findViewById(R.id.dropdownOrderList);
                        dropdownOrderList.setInputType(InputType.TYPE_NULL);
                        dropdownOrderList.setOnClickListener(v -> dropdownOrderList.showDropDown());
                        dropdownOrderList.setOnFocusChangeListener((v, hasFocus) -> {
                            if (hasFocus) dropdownOrderList.showDropDown();
                        });
                        TextInputEditText inputQuantity = dialogView.findViewById(R.id.inputQuantity);
                        dialogView.findViewById(R.id.dropdownProductList).setVisibility(View.GONE);

                        String[] orderNames = new String[orders.size()];
                        for (int i = 0; i < orders.size(); i++) {
                            orderNames[i] = orders.get(i).name;
                        }

                        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, orderNames);
                        dropdownOrderList.setAdapter(orderAdapter);

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        AlertDialog dialog = builder.setTitle("Dodaj do listy").setView(dialogView).create();
                        dialog.show();

                        Button buttonAdd = dialogView.findViewById(R.id.buttonSave);
                        buttonAdd.setOnClickListener(v -> {
                            try {
                                String selectedList = dropdownOrderList.getText().toString().trim();
                                String quantity = inputQuantity.getText().toString().trim();
                                orderListLayout = dialogView.findViewById(R.id.dropdownOrderListLayout);
                                quantityLayout = dialogView.findViewById(R.id.inputQuantityLayout);
                                Boolean hasError = false;

                                if (selectedList.isEmpty()) {
                                    orderListLayout.setError("Wybierz listę");
                                    hasError = true;
                                } else {
                                    orderListLayout.setError(null);
                                }
                                if (quantity.isEmpty()) {
                                    quantityLayout.setError("Podaj ilość");
                                    hasError = true;
                                } else {
                                    quantityLayout.setError(null);
                                }

                                int selectedOrderId = -1;
                                for (Order order : orders) {
                                    if (order.name.equals(selectedList)) {
                                        selectedOrderId = order.id;
                                        break;
                                    }
                                }
                                if (selectedOrderId == -1) {
                                    orderListLayout.setError("Nie znaleziono listy");
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

                                int finalOrderId = selectedOrderId;
                                int finalQuantity = selectedQuantity;
                                new Thread(() -> {
                                    OrderProduct newRelation = new OrderProduct(finalOrderId, currentProductId, finalQuantity);
                                    long result = orderProductDao.insert(newRelation);

                                    if (result != -1)
                                        orderDao.addProductToOrder(finalOrderId, finalQuantity);

                                    runOnUiThread(() -> {
                                        if (result == -1)
                                            Toast.makeText(this, "Produkt już jest na liście", Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(this, "Produkt dodano", Toast.LENGTH_SHORT).show();
                                            displayOrderLists(currentProductId);
                                            dialog.dismiss();
                                        }
                                    });
                                }).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Błąd: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
                        buttonCancel.setOnClickListener(v -> dialog.dismiss());
                    });
                }).start();
                return true;
            } else if (id == R.id.prodRemove) { //Usuń
                new AlertDialog.Builder(this)
                        .setTitle("Usuwanie produktu")
                        .setMessage("Czy chcesz usunąć ten produkt z bazy?")
                        .setPositiveButton("Tak", (dialog, which) -> {

                            new Thread(() -> {
                                Product toDelete = productDao.getByNameAndProducer(name, producer);
                                if (toDelete != null) {
                                    productDao.delete(toDelete);

                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Produkt usunięty", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }
                            }).start();
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
                return true;
            } else if (id == R.id.prodFav) { //Ulubione
                new Thread(() -> {
                    Product p = productDao.getByNameAndProducer(name, producer);
                    if (p != null){
                        p.favourite = !p.favourite; //Zmiana stanu polubienia
                        productDao.update(p); //Zapis do bazy

                        runOnUiThread(() -> {
                            int iconRes = p.favourite ? R.drawable.ic_star : R.drawable.ic_star_empty;
                            toolbar.getMenu().findItem(R.id.prodFav).setIcon(iconRes);
                            String msg = p.favourite ? "Dodano do ulubionych" : "Usunięto z ulubionych";
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
                return true;
            }
            return false;
        });
        loadFromIntent();

        //Kategoria i lista zamówień + gwiazdka
        new Thread(() -> {
            Product p = productDao.getByNameAndProducer(name, producer);
            if (p != null) {
                currentProductId = p.id;
                runOnUiThread(() -> {
                    displayCategory(p.applicationCategoryId);
                    displayOrderLists(currentProductId);
                    toolbar.getMenu().findItem(R.id.prodFav).setIcon(p.favourite ? R.drawable.ic_star : R.drawable.ic_star_empty);
                });
            }
        }).start();

        //Dadawanie do listy
        new Thread(() -> {
            Product product = productDao.getByNameAndProducer(name, producer);
            if (product != null) currentProductId = product.id;
        }).start();
    }

    private void displayOrderLists(int productId) {
        new Thread(() -> {
            List<OrderProduct> relations = orderProductDao.getByProductId(productId);

            List<String> orderInfo = new ArrayList<>();
            for (OrderProduct rel : relations) {
                Order o = orderDao.getById(rel.orderId);
                if (o != null) orderInfo.add("• " + o.name + " - " + rel.count + "szt.");
            }

            String displayText;
            if (orderInfo.isEmpty()) {
                displayText = ""; //Brak przypisania do list
            } else {
                displayText = "Na liście:\n" + TextUtils.join("\n", orderInfo);
            }
            runOnUiThread(() -> {
                textOrderLists.setVisibility(TextView.VISIBLE);
                textOrderLists.setText(displayText);
            });
        }).start();
    }

    private void displayCategory(int categoryId) {
        if (categoryId == 0) {
            textCategory.setText(""); //Brak kategorii
            return;
        }
        new Thread(() -> {
            ApplicationCategory cat = applicationCategoryDao.getById(categoryId);
            String categoryName = (cat != null ? cat.name : "");
            runOnUiThread(() -> textCategory.setText(categoryName));
        }).start();
    }

    private void loadFromIntent() {
        //Pobranie danych
        textProductName.setText(getIntent().getStringExtra("name"));
        textProducer.setText(getIntent().getStringExtra("producer"));
        textBarcode.setText(getIntent().getStringExtra("barcode"));
        textQuantity.setText(String.valueOf(getIntent().getIntExtra("quantity", 0)));
        textDescription.setText(getIntent().getStringExtra("description"));
        String uri = getIntent().getStringExtra("imageUri");
        if (uri != null && !uri.isEmpty()) {
            imageViewProduct.setImageURI(Uri.parse(uri));
            imageViewProduct.setVisibility(View.VISIBLE);
        } else {
            imageViewProduct.setVisibility(View.GONE);
        }
    }
}