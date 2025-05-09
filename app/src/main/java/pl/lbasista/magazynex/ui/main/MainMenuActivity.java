package pl.lbasista.magazynex.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.ui.about.AboutFragment;
import pl.lbasista.magazynex.ui.addproduct.AddProductFragment;
import pl.lbasista.magazynex.ui.product.ProductListFragment;
import pl.lbasista.magazynex.ui.product.FavouriteFragment;

public class MainMenuActivity extends AppCompatActivity {
    private LinearLayout menuAdd, menuProducts, menuFav, menuAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Wczytaj menu główne
        setContentView(R.layout.activity_main_menu);

        //Znajdź widok
        menuAdd = findViewById(R.id.menuAdd);
        menuProducts = findViewById(R.id.menuProducts);
        menuAbout = findViewById(R.id.menuAbout);
        menuFav = findViewById(R.id.menuFav);

        //Domyślne okno
        loadFragment(new ProductListFragment());

        //Wybieranie okna
        menuProducts.setOnClickListener(v -> loadFragment(new ProductListFragment()));
        menuAdd.setOnClickListener(v -> loadFragment(new AddProductFragment()));
        menuFav.setOnClickListener(v -> loadFragment(new FavouriteFragment()));
        menuAbout.setOnClickListener(v -> loadFragment(new AboutFragment()));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }
}