package pl.lbasista.magazynex.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.ui.orders.OrdersFragment;
import pl.lbasista.magazynex.ui.addproduct.AddProductFragment;
import pl.lbasista.magazynex.ui.product.ProductListFragment;
import pl.lbasista.magazynex.ui.product.FavouriteFragment;

public class MainMenuActivity extends AppCompatActivity {
    private LinearLayout menuAdd, menuProducts, menuFav, menuOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Wczytaj menu główne
        setContentView(R.layout.activity_main_menu);

        //Znajdź widok
        menuAdd = findViewById(R.id.menuAdd);
        menuProducts = findViewById(R.id.menuProducts);
        menuFav = findViewById(R.id.menuFav);
        menuOrders = findViewById(R.id.menuOrders);

        //Domyślne okno
        loadFragment(new ProductListFragment());
        selectMenu(menuProducts);

        //Wybieranie okna
        menuProducts.setOnClickListener(v -> {
            loadFragment(new ProductListFragment());
            selectMenu(menuProducts);
        });
        menuAdd.setOnClickListener(v -> {
            loadFragment(new AddProductFragment());
            selectMenu(menuAdd);
        });
        menuFav.setOnClickListener(v -> {
            loadFragment(new FavouriteFragment());
            selectMenu(menuFav);
        });
        menuOrders.setOnClickListener(v -> {
            loadFragment(new OrdersFragment());
            selectMenu(menuOrders);
        });
    }

    private void selectMenu(LinearLayout selected) {
        //Usuń zaznaczenie tła
        menuAdd.setSelected(false);
        menuProducts.setSelected(false);
        menuFav.setSelected(false);
        menuOrders.setSelected(false);
        //Ustaw tło na aktywnym
        selected.setSelected(true);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }
}