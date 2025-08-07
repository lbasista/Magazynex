package pl.lbasista.magazynex.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.ui.orders.OrdersFragment;
import pl.lbasista.magazynex.ui.addproduct.AddProductFragment;
import pl.lbasista.magazynex.ui.product.ProductListFragment;
import pl.lbasista.magazynex.ui.product.FavouriteFragment;
import pl.lbasista.magazynex.ui.user.LoginActivity;
import pl.lbasista.magazynex.ui.user.ProfileFragment;
import pl.lbasista.magazynex.ui.user.RoleChecker;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class MainMenuActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager session = new SessionManager(this);
        //Brak użytkownika w bazie
        if (session.getUserId() == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (session.isRemoteMode()) {
            String apiUrl = getIntent().getStringExtra("api_url");
            if (apiUrl != null) {
                session.setApiUrl(apiUrl);
                Log.d("API", "Tryb zdalny – zapisuję adres: " + apiUrl);
            } else Log.e("API", "Brak adresu API");
        }

        super.onCreate(savedInstanceState);
        //Wczytaj menu główne
        setContentView(R.layout.activity_main_menu);

        //Znajdź widok
        bottomNavigation = findViewById(R.id.bottom_navigation);

        //Domyślna zakładka
        bottomNavigation.setSelectedItemId(R.id.nav_products);
        loadFragment(new ProductListFragment());

        //Blokowanie okien
        if (RoleChecker.isViewer(session)) bottomNavigation.getMenu().removeItem(R.id.nav_add);

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
                case R.id.nav_user:
                    loadFragment(new ProfileFragment());
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

    public void refreshCurrentFragment() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (current instanceof ProductListFragment) loadFragment(new ProductListFragment());
        else if (current instanceof FavouriteFragment) loadFragment(new FavouriteFragment());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) refreshCurrentFragment();
    }
}