package pl.lbasista.magazynex.data.repo;

import androidx.lifecycle.LiveData;

import java.util.List;

import pl.lbasista.magazynex.data.Product;

public interface ProductRepository {
    LiveData<List<Product>> getAllProducts();
    LiveData<List<Product>> searchProducts(String query);
    LiveData<List<Product>> getFavourites();
    void toggleFavourite(Product product);
    void reloadFavourites(RemoteProductRepository.ProductCallback callback);
    List<Product> getAllProductsSync();
    void fetchAllProductsFromApi();
}
