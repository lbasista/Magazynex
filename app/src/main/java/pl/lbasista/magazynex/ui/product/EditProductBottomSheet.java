package pl.lbasista.magazynex.ui.product;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.concurrent.Executors;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.Product;

public class EditProductBottomSheet extends BottomSheetDialogFragment {
    private EditText editName, editProducer, editQuantity, editDescription;
    private TextInputLayout editNameLayout, editProducerLayout, editQuantityLayout;
    private String barcode, imageUri;
    private Button selectImage, removeImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private AutoCompleteTextView editCategoryDropdown;
    private int selectedCategoryId = 0;

    public interface OnProductUpdatedListener {
        void onProductUpdated(Product updatedProduct);
    }

    private OnProductUpdatedListener listener;

    public void setOnProductUpdatedListener(OnProductUpdatedListener l) {
        listener = l;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    imageUri = uri.toString();
                    selectImage.setText("Wybrano: " + getFileNameFromUri(uri));
                    removeImage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null)) {
                if (cursor != null & cursor.moveToFirst()) return cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        }
        String path = uri.getLastPathSegment();
        return (path != null) ? path.substring(path.lastIndexOf('/') + 1) : "plik";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_edit_product, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle state) {
        Bundle args = getArguments();
        barcode = args.getString("barcode");

        editName = view.findViewById(R.id.editName);
        editProducer = view.findViewById(R.id.editProducer);
        editQuantity = view.findViewById(R.id.editQuantity);
        editDescription = view.findViewById(R.id.editDescription);
        editCategoryDropdown = view.findViewById(R.id.editCategoryDropdown);
        selectImage = view.findViewById(R.id.buttonSelectImage);
        removeImage = view.findViewById(R.id.buttonRemoveImage);
        Button btnSave = view.findViewById(R.id.buttonSave);
        Button btnCancel = view.findViewById(R.id.buttonCancel);

        Executors.newSingleThreadExecutor().execute(() -> {
            Product existing = AppDatabase.getInstance(requireContext()).productDao().getByBarcode(barcode);
            List<ApplicationCategory> categories = AppDatabase.getInstance(requireContext()).applicationCategoryDao().getAllSync();
            requireActivity().runOnUiThread(() -> {
                editName.setText(existing.name);
                editProducer.setText(existing.producer);
                editQuantity.setText(String.valueOf(existing.quantity));
                editDescription.setText(existing.description);
                if (categories.isEmpty()) {
                    editCategoryDropdown.setText("");
                    selectedCategoryId = 0;
                    editCategoryDropdown.setEnabled(false);
                } else {
                    ArrayAdapter<ApplicationCategory> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categories
                    );
                    editCategoryDropdown.setAdapter(adapter);

                    ApplicationCategory currentCategory = null;
                    for (ApplicationCategory cat : categories) {
                        if (cat.id == existing.applicationCategoryId) {
                            currentCategory = cat;
                            break;
                        }
                    }
                    if (currentCategory != null) {
                        editCategoryDropdown.setText(currentCategory.name, false);
                        selectedCategoryId = currentCategory.id;
                    }

                    editCategoryDropdown.setOnItemClickListener((parent, v1, position, id) -> {
                        ApplicationCategory selected = adapter.getItem(position);
                        if (selected != null) {
                            selectedCategoryId = selected.id;
                        }
                    });
                    editCategoryDropdown.setOnClickListener(v -> editCategoryDropdown.showDropDown());
                    editCategoryDropdown.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) editCategoryDropdown.showDropDown();
                    });
                }
            });
            imageUri = existing.imageUri;
            if (imageUri != null && !imageUri.isEmpty()) {
                try {
                    Uri uri = Uri.parse(imageUri);
                    selectImage.setText("Wybrano: " + getFileNameFromUri(uri));
                    removeImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    selectImage.setText("Wybierz zdjęcie");
                    removeImage.setVisibility(View.GONE);
                }
            } else {
                selectImage.setText("Wybierz zdjęcie");
                removeImage.setVisibility(View.GONE);
            }
        });

        selectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Wybierz zdjęcie"));
        });

        removeImage.setOnClickListener(v -> {
            imageUri = null;
            selectImage.setText("Wybierz zdjęcie");
            removeImage.setVisibility(View.GONE);
        });

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String producer = editProducer.getText().toString().trim();
            String quantityText = editQuantity.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            editNameLayout = view.findViewById(R.id.editNameLayout);
            editProducerLayout = view.findViewById(R.id.editProducerLayout);
            editQuantityLayout = view.findViewById(R.id.editQuantityLayout);

            boolean hasError = false;
            if (TextUtils.isEmpty(name)) {
                editNameLayout.setError("Podaj nazwę");
                hasError = true;
            } else editNameLayout.setError(null);

            if (TextUtils.isEmpty(producer)) {
                editProducerLayout.setError("Podaj producenta");
                hasError = true;
            } else editProducerLayout.setError(null);

            int quantity = 0;
            if (TextUtils.isEmpty(quantityText)) {
                editQuantityLayout.setError("Podaj ilość");
                hasError = true;
            } else {
                try {
                    quantity = Integer.parseInt(quantityText);
                    if (quantity < 0) {
                        editQuantityLayout.setError("Liczba nie może być ujemna");
                        hasError = true;
                    } else editQuantityLayout.setError(null);
                } catch (NumberFormatException e) {
                    editQuantityLayout.setError("Nieprawidłowa wartość");
                    hasError = true;
                }
            }
            if (hasError) return;

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                Product p = db.productDao().getByBarcode(barcode);
                p.name = name;
                p.producer = producer;
                p.quantity = Integer.parseInt(quantityText);
                p.description = description;
                p.applicationCategoryId = selectedCategoryId;
                p.imageUri = imageUri;
                db.productDao().update(p);
                requireActivity().runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onProductUpdated(p);
                    }
                    dismiss();
                });
            });
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    //Ekran nad klawiaturą
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());
            }
        });
        return dialog;
    }
}
