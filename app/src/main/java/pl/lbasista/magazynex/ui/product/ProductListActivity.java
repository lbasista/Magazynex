package pl.lbasista.magazynex.ui.product;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.Product;

public class ProductListActivity extends AppCompatActivity {

    RecyclerView recyclerViewProducts;
    ProductAdapter productAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        ProductViewModel viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        viewModel.getProductList().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                productAdapter = new ProductAdapter(products);
                recyclerViewProducts.setAdapter(productAdapter);
            }
        });
    }
}