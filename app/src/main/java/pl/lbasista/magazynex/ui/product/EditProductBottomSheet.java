package pl.lbasista.magazynex.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.concurrent.Executors;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Product;

public class EditProductBottomSheet extends BottomSheetDialogFragment {
    private EditText editName, editProducer, editQuantity, editDescription;
    private String barcode, imageUri;

    public interface OnProductUpdatedListener {
        void onProductUpdated(Product updatedProduct);
    }

    private OnProductUpdatedListener listener;

    public void setOnProductUpdatedListener(OnProductUpdatedListener l) {
        listener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_edit_product, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle state) {
        Bundle args = getArguments();
        barcode = args.getString("barcode");
        imageUri = args.getString("imageUri");

        editName = view.findViewById(R.id.editName);
        editProducer = view.findViewById(R.id.editProducer);
        editQuantity = view.findViewById(R.id.editQuantity);
        editDescription = view.findViewById(R.id.editDescription);
        Button btnSave = view.findViewById(R.id.buttonSave);
        Button btnCancel = view.findViewById(R.id.buttonCancel);

        Executors.newSingleThreadExecutor().execute(() -> {
            Product existing = AppDatabase.getInstance(requireContext()).productDao().getByBarcode(barcode);
            requireActivity().runOnUiThread(() -> {
                editName.setText(existing.name);
                editProducer.setText(existing.producer);
                editQuantity.setText(String.valueOf(existing.quantity));
                editDescription.setText(existing.description);
            });
        });

        btnSave.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                Product p = db.productDao().getByBarcode(barcode);
                p.name = editName.getText().toString();
                p.producer = editProducer.getText().toString();
                p.quantity = Integer.parseInt(editQuantity.getText().toString());
                p.description = editDescription.getText().toString();
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
}
