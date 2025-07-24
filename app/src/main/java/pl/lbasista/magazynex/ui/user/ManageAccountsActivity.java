package pl.lbasista.magazynex.ui.user;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.UserDao;

public class ManageAccountsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAccount;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);

        recyclerViewAccount = findViewById(R.id.recyclerViewAccounts);
        recyclerViewAccount.setLayoutManager(new LinearLayoutManager(this));

        getSupportFragmentManager().setFragmentResultListener("user_added", this, (key, bundle) -> {
            reloadAccounts();
        });
        getSupportFragmentManager().setFragmentResultListener("user_updated", this, (key, bundle) -> {
            reloadAccounts();
        });

        toolbar = findViewById(R.id.appBarUserDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.addUser) {
                Dialog_add_user dialog = new Dialog_add_user();
                dialog.show(getSupportFragmentManager(), "dialog_add_user");
                return true;
            }
            return false;
        });

        new Thread(() -> {
            UserDao userDao = AppDatabase.getInstance(this).userDao();
            List<User> users = userDao.getAllUsers();

            runOnUiThread(() -> {
                AccountsAdapter accountsAdapter = new AccountsAdapter(users);
                recyclerViewAccount.setAdapter(accountsAdapter);
            });
        }).start();
    }

    private void reloadAccounts() {
        new Thread(() -> {
            UserDao userDao = AppDatabase.getInstance(this).userDao();
            List<User> users = userDao.getAllUsers();
            runOnUiThread(() -> recyclerViewAccount.setAdapter(new AccountsAdapter(users)));
        }).start();
    }
}
