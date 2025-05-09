package pl.lbasista.magazynex.ui.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;

public class ProductViewModel extends AndroidViewModel {
    private final ProductDao productDao;
    private final LiveData<List<Product>> productList; //Wszystko
    private final LiveData<List<Product>> favouriteList; //Ulubione

    //Operacje w tle
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        productDao = db.productDao();

        productList = productDao.getAll();
        favouriteList = productDao.getFavourites();
    }

    public LiveData<List<Product>> getAllProducts() {
        return productList;
    }
    public LiveData<List<Product>> getFavourites() { return favouriteList; }

    public void updateFavourite(int id, boolean fav) {
        executor.execute(() -> productDao.updateFavourite(id, fav));
    }

    public void toggleFavourite(Product product) {
        executor.execute(() -> {
            boolean newVal = !product.favourite;
            productDao.updateFavourite(product.id, newVal);
        });
    }
}