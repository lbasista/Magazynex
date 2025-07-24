package pl.lbasista.magazynex.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.UserDao;

public class ProfileFragment extends Fragment {
    TextView userName, userLogin, userRole;
    Button buttonChangeLogin, buttonChangePassword, buttonUserOptions, buttonLogout;
    SessionManager session;
    User user;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        userName = view.findViewById(R.id.textviewUserName);
        userLogin = view.findViewById(R.id.textviewUserLogin);
        userRole = view.findViewById(R.id.textviewUserRole);
        buttonChangeLogin = view.findViewById(R.id.buttonChangeLogin);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);
        buttonUserOptions = view.findViewById(R.id.buttonUsers);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        session = new SessionManager(requireContext());

        if (!RoleChecker.isAdmin(session)) buttonUserOptions.setVisibility(View.GONE);

        getParentFragmentManager().setFragmentResultListener("update_profile", getViewLifecycleOwner(), (key, bundle) -> {
            loadUserData(session.getUserId());
        });

        int userId = session.getUserId();
        loadUserData(userId);

        buttonChangeLogin.setOnClickListener(v -> {
            updateProfileSettings(userId, true, false);
        });
        buttonChangePassword.setOnClickListener(v -> updateProfileSettings(userId, false, true));

        buttonUserOptions.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ManageAccountsActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener( v -> {
            session.clearSession();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        int userId = session.getUserId();
        loadUserData(userId);
    }

    private void updateProfileSettings(int userId, boolean login, boolean password) {
        DialogChangeLoginPassword dialog = new DialogChangeLoginPassword(userId, login, password);
        dialog.show(getParentFragmentManager(), "dialog_change_login_password");
    }

    private  void loadUserData(int userId) {
        new Thread(() -> {
            UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
            User user = userDao.getById(userId);
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    userName.setText(user.getFullName());
                    userLogin.setText("@" + user.login);
                    userRole.setText(user.role);
                }
            });
        }).start();
    }
}
