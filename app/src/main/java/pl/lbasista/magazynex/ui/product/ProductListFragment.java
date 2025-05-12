package pl.lbasista.magazynex.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.Product;

public class ProductListFragment extends Fragment {
    private RecyclerView recyclerViewProducts;
    private EditText editTextSearch;
    private Button buttonSearch;
    private TextView textViewEmpty;
    private ProductAdapter productAdapter;
    private ProductViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        //Pionowa lista
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(requireContext()));

        editTextSearch = view.findViewById(R.id.editTextProductSearch);
        buttonSearch = view.findViewById(R.id.buttonProductSearch);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);

        ProductViewModel viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        Observer<List<Product>> updateUI = products -> {
            if (products == null || products.isEmpty()) {
                //Brak wyników
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerViewProducts.setVisibility(View.GONE);
            } else {
                //Są wyniki
                textViewEmpty.setVisibility(View.GONE);
                recyclerViewProducts.setVisibility(View.VISIBLE);
                productAdapter = new ProductAdapter(products, viewModel);
                recyclerViewProducts.setAdapter(productAdapter);
            }
        };

        //Wyświetlanie wszystkich produktów
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), updateUI);

        //Wyszukiwanie produktu
        buttonSearch.setOnClickListener(v -> {
            String query = editTextSearch.getText().toString().trim();
            if (query.isEmpty()) {
                //Pusto - wyświetl pełną listę
                viewModel.getAllProducts().observe(getViewLifecycleOwner(), updateUI);
            } else {
                //Szukamy przez DAO
                viewModel.searchProducts(query).observe(getViewLifecycleOwner(), updateUI);
            }
        });
    }
}