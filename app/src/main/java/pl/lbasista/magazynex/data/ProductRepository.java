package pl.lbasista.magazynex.data;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface ProductRepository {
    LiveData<List<Product>> getAllProducts();
    LiveData<List<Product>> searchProducts(String query);
    LiveData<List<Product>> getFavourites();
    void toggleFavourite(Product product);
    void reloadFavourites(RemoteProductRepository.ProductCallback callback);
}
