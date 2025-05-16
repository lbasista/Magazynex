package pl.lbasista.magazynex.ui.product;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.lbasista.magazynex.R;

public class ProductDetailsActivity extends AppCompatActivity {
    private TextView textProductName, textProducer, textBarcode, textQuantity, textDescription;
    private ImageView imageViewProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        //Inicjalizacja pól widoków
        textProductName = findViewById(R.id.textProductName);
        textProducer = findViewById(R.id.textProducer);
        textBarcode = findViewById(R.id.textBarcode);
        textQuantity = findViewById(R.id.textQuantity);
        textDescription = findViewById(R.id.textDescription);
        imageViewProduct = findViewById(R.id.imageProduct);

        //Powrót do listy produktów
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        //Edycja
        findViewById(R.id.tvEdit).setOnClickListener(v -> {
            //Dane z Intent
            Bundle args = new Bundle();
            args.putString("barcode", getIntent().getStringExtra("barcode"));
            args.putString("name", getIntent().getStringExtra("name"));
            args.putString("producer", getIntent().getStringExtra("producer"));
            args.putInt("quantity", getIntent().getIntExtra("quantity", 0));
            args.putString("description", getIntent().getStringExtra("description"));
            args.putString("imageUri", getIntent().getStringExtra("imageUri"));

            EditProductBottomSheet sheet = new EditProductBottomSheet();
            sheet.setArguments(args);

            //Aktualizowanie obiektu po edycji
            sheet.setOnProductUpdatedListener(updatedProduct -> {
                //Nowe wartości
                textProductName.setText(updatedProduct.name);
                textProducer.setText(updatedProduct.producer);
                textBarcode.setText(updatedProduct.barcode);
                textQuantity.setText(String.valueOf(updatedProduct.quantity));
                textDescription.setText(updatedProduct.description);
                if (!updatedProduct.imageUri.isEmpty()) {
                    imageViewProduct.setImageURI(Uri.parse(updatedProduct.imageUri));
                    imageViewProduct.setVisibility(View.VISIBLE);
                } else {
                    imageViewProduct.setVisibility(View.GONE);
                }
            });
            sheet.show(getSupportFragmentManager(), "editProduct");
        });
        loadFromIntent();
    }

    private void loadFromIntent() {
        //Pobranie danych
        textProductName.setText(getIntent().getStringExtra("name"));
        textProducer.setText(getIntent().getStringExtra("producer"));
        textBarcode.setText(getIntent().getStringExtra("barcode"));
        textQuantity.setText(String.valueOf(getIntent().getIntExtra("quantity", 0)));
        textDescription.setText(getIntent().getStringExtra("description"));
        String uri = getIntent().getStringExtra("imageUri");
        if (uri != null && !uri.isEmpty()) {
            imageViewProduct.setImageURI(Uri.parse(uri));
            imageViewProduct.setVisibility(View.VISIBLE);
        } else {
            imageViewProduct.setVisibility(View.GONE);
        }
    }
}