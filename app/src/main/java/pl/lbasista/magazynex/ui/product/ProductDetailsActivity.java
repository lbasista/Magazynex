package pl.lbasista.magazynex.ui.product;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.lbasista.magazynex.R;

public class ProductDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        //Powrót do listy produktów
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        //Edycja
        findViewById(R.id.tvEdit).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("barcode", getIntent().getStringExtra("barcode"));
            args.putString("name", getIntent().getStringExtra("name"));
            args.putString("producer", getIntent().getStringExtra("producer"));
            args.putInt("quantity", getIntent().getIntExtra("quantity", 0));
            args.putString("description", getIntent().getStringExtra("description"));
            args.putString("imageUri", getIntent().getStringExtra("imageUri"));

            EditProductBottomSheet sheet = new EditProductBottomSheet();
            sheet.setArguments(args);
            sheet.show(getSupportFragmentManager(), "editProduct");
        });


        //Pobranie danych
        String name = getIntent().getStringExtra("name");
        String producer = getIntent().getStringExtra("producer");
        String barcode = getIntent().getStringExtra("barcode");
        int quantity = getIntent().getIntExtra("quantity", 0);
        String description = getIntent().getStringExtra("description");
        String imageUri = getIntent().getStringExtra("imageUri");

        //Ustawienie danych
        ((TextView)findViewById(R.id.textProductName)).setText(name);
        ((TextView)findViewById(R.id.textProducer)).setText(producer);
        ((TextView)findViewById(R.id.textBarcode)).setText(barcode);
        ((TextView)findViewById(R.id.textQuantity)).setText(String.valueOf(quantity));
        ((TextView)findViewById(R.id.textDescription)).setText(description);

        ImageView imageView = findViewById(R.id.imageProduct);
        if (imageView != null && !imageUri.isEmpty()) {
            imageView.setImageURI(Uri.parse(imageUri));
        } else {
            imageView.setVisibility(TextView.GONE);
        }
    }
}
