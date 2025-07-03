package pl.lbasista.magazynex.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.ui.orders.OrdersFragment;
import pl.lbasista.magazynex.ui.addproduct.AddProductFragment;
import pl.lbasista.magazynex.ui.product.ProductListFragment;
import pl.lbasista.magazynex.ui.product.FavouriteFragment;

public class MainMenuActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Wczytaj menu główne
        setContentView(R.layout.activity_main_menu);

        //Znajdź widok
        bottomNavigation = findViewById(R.id.bottom_navigation);

        //Domyślna zakładka
        bottomNavigation.setSelectedItemId(R.id.nav_products);
        loadFragment(new ProductListFragment());

        //Wybieranie okna
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_add:
                    loadFragment(new AddProductFragment());
                    return true;
                case R.id.nav_products:
                    loadFragment(new ProductListFragment());
                    return true;
                case R.id.nav_fav:
                    loadFragment(new FavouriteFragment());
                    return true;
                case R.id.nav_orders:
                    loadFragment(new OrdersFragment());
                    return true;
                default:
                    return false;
            }
        });
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }
}