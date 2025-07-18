package pl.lbasista.magazynex.ui.addproduct;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.ProductDao;
import pl.lbasista.magazynex.ui.product.AddCategoryBottomSheet;

public class AddProductFragment extends Fragment {
    private EditText editTextBarcode, editTextName, editTextProducer, editTextQuantity, editTextDescription;
    TextInputLayout inputBarcodeLayout;
    private Button buttonBarcodeSearch, buttonSelectImage, buttonSave, buttonCancel, buttonMoreDetails;
    private String selectedImageUri = null;
    private int selectedApplicationCategoryId = 0; //0 = brak kategorii

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        editTextBarcode = view.findViewById(R.id.textInputBarcode);
        buttonBarcodeSearch = view.findViewById(R.id.searchBarcode);
        inputBarcodeLayout = view.findViewById(R.id.textInputLayoutBarcode);
        editTextName = view.findViewById(R.id.textInputName);
        editTextProducer = view.findViewById(R.id.textInputProducer);
        editTextQuantity = view.findViewById(R.id.textInputQuantity);
        buttonMoreDetails = view.findViewById(R.id.moreDetailsButton);
        buttonSave = view.findViewById(R.id.buttonSaveProduct);
        buttonCancel = view.findViewById(R.id.buttonCancelInputs);

        buttonBarcodeSearch.setOnClickListener(v -> searchByBarcode());
        buttonCancel.setOnClickListener(v -> clearInputs());
        buttonSave.setOnClickListener(v -> saveProduct());

        //Więcej szczegółów
        buttonMoreDetails.setOnClickListener(v -> {
            ViewStub moreDetails = view.findViewById(R.id.moreDetailsStub);
            View inflated = moreDetails.inflate();
            buttonMoreDetails.setVisibility(View.GONE);

            editTextDescription = view.findViewById(R.id.textInputDescription);
            buttonSelectImage = view.findViewById(R.id.buttonSelectImage);

            buttonSelectImage.setOnClickListener(x -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Uprawnienia do zdjęć
                startActivityForResult(intent, 101);
            });

            setupApplicationCategoryDropdown(view);
            setupInputs(view);
        });
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
                    inputBarcodeLayout.setError(null);
                } else {
                    //Brak produktu w bazie
                    inputBarcodeLayout.setError("Brak kodu w Twojej bazie");
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
        MaterialAutoCompleteTextView dropdown = view.findViewById(R.id.dropdownCategory);

        //Pobierz listę
        ApplicationCategoryDao dao = AppDatabase.getInstance(requireContext()).applicationCategoryDao();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dao.getAll().observe(getViewLifecycleOwner(), categories -> {
                //Adapter do wyświetlania kategorii w dropdownie
                ArrayAdapter<ApplicationCategory> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categories
                );
                dropdown.setAdapter(adapter);

                if (categories.isEmpty()) {
                    Toast.makeText(getContext(), "Brak dostępnych kategorii. Dodaj nową", Toast.LENGTH_SHORT).show();
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
        }, 250);
    }

    private void setupInputs(View view) {
        Button addCategory = view.findViewById(R.id.addCategory);

        addCategory.setOnClickListener(v -> {
            AddCategoryBottomSheet bottomSheet = new AddCategoryBottomSheet();
            bottomSheet.setOnCategoryAddedListener(() -> {});
            bottomSheet.show(getParentFragmentManager(), "AddApplicationBottomSheet");
        });
    }

    private void saveProduct() {
        String barcodeInput = editTextBarcode.getText().toString().trim();
        String productName = editTextName.getText().toString().trim();
        String producer = editTextProducer.getText().toString().trim();
        String quantityText = editTextQuantity.getText().toString().trim();
        String description = (editTextDescription != null) ? editTextDescription.getText().toString().trim() : "";

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
                //requireActivity().getSupportFragmentManager().popBackStack();
                clearInputs();
            });
        }).start();
    }

    private void clearInputs() {
        editTextBarcode.setText("");
        editTextName.setText("");
        editTextProducer.setText("");
        editTextQuantity.setText("");
        if (editTextDescription != null) editTextDescription.setText("");
        selectedImageUri = null;
        if (buttonSelectImage != null) buttonSelectImage.setText("Wybierz zdjęcie");
        MaterialAutoCompleteTextView dropdown = getView().findViewById(R.id.dropdownCategory);
        if (dropdown != null) dropdown.setText("");
        selectedApplicationCategoryId = 0;
    }
}