package pl.lbasista.magazynex.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Order;

public class AddOrderBottomSheet extends BottomSheetDialogFragment {
    public interface OnOrderAddedListener {void onOrderAdded(Order newOrder);}

    private OnOrderAddedListener listener;
    public void setOnOrderAddedListener(OnOrderAddedListener l) {listener = l;}

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText nameEt = view.findViewById(R.id.editTextOrderName);
        Button saveBtn = view.findViewById(R.id.buttonSaveOrder);
        Button cancelBtn = view.findViewById(R.id.buttonCancel);

        saveBtn.setOnClickListener(v -> {
            String name = nameEt.getText().toString().trim();
            if (name.isEmpty()) {
                nameEt.setError("Podaj nazwę");
                return;
            }

            Order newOrder = new Order(name, 0);

            new Thread(() -> {
                long id = AppDatabase
                        .getInstance(requireContext())
                        .orderDao()
                        .insert(newOrder);
                newOrder.setId((int) id);

                requireActivity().runOnUiThread(() -> {
                    listener.onOrderAdded(newOrder);
                    dismiss();
                });
            }).start();
        });

        cancelBtn.setOnClickListener(v -> dismiss());
    }
}
