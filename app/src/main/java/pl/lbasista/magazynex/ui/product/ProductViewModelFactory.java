package pl.lbasista.magazynex.ui.product;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import pl.lbasista.magazynex.data.ProductRepository;

public class ProductViewModelFactory implements ViewModelProvider.Factory {
    private final ProductRepository repository;

    public ProductViewModelFactory(ProductRepository repository) {this.repository = repository;}

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProductViewModel.class)) return (T) new ProductViewModel(repository);
    throw new IllegalArgumentException("Nieobsługiwany typ: " + modelClass);
    }
}
