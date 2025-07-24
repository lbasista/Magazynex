package pl.lbasista.magazynex.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.User;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountViewHolder> {
    private final List<User> users;

    public AccountsAdapter(List<User> users) {this.users = users;}

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvLogin.setText(user.login);
        holder.tvFullName.setText(user.getFullName());
        holder.tvRole.setText(user.role);
        holder.btnEdit.setOnClickListener(v -> {
            Dialog_edit_user dialog = new Dialog_edit_user(users.get(position));
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            dialog.show(activity.getSupportFragmentManager(), "edit_user");
        });
    }

    @Override
    public int getItemCount() {return users.size();}

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogin, tvFullName, tvRole;
        ImageButton btnEdit;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogin = itemView.findViewById(R.id.tvLogin);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
        }
    }
}