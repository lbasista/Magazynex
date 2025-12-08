package pl.lbasista.magazynex.ui.user;

import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import at.favre.lib.crypto.bcrypt.BCrypt;
import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.repo.RemoteUserRepository;
import pl.lbasista.magazynex.data.repo.RoomUserRepository;
import pl.lbasista.magazynex.data.repo.UserRepository;

public class DialogChangeLoginPassword extends DialogFragment {
    private final int userId;
    private final boolean showLogin;
    private final boolean showPassword;

    public DialogChangeLoginPassword(int userId, boolean showLogin, boolean showPassword) {
        this.userId = userId;
        this.showLogin = showLogin;
        this.showPassword = showPassword;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_change_login_password, null);

        SessionManager session = new SessionManager(requireContext());
        UserRepository userRepository = session.isRemoteMode() ? new RemoteUserRepository(requireContext(), session.getApiUrl()) : new RoomUserRepository(requireContext());

        TextInputEditText editLogin = view.findViewById(R.id.inputEditLogin);
        TextInputEditText editPassword = view.findViewById(R.id.inputEditPassword);
        TextInputEditText editconfirmPassword = view.findViewById(R.id.inputConfirmPassword);

        View layoutLogin = view.findViewById(R.id.inputEditLoginLayout);
        View layoutPassword = view.findViewById(R.id.inputEditPasswordLayout);
        View layoutConfirmPassword = view.findViewById(R.id.inputConfirmPasswordLayout);

        Button buttonSave = view.findViewById(R.id.buttonSaveEdits);
        Button buttonCancel = view.findViewById(R.id.buttonCancelEdits);

        view.findViewById(R.id.inputEditLoginLayout).setVisibility(showLogin ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.inputEditPasswordLayout).setVisibility(showPassword ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.inputConfirmPasswordLayout).setVisibility(showPassword ? View.VISIBLE : View.GONE);

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> {
            String newLogin = editLogin.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();
            String confirmPassword = editconfirmPassword.getText().toString().trim();

            if (showPassword && !newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Hasła muszą być identyczne", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = userRepository.getById(userId);
                if (user != null) {
                    if (showLogin) user.login = newLogin;
                    if (showPassword) user.password = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
                    boolean ok = userRepository.updateUser(user);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Zaktualizowano dane", Toast.LENGTH_SHORT);
                        getParentFragmentManager().setFragmentResult("update_profile", new Bundle());
                        dismiss();
                    });
                }
            }).start();
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