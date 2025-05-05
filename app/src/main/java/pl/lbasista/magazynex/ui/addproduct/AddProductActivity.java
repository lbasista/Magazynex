package pl.lbasista.magazynex.ui.addproduct;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Product;

public class AddProductActivity extends AppCompatActivity {
    EditText editTextProductName, editTextProductProducer, editTextProductQuantity;
    Button buttonSaveProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductQuantity = findViewById(R.id.editTextProductQuantity);
        editTextProductProducer = findViewById(R.id.editTextProductProducer);
        buttonSaveProduct = findViewById(R.id.buttonSaveProduct);

        buttonSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    private void saveProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String producer = editTextProductProducer.getText().toString().trim();
        String quantityText = editTextProductQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, "Podaj nazwę produktu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(producer)) {
            Toast.makeText(this, "Podaj producenta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityText)) {
            Toast.makeText(this, "Podaj ilość produktu", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity = Integer.parseInt(quantityText);

        if (quantity < 0) {
            Toast.makeText(this, "Ilość nie może być ujemna", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(productName, quantity, producer);

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(getApplicationContext()).productDao().insert(product);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddProductActivity.this, "Produkt dodany", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }).start();
    }
}