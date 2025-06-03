package pl.lbasista.magazynex.ui.product;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private TextView textProductName, textProducer, textBarcode, textQuantity, textCategory, textDescription, textAddToOrder, buttonDelete, textOrderLists;
    private ImageView imageViewProduct;
    private ProductDao productDao;
    private OrderDao orderDao;
    private OrderProductDao orderProductDao;
    private ApplicationCategoryDao applicationCategoryDao;
    private int currentProductId = -1;

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
        textAddToOrder = findViewById(R.id.tvAddToOrder);
        buttonDelete = findViewById(R.id.buttonDeleteProduct);
        textCategory = findViewById(R.id.textCategory);
        textOrderLists = findViewById(R.id.textOrderLists);

        //Powrót do listy produktów
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        //Edycja
        findViewById(R.id.tvEdit).setOnClickListener(v -> {
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
        });
        loadFromIntent();

        //Zmienne do operacji na produktach
        String name = getIntent().getStringExtra("name");
        String producer = getIntent().getStringExtra("producer");

        //Kategoria i lista zamówień
        new Thread(() -> {
            Product p = productDao.getByNameAndProducer(name, producer);
            if (p != null) {
                currentProductId = p.id;
                runOnUiThread(() -> {
                    displayCategory(p.applicationCategoryId);
                    displayOrderLists(currentProductId);
                });
            }
        }).start();

        //Usuwanie produktu
        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Usuwanie produktu")
                    .setMessage("Czy chcesz usunąć ten produkt z bazy?")
                    .setPositiveButton("Tak", (dialog, which) -> {
//                        String name = getIntent().getStringExtra("name");
//                        String producer = getIntent().getStringExtra("producer");

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
        });

        //Dadawanie do listy
        new Thread(() -> {
            Product product = productDao.getByNameAndProducer(name, producer);
            if (product != null) currentProductId = product.id;
        }).start();

        textAddToOrder.setOnClickListener(v -> {
            if (currentProductId == -1) return; //Brak wczytanego produktu

            new Thread(() -> {
                List<Order> orders = orderDao.getAllOrders();
                String[] names = new String[orders.size()];
                for (int i = 0; i < orders.size(); i++) {
                    names[i] = orders.get(i).name;
                }

                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Dodawanie do listy")
                            .setItems(names, (dialog, which) -> {
                                Order selectedOrder = orders.get(which);
                                new Thread(() -> {
                                    long result = orderProductDao.insert(new OrderProduct(selectedOrder.id, currentProductId));

                                    if (result != -1) orderDao.addProductToOrder(selectedOrder.id);

                                    runOnUiThread(() -> {
                                        if (result == -1) {
                                            Toast.makeText(this, "Produkt już jest na tej liście", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Dodano do listy " + selectedOrder.name, Toast.LENGTH_SHORT).show();
                                            displayOrderLists(currentProductId);
                                        }
                                    });
                                }).start();
                            })
                            .setNegativeButton("Anuluj", null)
                            .show();
                });
            }).start();
        });
    }

    private void displayOrderLists(int productId) {
        new Thread(() -> {
            List<OrderProduct> relations = orderProductDao.getByProductId(productId);

            List<String> orderNames = new ArrayList<>();
            for (OrderProduct rel : relations) {
                Order o = orderDao.getById(rel.orderId);
                if (o != null) orderNames.add(o.name);
            }

            String displayText;
            if (orderNames.isEmpty()) {
                displayText = ""; //Brak przypisania do list
            } else {
                displayText = "Na liście: " + TextUtils.join(", ", orderNames);
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