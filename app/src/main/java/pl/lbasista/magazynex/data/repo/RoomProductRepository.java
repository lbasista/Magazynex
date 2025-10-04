package pl.lbasista.magazynex.data.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;
import pl.lbasista.magazynex.data.repo.RemoteProductRepository.ProductCallback;

public class RoomProductRepository implements ProductRepository {
    private final Context context;
    private final ProductDao productDao;

    public RoomProductRepository(Context context) {
        this.context = context;
        productDao = AppDatabase.getInstance(context).productDao();
    }

    @Override
    public boolean updateProduct(Product product) {
        AppDatabase.getInstance(context).productDao().update(product);
        return true;
    }

    @Override
    public long insertProduct(Product product) {
        return AppDatabase.getInstance(context).productDao().insert(product);
    }

    @Override
    public boolean deleteProduct(int id) {
        try {
            ProductDao dao = AppDatabase.getInstance(context).productDao();
            Product p = dao.getById(id);

            if (p == null) return false;
            dao.delete(p);
            return true;
        } catch (Exception e) {
            Log.e("RoomProductRepo", "deleteProduct error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public LiveData<List<Product>> getAllProducts() {return productDao.getAll();}

    @Override
    public LiveData<List<Product>> searchProducts(String query) {return productDao.searchProducts("%" + query + "%");}

    @Override
    public LiveData<List<Product>> getFavourites() {return productDao.getFavourites();}

    @Override
    public void toggleFavourite(Product product) {
        boolean newVal = !product.favourite;
        product.favourite = newVal;
        new Thread(() -> AppDatabase.getInstance(context).productDao().updateFavourite(product.id, newVal)).start();
    }

    @Override
    public void reloadFavourites(ProductCallback callback) {
        new Thread(() -> {
            List<Product> favourites = productDao.getFavouritesList();
            callback.onResult(favourites);
        }).start();
    }

    @Override
    public List<Product> getAllProductsSync() {return AppDatabase.getInstance(context).productDao().getAllSync();}

    @Override
    public void fetchAllProductsFromApi() {}
}
