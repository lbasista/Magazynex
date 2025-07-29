package pl.lbasista.magazynex.ui.user;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import at.favre.lib.crypto.bcrypt.BCrypt;
import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;

public class Dialog_edit_user extends DialogFragment {
    private final User userToEdit;
    private final int loggedUserId;

    public Dialog_edit_user(User user, int loggedUserId) {
        this.userToEdit = user;
        this.loggedUserId = loggedUserId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);

        TextView title = view.findViewById(R.id.dialogTitleAdd);
        title.setVisibility(View.GONE);

        TextInputEditText inputLogin = view.findViewById(R.id.inputNewLogin);
        TextInputEditText inputPassword = view.findViewById(R.id.inputNewPassword);
        TextInputEditText inputName = view.findViewById(R.id.inputNewName);
        TextInputEditText inputSurname = view.findViewById(R.id.inputNewSurname);
        AutoCompleteTextView dropdownRole = view.findViewById(R.id.dropdownRole);
        TextInputLayout layoutLogin = view.findViewById(R.id.inputNewLoginLayout);
        TextInputLayout layoutPassword = view.findViewById(R.id.inputNewPasswordLayout);
        TextInputLayout layoutName = view.findViewById(R.id.inputNewNameLayout);
        TextInputLayout layoutSurname = view.findViewById(R.id.inputNewSurnameLayout);
        TextInputLayout layoutRole = view.findViewById(R.id.dropdownRoleLayout);
        TextView roleInfo = view.findViewById(R.id.textRoleInformation);
        Button buttonRemove = view.findViewById(R.id.buttonRemoveUser);
        Button buttonSave = view.findViewById(R.id.buttonSaveUser);
        Button buttonCancel = view.findViewById(R.id.buttonCancelUser);

        String[] roleOptions = new String[] {"Administrator", "Pracownik", "Przeglądający"};
        ArrayAdapter<String> adapterRoles = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, roleOptions);
        dropdownRole.setAdapter(adapterRoles);

        inputLogin.setText(userToEdit.login);
        inputPassword.setText(userToEdit.password);
        inputName.setText(userToEdit.name);
        inputSurname.setText(userToEdit.surname);
        dropdownRole.setText(userToEdit.role, false);

        if (userToEdit.id == loggedUserId) buttonRemove.setVisibility(View.GONE);
        else {
            buttonRemove.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Usuwanie użytkownika")
                        .setMessage("Czy na pewno chcesz usunąć tego użytkownika?")
                        .setPositiveButton("Usuń", (dialogInterface, i) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(requireContext()).userDao().deleteUser(userToEdit);
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Użytkownik usunięty", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().setFragmentResult("user_updated", new Bundle());
                                    dismiss();
                                });
                            }).start();
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
            });
        }

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> {
            String login = inputLogin.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String name = inputName.getText().toString().trim();
            String surname = inputSurname.getText().toString().trim();
            String role = dropdownRole.getText().toString().trim();

            boolean hasError = false;
            if (login.isEmpty()) {
                layoutLogin.setError("Wpisz login");
                hasError = true;
            } else layoutLogin.setError(null);
            if (password.isEmpty()) {
                layoutPassword.setError("Wpisz hasło");
                hasError = true;
            } else layoutPassword.setError(null);
            if (name.isEmpty()) {
                layoutName.setError("Wpisz imię");
                hasError = true;
            } else layoutName.setError(null);
            if (surname.isEmpty()) {
                layoutSurname.setError("Wpisz nazwisko");
                hasError = true;
            } else layoutSurname.setError(null);
            if (role.isEmpty()) {
                layoutRole.setError("Wybierz rolę");
                hasError = true;
            } else layoutRole.setError(null);
            if (hasError) return;

            new Thread(() -> {
                userToEdit.login = login;
                userToEdit.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                userToEdit.name = name;
                userToEdit.surname = surname;
                userToEdit.role = role;

                AppDatabase.getInstance(requireContext()).userDao().updateUser(userToEdit);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Zaktualizowano użytkownika", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("user_updated", new Bundle());
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