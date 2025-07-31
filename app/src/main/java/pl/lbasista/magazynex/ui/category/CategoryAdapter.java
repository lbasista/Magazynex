package pl.lbasista.magazynex.ui.category;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.Product;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<ApplicationCategory> categoryList;

    public CategoryAdapter(List<ApplicationCategory> categoryList) {this.categoryList = categoryList;}

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ApplicationCategory category = categoryList.get(position);
        holder.textCategoryName.setText(category.name);
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            new Thread(() -> {
                List<Product> products = AppDatabase.getInstance(context).productDao().getByCategoryId(category.id);
                StringBuilder message = new StringBuilder();

                if (products.isEmpty()) message.append("Wybrana kategoria jest pusta");
                else {
                    for (Product p : products) {
                        message.append("• " + p.name + " (" + p.producer + ")\n");
                    }
                }
                ((Activity) context).runOnUiThread(() -> {
                    new AlertDialog.Builder(context)
                            .setTitle(category.name)
                            .setMessage("Produkty na liście:\n" + message.toString())
                            .setPositiveButton("Zamknij", null)
                            .show();
                });
            }).start();
        });
        holder.editCategory.setOnClickListener(v -> showEditCategoryDialog(v.getContext(), category, () -> notifyItemChanged(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textCategoryName;
        ImageButton editCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.textCategoryName);
            editCategory = itemView.findViewById(R.id.btnEditCategory);
        }
    }

    private void showEditCategoryDialog(Context context, ApplicationCategory category, Runnable onUpdated) {
        EditText input = new EditText(context);
        input.setText(category.name);
        input.setSelection(category.name.length());

        new AlertDialog.Builder(context)
                .setTitle("Edytuj nazwę kategorii")
                .setView(input)
                .setNegativeButton("Anuluj", null)
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(category.name)) {
                        new Thread(() -> {
                            category.name = newName;
                            AppDatabase.getInstance(context).applicationCategoryDao().update(category);
                            ((Activity) context).runOnUiThread(onUpdated);
                        }).start();
                    }
                })
                .show();
    }
}