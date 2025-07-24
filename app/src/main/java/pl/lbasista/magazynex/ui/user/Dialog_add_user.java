package pl.lbasista.magazynex.ui.user;


import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;

public class Dialog_add_user extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);

        TextInputEditText inputLogin = view.findViewById(R.id.inputNewLogin);
        TextInputLayout inputLoginLayout = view.findViewById(R.id.inputNewLoginLayout);
        TextInputEditText inputPassword = view.findViewById(R.id.inputNewPassword);
        TextInputLayout inputPasswordLayout = view.findViewById(R.id.inputNewPasswordLayout);
        TextInputEditText inputName = view.findViewById(R.id.inputNewName);
        TextInputLayout inputNameLayout = view.findViewById(R.id.inputNewNameLayout);
        TextInputEditText inputSurname = view.findViewById(R.id.inputNewSurname);
        TextInputLayout inputSurnameLayout = view.findViewById(R.id.inputNewSurnameLayout);
        AutoCompleteTextView dropdownRole = view.findViewById(R.id.dropdownRole);
        TextInputLayout dropdownRoleLayout = view.findViewById(R.id.dropdownRoleLayout);
        TextView roleInfo = view.findViewById(R.id.textRoleInformation);
        Button buttonSave = view.findViewById(R.id.buttonSaveUser);
        Button buttonCancel = view.findViewById(R.id.buttonCancelUser);

        TextView title = view.findViewById(R.id.dialogTitleEdit);
        title.setVisibility(View.GONE);

        String[] roleOptions = new String[] {"Administrator", "Pracownik", "Przeglądający"};
        ArrayAdapter<String> adapterRoles = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, roleOptions);
        dropdownRole.setAdapter(adapterRoles);

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> {
            String login = inputLogin.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String name = inputName.getText().toString().trim();
            String surname = inputSurname.getText().toString().trim();
            String role = dropdownRole.getText().toString().trim();

            boolean hasError = false;
            if (login.isEmpty()) {
                inputLoginLayout.setError("Wprowadź login");
                hasError = true;
            }
            if (password.isEmpty()) {
                inputPasswordLayout.setError("Wprowadź hasło");
                hasError = true;
            }
            if (name.isEmpty()) {
                inputNameLayout.setError("Wprowadź imię");
                hasError = true;
            }
            if (surname.isEmpty()) {
                inputSurnameLayout.setError("Wprowadź nazwisko");
                hasError = true;
            }
            if (role.isEmpty()) {
                dropdownRoleLayout.setError("Wybierz rolę");
                hasError = true;
            }
            if (hasError) return;

            new Thread(() -> {
                User newUser = new User();
                newUser.login = login;
                newUser.password = password;
                newUser.name = name;
                newUser.surname = surname;
                newUser.role = role;

                AppDatabase.getInstance(requireContext()).userDao().insert(newUser);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Użytkownik dodany", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("user_added", new Bundle());
                    dismiss();
                });
            }).start();
        });

        roleInfo.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Różnice między rolami")
                    .setMessage("Administrator - pełny dostęp: może edytować, usuwać, zarządzać użytkownikami\n\n" +
                            "Pracownik - może przeglądać oraz zarządzać produktami i listami\n\n" +
                            "Przeglądający - tylko podgląd danych, bez możliwości edycji.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
