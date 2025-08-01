package pl.lbasista.magazynex.ui.product;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Comparator;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.ui.category.ManageCategoriesActivity;
import pl.lbasista.magazynex.ui.user.RoleChecker;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class ProductListFragment extends Fragment implements SortDialogFragment.SortDialogListener {
    private RecyclerView recyclerViewProducts;
    private EditText editTextSearch;
    private Button buttonSearch;
    private TextView textViewEmpty;
    private ProductAdapter productAdapter;
    private FloatingActionButton fabMain;
    private ExtendedFloatingActionButton fabSort, fabCategory;
    private boolean isFabMenuOpen = false;
    private List<Product> currentList; //Aktualnie wyświetlana lista
    private ProductViewModel viewModel;
    private Observer<List<Product>> updateUI;

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

        editTextSearch = view.findViewById(R.id.textInputSearch);
        buttonSearch = view.findViewById(R.id.searchProduct);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);

        fabMain = view.findViewById(R.id.fabMain);
        fabSort = view.findViewById(R.id.fabSort);
        fabCategory = view.findViewById(R.id.fabCategory);

        //Pozycja przycisku nad menu
        view.post(() -> {
            View menuBar = requireActivity().findViewById(R.id.bottom_navigation);
            if (menuBar != null) {
                int menuHeight = menuBar.getHeight();
                int extraSpacing = (int) getResources().getDisplayMetrics().density * 16;
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) fabMain.getLayoutParams();
                lp.bottomMargin = menuHeight + extraSpacing;
                fabMain.setLayoutParams(lp);
            }
        });

        fabMain.setOnClickListener(v -> {
            Log.d("FAB", "Kliknięto fabMain");
            toggleFabMenu();
        });
        fabSort.setOnClickListener(v -> {
            toggleFabMenu();
            new SortDialogFragment().show(getChildFragmentManager(), "SortDialog");
        });
        fabCategory.setOnClickListener(v -> {
            toggleFabMenu();
            Intent intent = new Intent(requireContext(), ManageCategoriesActivity.class);
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        updateUI = products -> {
            currentList = products;
            if (products == null || products.isEmpty()) {
                //Brak wyników
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerViewProducts.setVisibility(View.GONE);
                fabMain.setVisibility(View.GONE);
            } else {
                //Są wyniki
                textViewEmpty.setVisibility(View.GONE);
                recyclerViewProducts.setVisibility(View.VISIBLE);
                fabMain.setVisibility(View.VISIBLE);
                productAdapter = new ProductAdapter(currentList, viewModel);
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

    //Sortowanie
    @Override
    public void onSortSelected(String sortType) {
        if (currentList == null) return;

        switch (sortType) {
            case "BARCODE_ASC":
                currentList.sort(Comparator.comparingLong(p -> {
                    try {
                        return Long.parseLong(p.barcode);
                    } catch (Exception e) {
                        return Long.MAX_VALUE; //Brak kodu = na końcu listy
                    }
                }));
                break;
            case "BARCODE_DESC":
                currentList.sort((p1, p2) -> {
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
                currentList.sort(Comparator.comparing(p -> p.name.toLowerCase()));
                break;
            case "NAME_DESC":
                currentList.sort((p1, p2) -> p2.name.compareToIgnoreCase(p1.name));
                break;
            case "PRODUCER_ASC":
                currentList.sort(Comparator.comparing(p -> p.producer.toLowerCase()));
                break;
            case "PRODUCER_DESC":
                currentList.sort((p1, p2) -> p2.producer.compareToIgnoreCase(p1.producer));
                break;
            case "QUANTITY_ASC":
                currentList.sort(Comparator.comparingInt(p -> p.quantity));
                break;
            case "QUANTITY_DESC":
                currentList.sort((p1, p2) -> Integer.compare(p2.quantity, p1.quantity));
                break;
            case "CATEGORY_ASC":
                new Thread(() -> {
                    ApplicationCategoryDao dao = AppDatabase.getInstance(requireContext()).applicationCategoryDao();

                    for (Product p : currentList) {
                        if (p.applicationCategoryId != 0) {
                            ApplicationCategory cat = dao.getById(p.applicationCategoryId);
                            p.applicationName = cat != null ? cat.name.toLowerCase() : "zzzzz";
                        } else {
                            p.applicationName = "zzzzz";
                        }
                    }

                    currentList.sort(Comparator.comparing(p -> p.applicationName));
                    requireActivity().runOnUiThread(() -> productAdapter.notifyDataSetChanged());
                }).start();
                break;
            case "CATEGORY_DESC":
                new Thread(() -> {
                    ApplicationCategoryDao dao = AppDatabase.getInstance(requireContext()).applicationCategoryDao();

                    for (Product p : currentList) {
                        if (p.applicationCategoryId != 0) {
                            ApplicationCategory cat = dao.getById(p.applicationCategoryId);
                            p.applicationName = cat != null ? cat.name.toLowerCase() : "zzzzz";
                        } else {
                            p.applicationName = "zzzzz";
                        }
                    }

                    currentList.sort((p1, p2) -> p2.applicationName.compareTo(p1.applicationName));
                    requireActivity().runOnUiThread(() -> productAdapter.notifyDataSetChanged());
                }).start();
                break;
        }
        productAdapter.notifyDataSetChanged();
    }

    private void toggleFabMenu() {
        if (isFabMenuOpen) {
            fabSort.hide();
            fabCategory.hide();
        } else {
            fabSort.show();
            fabCategory.show();
        }
        isFabMenuOpen = !isFabMenuOpen;
    }
}