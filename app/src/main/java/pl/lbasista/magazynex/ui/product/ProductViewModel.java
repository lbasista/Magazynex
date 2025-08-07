package pl.lbasista.magazynex.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.function.Consumer;

import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductRepository;

public class ProductViewModel extends ViewModel {
    private final ProductRepository repository;

    public ProductViewModel(ProductRepository repository) {this.repository = repository;}
    public LiveData<List<Product>> getAllProducts() {return repository.getAllProducts();}
    public LiveData<List<Product>> searchProducts(String query) {return repository.searchProducts(query);}
    public LiveData<List<Product>> getFavourites() {return repository.getFavourites();}
    public void toggleFavourite(Product product) {repository.toggleFavourite(product);}
    public void refreshFavourites(Consumer<List<Product>> callback) {repository.reloadFavourites(callback::accept);}
}