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

import java.util.ArrayList;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.data.repo.CategoryRepository;
import pl.lbasista.magazynex.data.repo.RemoteProductRepository;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<ApplicationCategory> categoryList;
    private final CategoryRepository categoryRepository;

    public CategoryAdapter(List<ApplicationCategory> categoryList, CategoryRepository categoryRepository) {
        this.categoryList = categoryList;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        ApplicationCategory category = categoryList.get(position);
        holder.textCategoryName.setText(category.name);
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            SessionManager session = new SessionManager(context);

            new Thread(() -> {
                List<Product> products;

                if (session.isRemoteMode()) {
                    RemoteProductRepository repository = new RemoteProductRepository(context, session.getApiUrl());
                    List<Product> allProducts = repository.getAllProductsSync();
                    products = new ArrayList<>();
                    if (allProducts != null) {
                        for (Product p : allProducts) {
                            if (p != null && p.applicationCategoryId == category.id) products.add(p);
                        }
                    }
                } else {
                    products = AppDatabase.getInstance(context).productDao().getByCategoryId(category.id);
                }

                StringBuilder message = new StringBuilder();
                if (products == null || products.isEmpty()) {
                    message.append("Wybrana kategoria jest pusta.");
                } else {
                    for (Product p : products) {
                        message.append("• ").append(p.name).append(" (").append(p.producer).append(")\n");
                    }
                }

                ((Activity) context).runOnUiThread(() -> {
                    new AlertDialog.Builder(context)
                            .setTitle(category.name)
                            .setMessage("Produkty na liście:\n" + message)
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
                            boolean ok = categoryRepository.updateCategory(category);
                            ((Activity) context).runOnUiThread(() -> {
                                if (ok) onUpdated.run();
                                else Toast.makeText(context, "Błąd zapisu kategorii", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    }
                })
                .show();
    }
}