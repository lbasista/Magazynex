package pl.lbasista.magazynex.ui.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Product;

public class ProductViewModel extends AndroidViewModel {

    private LiveData<List<Product>> productList;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        productList = AppDatabase.getInstance(application).productDao().getAll();
    }

    public LiveData<List<Product>> getProductList() {
        return productList;
    }
}