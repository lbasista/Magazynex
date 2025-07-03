package pl.lbasista.magazynex.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;

public class AddCategoryBottomSheet extends BottomSheetDialogFragment {
    public interface OnCategoryAddedListener{
        void onCategoryAdded();
    }

    private OnCategoryAddedListener listener;

    public void setOnCategoryAddedListener(OnCategoryAddedListener l) {
        this.listener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_category, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle state) {
        EditText input = view.findViewById(R.id.editTextCategoryName);
        Button save = view.findViewById(R.id.buttonSaveCategory);
        Button cancel = view.findViewById(R.id.buttonCancelCategory);

        save.setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                input.setError("Podaj nazwę kategorii");
                return;
            }
            ApplicationCategory newCategory = new ApplicationCategory();
            newCategory.name = name;

            new Thread(() -> {
                AppDatabase.getInstance(requireContext()).applicationCategoryDao().insert(newCategory);

                requireActivity().runOnUiThread(() -> {
                    if (listener != null) listener.onCategoryAdded();
                    dismiss();
                });
            }).start();
        });

        cancel.setOnClickListener(v -> dismiss());
    }
}
