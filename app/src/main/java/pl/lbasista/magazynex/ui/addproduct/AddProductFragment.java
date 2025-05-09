package pl.lbasista.magazynex.ui.addproduct;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;

public class AddProductFragment extends Fragment {
    private EditText editTextName, editTextProducer, editTextQuantity;
    private Button buttonSave;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        editTextName = view.findViewById(R.id.editTextProductName);
        editTextProducer = view.findViewById(R.id.editTextProductProducer);
        editTextQuantity = view.findViewById(R.id.editTextProductQuantity);
        buttonSave = view.findViewById(R.id.buttonSaveProduct);

        buttonSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String productName = editTextName.getText().toString().trim();
        String producer = editTextProducer.getText().toString().trim();
        String quantityText = editTextQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(getContext(), "Podaj nazwę produktu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(producer)) {
            Toast.makeText(getContext(), "Podaj producenta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityText)) {
            Toast.makeText(getContext(), "Podaj ilość produktu", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity = Integer.parseInt(quantityText);

        if (quantity < 0) {
            Toast.makeText(getContext(), "Ilość nie może być ujemna", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(productName, quantity, producer, false);

        new Thread(() -> {
            ProductDao dao = AppDatabase.getInstance(requireContext()).productDao();

            Product existing = dao.getByNameAndProducer(productName, producer);
            if (existing != null) {
                //Istnieje = zwiększanie ilości
                existing.quantity += quantity;
                dao.update(existing);
            } else {
                //Brak w bazie = dodaj nowy
                dao.insert(product);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Produkt dodany", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }).start();
    }
}