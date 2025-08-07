package pl.lbasista.magazynex.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import at.favre.lib.crypto.bcrypt.BCrypt;
import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.UserDao;
import pl.lbasista.magazynex.ui.main.MainMenuActivity;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout inputLoginLayout, inputPasswordLayout, inputAddressLayout;
    TextInputEditText inputLogin, inputPassword, inputAddress;
    Button buttonLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputLoginLayout = findViewById(R.id.inputLoginLayout);
        inputLogin = findViewById(R.id.inputLogin);
        inputPasswordLayout = findViewById(R.id.inputPasswordLayout);
        inputPassword = findViewById(R.id.inputPassword);
        inputAddressLayout = findViewById(R.id.inputAddressLayout);
        inputAddress = findViewById(R.id.inputAddress);
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
                user.password = BCrypt.withDefaults().hashToString(12, "admin".toCharArray());
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

            String serverAddress = inputAddress.getText().toString().trim();
            if (serverAddress.isEmpty()) {
                //Lokalna baza Room
                new Thread(() -> {
                    User user = AppDatabase.getInstance(this).userDao().getByLogin(userLogin);
                    if (user == null || !BCrypt.verifyer().verify(userPassword.toCharArray(), user.password).verified) {
                        runOnUiThread(() -> Toast.makeText(this, "Błędne dane", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, MainMenuActivity.class);
                        SessionManager session = new SessionManager(this);
                        session.saveUserSession(user.id);
                        session.saveUserRole(user.role);
                        session.setRemoteMode(false);
                        startActivity(intent);
                        finish();
                    });
                }).start();
            } else {
                //Zewnętrzna baza przez API
                if (!serverAddress.startsWith("http")) {
                    serverAddress = "https://" + serverAddress;
                }
                String apiUrl = serverAddress + "/api.php";
                Log.d("API", "Adres API: " + apiUrl);
                SessionManager session = new SessionManager(this);
                session.setApiUrl(apiUrl);
                RequestQueue queue = Volley.newRequestQueue(this);
                JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, apiUrl, null, response -> {
                    Log.d("API", "Pobrano dane z API. Liczba rekordów: " + response.length());
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, MainMenuActivity.class);
                        intent.putExtra("api_url", apiUrl);
                        session.saveUserSession(0);
                        session.saveUserRole("Zdalny");
                        session.setRemoteMode(true);
                        startActivity(intent);
                        finish();
                    });
                }, error -> {
                    Log.e("API", "Błąd zapytania: " + error.toString());
                    runOnUiThread(() -> Toast.makeText(this, "Błąd połączenia z bazą zewnętrzną", Toast.LENGTH_SHORT).show());
                });
                queue.add(request);
            }
        });
    }
}