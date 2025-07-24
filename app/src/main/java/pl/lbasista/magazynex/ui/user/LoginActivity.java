package pl.lbasista.magazynex.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.UserDao;
import pl.lbasista.magazynex.ui.main.MainMenuActivity;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout inputLoginLayout, inputPasswordLayout;
    TextInputEditText inputLogin, inputPassword;
    Button buttonLogin;
    UserDao userDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputLoginLayout = findViewById(R.id.inputLoginLayout);
        inputLogin = findViewById(R.id.inputLogin);
        inputPasswordLayout = findViewById(R.id.inputPasswordLayout);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        //Brak użytkowników w bazie
        new Thread(() -> {
            UserDao userDao = AppDatabase.getInstance(this).userDao();
            List<User> users = userDao.getAllUsers();
            if (users.isEmpty()) {
                User user = new User();
                user.name = "Admin";
                user.surname = "Admin";
                user.login = "admin";
                user.password = "admin";
                user.role = "Administrator";
                userDao.insert(user);
            }
        }).start();

        buttonLogin.setOnClickListener(v -> {
            String userLogin = inputLogin.getText().toString().trim();
            String userPassword = inputPassword.getText().toString().trim();

            boolean isError = false;
            if (userLogin.isEmpty()) {
                inputLoginLayout.setError("Wprawadź login");
                isError = true;
            } else inputLoginLayout.setError(null);
            if (userPassword.isEmpty()) {
                inputPasswordLayout.setError("Wprowadź hasło");
                isError = true;
            } else inputPasswordLayout.setError(null);
            if (isError) return;

            new Thread(() -> {
                User user = AppDatabase.getInstance(this).userDao().getLogin(userLogin, userPassword);
                if (user == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Błędne dane", Toast.LENGTH_SHORT).show());
                    return;
                }
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, MainMenuActivity.class);
                    SessionManager session = new SessionManager(this);
                    session.saveUserSession(user.id);
                    startActivity(intent);
                    finish();
                });
            }).start();
        });
    }
}