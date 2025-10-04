package pl.lbasista.magazynex.ui.product;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.data.OrderDao;
import pl.lbasista.magazynex.data.OrderProductDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;
import pl.lbasista.magazynex.data.repo.CategoryRepository;
import pl.lbasista.magazynex.data.repo.OrderRepository;
import pl.lbasista.magazynex.data.repo.ProductRepository;
import pl.lbasista.magazynex.data.repo.RemoteCategoryRepository;
import pl.lbasista.magazynex.data.repo.RemoteOrderRepository;
import pl.lbasista.magazynex.data.repo.RemoteProductRepository;
import pl.lbasista.magazynex.data.repo.RoomCategoryRepository;
import pl.lbasista.magazynex.data.repo.RoomOrderRepository;
import pl.lbasista.magazynex.data.repo.RoomProductRepository;
import pl.lbasista.magazynex.ui.user.RoleChecker;
import pl.lbasista.magazynex.ui.user.SessionManager;

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
    private String name, producer;
    private ProductRepository repository;
    private RemoteProductRepository remoteRepo;
    private RoomProductRepository roomRepo;
    private OrderRepository orderRepository;
    private CategoryRepository categoryRepository;
    private Map<Integer, String> catNameCache = new HashMap<>();

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
        currentProductId = getIntent().getIntExtra("productId", -1);
        name = getIntent().getStringExtra("name");
        producer = getIntent().getStringExtra("producer");

        if (RoleChecker.isViewer(new SessionManager(this))) {
            toolbar.getMenu().findItem(R.id.prodEdit).setVisible(false);
            toolbar.getMenu().findItem(R.id.prodAddList).setVisible(false);
            toolbar.getMenu().findItem(R.id.prodFav).setVisible(false);
            toolbar.getMenu().findItem(R.id.prodRemove).setVisible(false);
        }

        //Powrót do listy produktów
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        //Wybór repo
        SessionManager session = new SessionManager(this);
        if (session.isRemoteMode()) {
            //Produkty
            remoteRepo = new RemoteProductRepository(this, session.getApiUrl());
            remoteRepo.fetchAllProductsFromApi();
            repository = remoteRepo;
            //Listy
            orderRepository = new RemoteOrderRepository(this, session.getApiUrl());
            categoryRepository = new RemoteCategoryRepository(this, session.getApiUrl());
        } else {
            //Produkty
            roomRepo = new RoomProductRepository(this);
            repository = roomRepo;
            //Listy
            orderRepository = new RoomOrderRepository(this);
            categoryRepository = new RoomCategoryRepository(this);
        }

        if (remoteRepo != null) {
            remoteRepo.getAllProducts().observe(this, products -> {
                setIconState();

                int categoryId = 0;
                if (products != null) {
                    for (Product p : products) {
                        boolean sameId = (p.id == currentProductId);
                        boolean sameKeys = (p.name.equals(name) && p.producer.equals(producer));
                        if (sameId || sameKeys) {
                            categoryId = p.applicationCategoryId;
                            break;
                        }
                    }
                }
                displayCategory(categoryId);
            });
        }

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

                android.util.Log.d("ProductDetails", "Open Edit sheet with args: "
                        + "barcode=" + getIntent().getStringExtra("barcode")
                        + ", name=" + getIntent().getStringExtra("name")
                        + ", producer=" + getIntent().getStringExtra("name")
                        + ", quantity=" + getIntent().getIntExtra("quantity", 0)
                        + ", description=" + getIntent().getStringExtra("description")
                        + ", imageUri=" + getIntent().getStringExtra("description"));

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
                    displayCategory(updatedProduct.applicationCategoryId);
                    displayOrderLists(updatedProduct.id);
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
                    List<Order> orders = orderRepository.getAllOrders();

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
                                    boolean ok = orderRepository.addProductToOrder(finalOrderId, currentProductId, finalQuantity);
                                    runOnUiThread(() -> {
                                        if (!ok) Toast.makeText(this, "Nie udało się dodać produktu", Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(this, "Produkt dodany do listy", Toast.LENGTH_SHORT).show();
                                            displayOrderLists(currentProductId);
                                            setResult(RESULT_OK);
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
                            final int toDelete;
                            if (getIntent() != null && getIntent().hasExtra("productId")) toDelete = getIntent().getIntExtra("productId", 0);
                            else {
                                AppDatabase db = AppDatabase.getInstance(this);
                                Product found = db.productDao().getByNameAndProducer(name, producer);
                                toDelete = (found != null) ? found.id : 0;
                            }

                            if (toDelete <= 0) {
                                Toast.makeText(this, "Błąd ustalenia ID produktu", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            new Thread(() -> {
                                boolean ok = repository.deleteProduct(toDelete);
                                runOnUiThread(() -> {
                                    if (ok) {
                                        Toast.makeText(this, "Produkt usunięty", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK, new Intent().putExtra("deletedProductId", toDelete));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Nie udało się usunąć produktu", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_CANCELED);
                                    }
                                });
                            }).start();
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
                return true;
            } else if (id == R.id.prodFav) { //Ulubione
                if (remoteRepo != null) { //Baza zdalna
                    Product p = null;
                    String msg = null;
                    List<Product> productList = remoteRepo.getAllProducts().getValue();
                    if (productList != null) {
                        for (Product prod : productList) {
                            if (prod.name.equals(name) && prod.producer.equals(producer)) {
                                p = prod;
                                break;
                            }
                        }
                    }
                    if (p == null) {
                        msg = "Nie znaleziono produktu w bazie";
                        return true;
                    } else {
                        remoteRepo.toggleFavourite(p);
                        remoteRepo.fetchAllProductsFromApi();
                        remoteRepo.fetchFavouritesFromApi();
                        msg = !p.favourite ? "Dodano do ulubionych" : "Usunięto z ulubionych";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else { //Room
                    new Thread(() -> {
                        Product p = productDao.getByNameAndProducer(name, producer);
                        if (p == null) return;
                        p.favourite = !p.favourite;
                        productDao.update(p);
                        runOnUiThread(() -> {
                            setIconState();
                            Toast.makeText(this, p.favourite ? "Dodano do ulubionych" : "Usunięto z ulubionych", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                }
                return true;
            }
            return false;
        });
        loadFromIntent();

        //Kategoria i lista zamówień + gwiazdka
        if (currentProductId != -1) {
            if (remoteRepo != null) {
                int categoryId = 0;
                List<Product> list = remoteRepo.getAllProducts().getValue();
                if (list != null) {
                    for (Product p : list) {
                        boolean sameId = (p.id == currentProductId);
                        boolean sameKeys = (p.name.equals(name) && p.producer.equals(producer));
                        if (sameId || sameKeys) {
                            categoryId = p.applicationCategoryId;
                            break;
                        }
                    }
                }
                displayCategory(categoryId);
                displayOrderLists(currentProductId);
                setIconState();
            } else {
                new Thread(() -> {
                    Product p = productDao.getById(currentProductId);
                    runOnUiThread(() -> {
                        int catId = (p != null ? p.applicationCategoryId : 0);
                        displayCategory(catId);
                        displayOrderLists(currentProductId);
                        setIconState();
                    });
                }).start();
            }
        }
        android.util.Log.d("ProductDetails", "currentProductId z Intenta = " + currentProductId);
    }

    private void displayOrderLists(int productId) {
        Log.d("ProductDetails", "Wywołano displayOrderLists dla productId=" + productId);
        orderRepository.getOrdersForProduct(productId).observe(this, orders -> {
            Log.d("ProductDetails", "getOrdersForProduct zwrócił " + (orders == null ? "null" : orders.size()+" elementów"));
            if (orders == null || orders.isEmpty()) {
                textOrderLists.setVisibility(TextView.VISIBLE);
                textOrderLists.setText("");
                return;
            }

            List<String> lines = new ArrayList<>();
            for (Order o : orders) lines.add("• " + o.name + " - " + o.quantity + "szt.");
            String displayText = "Na liście:\n" + TextUtils.join("\n", lines);
            textOrderLists.setVisibility(TextView.VISIBLE);
            textOrderLists.setText(displayText);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setIconState();
    }

    private void displayCategory(int categoryId) {
        if (categoryId == 0) {
            textCategory.setText(""); //Brak kategorii
            return;
        }
        new Thread(() -> {
            List<ApplicationCategory> all = categoryRepository.getAllCategories();
            for (ApplicationCategory c : all) catNameCache.put(c.id, c.name);
            final String name = catNameCache.getOrDefault(categoryId, "");
            runOnUiThread(() -> textCategory.setText(name));
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
            try {
                imageViewProduct.setImageURI(Uri.parse(uri));
                imageViewProduct.setVisibility(View.VISIBLE);
            } catch (SecurityException e) {
                e.printStackTrace();
                imageViewProduct.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(imageViewProduct, "Brak dostępu do zdjęcia!\nWybierz grafikę ponownie.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Edytuj", v -> toolbar.getMenu().performIdentifierAction(R.id.prodEdit, 0));
                snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary)).show();
            }
        } else {
            imageViewProduct.setVisibility(View.GONE);
        }
    }

    private void setIconState() {
        if (remoteRepo != null) { //Baza zdalna
            Product p = null;
            List<Product> productList = remoteRepo.getAllProducts().getValue();
            if (productList != null) {
                for (Product prod : productList) {
                    if (prod.name.equals(name) && prod.producer.equals(producer)) {
                        p = prod;
                        break;
                    }
                }
            }
            final boolean isFavourite = p != null && p.favourite;
            toolbar.getMenu().findItem(R.id.prodFav).setIcon(isFavourite ? R.drawable.ic_star : R.drawable.ic_star_empty);
        } else { //Room
            new Thread(() -> {
                Product p = productDao.getByNameAndProducer(name, producer);
                final boolean isFavourite = p != null && p.favourite;
                runOnUiThread(() -> {
                    toolbar.getMenu().findItem(R.id.prodFav).setIcon(isFavourite ? R.drawable.ic_star : R.drawable.ic_star_empty);;
                });
            }).start();
        }
    }
}