package pl.lbasista.magazynex.ui.product;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;

public class AddApplicationCategoryDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Tworzenie pola do wpisywania nazwy kategorii
        final EditText input = new EditText(requireContext());
        input.setHint("Wprowadź nowe zastosowanie");

        return new AlertDialog.Builder(requireContext())
                .setTitle("Nowe zastosowanie")
                .setView(input)
                .setPositiveButton("Zapisz", ((dialog, which) -> {
                    String categoryName = input.getText().toString().trim();

                    if (!categoryName.isEmpty()) {
                        ApplicationCategory category = new ApplicationCategory();
                        category.name = categoryName;

                        new Thread(() -> {
                            AppDatabase.getInstance(requireContext()).applicationCategoryDao().insert(category);
                        }).start();
                    }
                }))
                .setNegativeButton("Anuluj", null)
                .create();
    }
}
