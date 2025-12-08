package pl.lbasista.magazynex.ui.user;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.repo.RemoteUserRepository;
import pl.lbasista.magazynex.data.repo.RoomUserRepository;
import pl.lbasista.magazynex.data.repo.UserRepository;

public class ManageAccountsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAccount;
    private MaterialToolbar toolbar;
    private UserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);
        SessionManager session = new SessionManager(this);
        int loggedUserId = session.getUserId();

        userRepository = session.isRemoteMode() ? new RemoteUserRepository(this, session.getApiUrl()) : new RoomUserRepository(this);

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
            if (id == R.id.addObject) {
                Dialog_add_user dialog = new Dialog_add_user();
                dialog.show(getSupportFragmentManager(), "dialog_add_user");
                return true;
            }
            return false;
        });

        new Thread(() -> {
            List<User> users = userRepository.getAllUsers();
            runOnUiThread(() -> {
                AccountsAdapter accountsAdapter = new AccountsAdapter(users, loggedUserId);
                recyclerViewAccount.setAdapter(accountsAdapter);
            });
        }).start();
    }

    private void reloadAccounts() {
        SessionManager session = new SessionManager(this);
        UserRepository userRepository = session.isRemoteMode() ? new RemoteUserRepository(this, session.getApiUrl()) : new RoomUserRepository(this);
        int loggedUserId = session.getUserId();
        new Thread(() -> {
            List<User> users = userRepository.getAllUsers();
            runOnUiThread(() -> recyclerViewAccount.setAdapter(new AccountsAdapter(users, loggedUserId)));
        }).start();
    }
}
