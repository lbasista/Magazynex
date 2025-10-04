package pl.lbasista.magazynex.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.repo.CategoryRepository;
import pl.lbasista.magazynex.data.repo.RemoteCategoryRepository;
import pl.lbasista.magazynex.data.repo.RoomCategoryRepository;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class AddCategoryBottomSheet extends BottomSheetDialogFragment {
    public interface OnCategoryAddedListener {void onCategoryAdded();}

    private OnCategoryAddedListener listener;
    private CategoryRepository categoryRepository;

    public void setOnCategoryAddedListener(OnCategoryAddedListener l) {this.listener = l;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_category, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle state) {
        EditText input = view.findViewById(R.id.editTextCategoryName);
        Button save = view.findViewById(R.id.buttonSaveCategory);
        Button cancel = view.findViewById(R.id.buttonCancelCategory);

        SessionManager session = new SessionManager(requireContext());
        if (session.isRemoteMode()) categoryRepository = new RemoteCategoryRepository(requireContext(), session.getApiUrl());
        else categoryRepository = new RoomCategoryRepository(requireContext());

        save.setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                input.setError("Podaj nazwę kategorii");
                return;
            }
            ApplicationCategory newCategory = new ApplicationCategory();
            newCategory.name = name;

            new Thread(() -> {
                long insertedId = categoryRepository.insertCategory(newCategory);
                requireActivity().runOnUiThread(() -> {
                    if (insertedId > 0) {
                        if (listener != null) listener.onCategoryAdded();
                        dismiss();
                    } else Toast.makeText(requireContext(), "Nie udało się dodać kategorii", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        cancel.setOnClickListener(v -> dismiss());
    }
}