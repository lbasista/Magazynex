package pl.lbasista.magazynex.ui.addproduct;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;
import pl.lbasista.magazynex.ui.product.AddApplicationCategoryDialogFragment;

public class AddProductFragment extends Fragment {
    private EditText editTextBarcode, editTextName, editTextProducer, editTextQuantity, editTextDescription;
    private TextView textBarcodeError;
    private Button buttonBarcodeSearch, buttonSelectImage, buttonSave;
    private String selectedImageUri = null;
    private int selectedApplicationCategoryId = 0; //0 = brak kategorii

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        editTextBarcode = view.findViewById(R.id.editTextProductBarcode);
        buttonBarcodeSearch = view.findViewById(R.id.buttonBarcodeSearch);
        textBarcodeError = view.findViewById(R.id.textBarcodeError);
        editTextName = view.findViewById(R.id.editTextProductName);
        editTextProducer = view.findViewById(R.id.editTextProductProducer);
        editTextQuantity = view.findViewById(R.id.editTextProductQuantity);
        editTextDescription = view.findViewById(R.id.editTextProductDescription);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        buttonSave = view.findViewById(R.id.buttonSaveProduct);

        buttonBarcodeSearch.setOnClickListener(v -> searchByBarcode());
        buttonSave.setOnClickListener(v -> saveProduct());

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Uprawnienia do zdjęć
            startActivityForResult(intent, 101);
        });

        setupApplicationCategoryDropdown(view);
    }

    private void searchByBarcode() {
        String barcode = editTextBarcode.getText().toString().trim();
        if (barcode.isEmpty()) {
            return;
        }
        new Thread(() -> {
            Product existing = AppDatabase.getInstance(requireContext()).productDao().getByBarcode(barcode);

            requireActivity().runOnUiThread(() -> {
                if (existing != null) {
                    //Jeśli produkt w bazie
                    editTextName.setText(existing.name);
                    editTextProducer.setText(existing.producer);
                    textBarcodeError.setVisibility(View.GONE);
                } else {
                    //Brak produktu w bazie
                    textBarcodeError.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    //Zdjęcie produktu
    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedImageUri = uri.toString();

                final int takeFlags = data.getFlags();
                if ((takeFlags & Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) != 0) {
                    final int realFlags = takeFlags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(uri, realFlags);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                //Nazwa pliku
                String fileName = "Wybrano zdjęcie";
                Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (cursor.moveToFirst() && nameIndex >= 0) fileName = cursor.getString(nameIndex);
                    cursor.close();
                }
                buttonSelectImage.setText("Wybrano: " + fileName);
            }
        }
    }

    private void setupApplicationCategoryDropdown(View view) {
        MaterialAutoCompleteTextView dropdown = view.findViewById(R.id.dropdownApplication);

        //Pobierz listę
        ApplicationCategoryDao dao = AppDatabase.getInstance(requireContext()).applicationCategoryDao();

        dao.getAll().observe(getViewLifecycleOwner(), categories -> {
            //Adapter do wyświetlania kategorii w dropdownie
            ArrayAdapter<ApplicationCategory> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categories
            );
            dropdown.setAdapter(adapter);

            if (categories.isEmpty()) {
                dropdown.setHint("Brak kategorii - dodaj nową");
                Toast.makeText(getContext(), "Brak dostępnych zastosowań. Dodaj nowe", Toast.LENGTH_SHORT).show();
            }

            dropdown.setOnItemClickListener((parent, v, position, id) -> {
                ApplicationCategory selected = adapter.getItem(position);
                if (selected != null) {
                    selectedApplicationCategoryId = selected.id; //Zapis wybranej kategorii
                }
            });
            dropdown.setOnClickListener(v -> dropdown.showDropDown());
            dropdown.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) dropdown.showDropDown();
            });
        });
    }

    private void setupInputs(View view) {
        TextView textAddApplication = view.findViewById(R.id.textAddApplicationCategory);

        textAddApplication.setOnClickListener(v -> {
            AddApplicationCategoryDialogFragment dialog = new AddApplicationCategoryDialogFragment();
            dialog.show(getParentFragmentManager(), "AddApplicationCategoryDialog");
        });
    }

    private void saveProduct() {
        String barcodeInput = editTextBarcode.getText().toString().trim();
        String productName = editTextName.getText().toString().trim();
        String producer = editTextProducer.getText().toString().trim();
        String quantityText = editTextQuantity.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        String barcode = barcodeInput.isEmpty() ? null : barcodeInput;

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

        Product product = new Product(barcode, productName, quantity, producer, false, selectedApplicationCategoryId, description, selectedImageUri);

        new Thread(() -> {
            ProductDao dao = AppDatabase.getInstance(requireContext()).productDao();

            Product existing;
            if (!barcodeInput.isEmpty()) {
                //Gdy jest kod kreskowy, szukaj po nim
                existing = dao.getByBarcode(barcodeInput);
            } else {
                //Gdy brak kodu
                existing = dao.getByNameAndProducer(productName, producer);
            }

            if (existing != null) {
                //Produkt w bazie
                existing.quantity += quantity;
                dao.update(existing);
            } else {
                //Brak w bazie
                dao.insert(product);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Produkt dodany", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }).start();
    }
}