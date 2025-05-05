package pl.lbasista.magazynex.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.ui.about.AboutActivity;
import pl.lbasista.magazynex.ui.addproduct.AddProductActivity;
import pl.lbasista.magazynex.ui.product.ProductListActivity;

public class MainMenuActivity extends AppCompatActivity {
    Button buttonProductList, buttonAddProduct, buttonAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        buttonProductList = findViewById(R.id.buttonProductList);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonAbout = findViewById(R.id.buttonAbout);

        buttonProductList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

    }
}
