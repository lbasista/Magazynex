package pl.lbasista.magazynex.ui.product;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.repo.CategoryRepository;
import pl.lbasista.magazynex.data.repo.ProductRepository;
import pl.lbasista.magazynex.data.repo.RemoteCategoryRepository;
import pl.lbasista.magazynex.data.repo.RemoteProductRepository;
import pl.lbasista.magazynex.data.repo.RoomCategoryRepository;
import pl.lbasista.magazynex.data.repo.RoomProductRepository;
import pl.lbasista.magazynex.ui.category.ManageCategoriesActivity;
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
    private RemoteProductRepository remoteRepo;
    private CategoryRepository categoryRepository;
    private Map<Integer, String> catNameById = new HashMap<>();

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

        SessionManager session = new SessionManager(requireContext());
        ProductRepository repository;
        if (session.isRemoteMode()) {
            remoteRepo = new RemoteProductRepository(requireContext(), session.getApiUrl());
            remoteRepo.loadFromApi();
            repository = remoteRepo;
            categoryRepository = new RemoteCategoryRepository(requireContext(), session.getApiUrl());
        } else {
            repository = new RoomProductRepository(requireContext());
            categoryRepository = new RoomCategoryRepository(requireContext());
        }
        Log.d("TEST", "Tryb: " + (session.isRemoteMode() ? "ZDALNY" : "LOKALNY"));

        ProductViewModelFactory factory = new ProductViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ProductViewModel.class);

        updateUI = products -> {
            currentList = products;
            applyCategoryNames(currentList);
            Log.d("DATA", "Liczba produktów w UI: " + (products != null ? products.size() : null));
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
                productAdapter = new ProductAdapter(currentList, viewModel, product -> viewModel.toggleFavourite(product));
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

        new Thread(() -> {
            List<ApplicationCategory> categories = categoryRepository.getAllCategories();
            Map<Integer, String> map = new HashMap<>();
            for (ApplicationCategory c : categories) map.put(c.id, c.name);
            requireActivity().runOnUiThread(() -> {
                catNameById.clear();
                catNameById.putAll(map);
                if (currentList != null) {
                    applyCategoryNames(currentList);
                    if (productAdapter != null) productAdapter.notifyDataSetChanged();
                }
            });
        }).start();
    }

    private void applyCategoryNames(List<Product> products) {
        if (products == null) return;
        for (Product p : products) {
            if (p == null) continue;
            String name = (p.applicationCategoryId != 0) ? catNameById.getOrDefault(p.applicationCategoryId, "") : "";
            p.applicationName = (name == null || name.isEmpty()) ? "" : name;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (remoteRepo != null) remoteRepo.fetchAllProductsFromApi();
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
                applyCategoryNames(currentList);
                currentList.sort(Comparator.comparing(p -> {
                    String n = (p.applicationName == null || p.applicationName.isEmpty()) ? "zzzzz" : p.applicationName.toLowerCase();
                    return n;
                }));
                productAdapter.notifyDataSetChanged();
                break;
            case "CATEGORY_DESC":
                applyCategoryNames(currentList);
                currentList.sort((p1, p2) -> {
                    String n1 = (p1.applicationName == null || p1.applicationName.isEmpty()) ? "zzzzz" : p1.applicationName.toLowerCase();
                    String n2 = (p2.applicationName == null || p2.applicationName.isEmpty()) ? "zzzzz" : p2.applicationName.toLowerCase();
                    return n2.compareTo(n1);
                });
                productAdapter.notifyDataSetChanged();
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