package pl.lbasista.magazynex.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Comparator;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.Product;

public class FavouriteFragment extends Fragment implements SortDialogFragment.SortDialogListener {
    private RecyclerView recyclerViewProducts;
    ProductAdapter productAdapter;
    private FloatingActionButton buttonSort;
    private List<Product> favouriteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_favourite_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        //Pionowa lista
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        //Sortowanie
        buttonSort = view.findViewById(R.id.buttonSort);
        view.post(() -> { //Pozycja przycisku nad menu
            View menuBar = requireActivity().findViewById(R.id.bottom_navigation);
            if (menuBar != null) {
                int menuHeight = menuBar.getHeight();
                int extraSpacing = (int) getResources().getDisplayMetrics().density * 16;
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) buttonSort.getLayoutParams();
                lp.bottomMargin = menuHeight + extraSpacing;
                buttonSort.setLayoutParams(lp);
            }
        });
        buttonSort.setOnClickListener(v -> new SortDialogFragment().show(getChildFragmentManager(), "SortDialog"));


        ProductViewModel viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        viewModel.getFavourites().observe(getViewLifecycleOwner(), products -> {
            favouriteList = products;
            productAdapter = new ProductAdapter(products, viewModel);
            recyclerViewProducts.setAdapter(productAdapter);
        });
    }

    @Override
    public void onSortSelected(String sortType) {
        if (favouriteList == null) return;

        switch (sortType) {
            case "BARCODE_ASC":
                favouriteList.sort(Comparator.comparingLong(p -> {
                    try {
                        return Long.parseLong(p.barcode);
                    } catch (Exception e) {
                        return Long.MAX_VALUE; //Brak kodu = na końcu listy
                    }
                }));
                break;
            case "BARCODE_DESC":
                favouriteList.sort((p1, p2) -> {
                    try {
                        long b1 = Long.parseLong(p1.barcode);
                        long b2 = Long.parseLong(p2.barcode);
                        return Long.compare(b2, b1);
                    } catch (Exception e) {
                        return 0; //Nieporównywalne = bez zmian
                    }
                });
                break;
            case "NAME_ASC":
                favouriteList.sort(Comparator.comparing(p -> p.name.toLowerCase()));
                break;
            case "NAME_DESC":
                favouriteList.sort((p1, p2) -> p2.name.compareToIgnoreCase(p1.name));
                break;
            case "PRODUCER_ASC":
                favouriteList.sort(Comparator.comparing(p -> p.producer.toLowerCase()));
                break;
            case "PRODUCER_DESC":
                favouriteList.sort((p1, p2) -> p2.producer.compareToIgnoreCase(p1.producer));
                break;
            case "QUANTITY_ASC":
                favouriteList.sort(Comparator.comparingInt(p -> p.quantity));
                break;
            case "QUANTITY_DESC":
                favouriteList.sort((p1, p2) -> Integer.compare(p2.quantity, p1.quantity));
                break;
        }
        productAdapter.notifyDataSetChanged();
    }
}