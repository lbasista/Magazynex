package pl.lbasista.magazynex.ui.product;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class SortDialogFragment extends DialogFragment {
    public interface SortDialogListener {void onSortSelected(String sortType);}

    private String selectedSort = "NAME_ASC"; //Domyślne sortowanie alfabetyczne po nazwie produktu

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] sortOptions = {
                "Kod rosnąco", "Kod malejąco",
                "Nazwa (A-Z)", "Nazwa (Z-A)",
                "Producent (A-Z)","Producent (Z-A)",
                "Ilość rosnąco","Ilość malejąco",
                "Zastosowanie (A-Z)", "Zastosowanie (Z-A)"
        };
        return new AlertDialog.Builder(getActivity())
                .setTitle("Sortuj według")
                .setSingleChoiceItems(sortOptions, 0, (dialog, which) -> {
                    switch (which) {
                        case 0: selectedSort = "BARCODE_ASC"; break;
                        case 1: selectedSort = "BARCODE_DESC"; break;
                        case 2: selectedSort = "NAME_ASC"; break;
                        case 3: selectedSort = "NAME_DESC"; break;
                        case 4: selectedSort = "PRODUCER_ASC"; break;
                        case 5: selectedSort = "PRODUCER_DESC"; break;
                        case 6: selectedSort = "QUANTITY_ASC"; break;
                        case 7: selectedSort = "QUANTITY_DESC"; break;
                        case 8: selectedSort = "CATEGORY_ASC"; break;
                        case 9: selectedSort = "CATEGORY_DESC"; break;
                    }
                })
                .setNegativeButton("Anuluj", null)
                .setPositiveButton("Sortuj", (dialog, which) -> {
                    Fragment parent = getParentFragment();
                    if (parent instanceof SortDialogListener) ((SortDialogListener) parent).onSortSelected(selectedSort);
                })
                .create();
    }
}