package pl.lbasista.magazynex.ui.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.Hold;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.ApplicationCategoryDao;
import pl.lbasista.magazynex.data.Product;
import pl.lbasista.magazynex.ui.user.RoleChecker;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final ProductViewModel viewModel;

    public List<Product> getProductList() {
        return productList;
    }

    public ProductAdapter(List<Product> productList, ProductViewModel viewModel) {
        this.productList = productList;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Tworzymy widok dla pojedynczego elementu listy
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        //Pobieramy produkt z listy
        Product product = productList.get(position);

        //Ustawiamy nazwę i ilość
        holder.textViewProductName.setText(product.name);
        holder.textViewProductProducent.setText(product.producer);
        holder.textViewProductQuantity.setText("Na stanie: " + String.valueOf(product.quantity));

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());
            int onList = db.orderProductDao().getTotalCountForProduct(product.id);
            int quantityLeft = product.quantity - onList;

            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                holder.textViewProductQuantityOnList.setText("Na liście: " + onList);
                holder.textViewProductQuantityLeft.setText("Wolnych: " + quantityLeft);
                int colorId = R.color.gray;
                if (quantityLeft == 0) colorId = R.color.pink;
                else if (quantityLeft < 0) colorId = R.color.red;
                holder.textViewProductQuantityLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorId));
            });
        }).start();

        //Kod kreskowy
        String barcode = product.barcode != null ? product.barcode : "";
        if (barcode.isEmpty()) {
            holder.textViewProductBarcode.setVisibility(View.GONE);
        } else {
            holder.textViewProductBarcode.setVisibility(View.VISIBLE);
            holder.textViewProductBarcode.setText(barcode);
        }

        //Kategoria
        if (product.applicationCategoryId != 0) {
            //Jeśli ma
            new Thread(() -> {
                ApplicationCategoryDao dao = AppDatabase.getInstance(holder.itemView.getContext()).applicationCategoryDao();

                ApplicationCategory category = dao.getById(product.applicationCategoryId);
                if (category != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        holder.textViewProductApplication.setText(category.name);
                    });
                }
            }).start();
        } else {
            holder.textViewProductApplication.setVisibility(View.GONE);
        }

        //Wyświetlanie zdjęcia
        if (product.imageUri != null && !product.imageUri.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                            .load(Uri.parse(product.imageUri))
                                    .placeholder(R.drawable.ic_no_image)
                                            .error(R.drawable.ic_no_image)
                                                    .into(holder.imageViewProduct);
            holder.imageViewProduct.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewProduct.setVisibility(View.GONE);
        }

        if (RoleChecker.isViewer(new SessionManager(holder.itemView.getContext()))) holder.textFavourite.setVisibility(View.GONE);

        //Zmiana koloru gwiazdki
        int starColorRes = product.favourite ? R.color.gold : R.color.light_gray;
        holder.textFavourite.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), starColorRes)
        );

        //Po kliknięciu gwiazdki
        holder.textFavourite.setOnClickListener(v -> viewModel.toggleFavourite(product));

        //Kliknięcie pozycji na liście
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailsActivity.class);
            intent.putExtra("name", product.name);
            intent.putExtra("producer", product.producer);
            intent.putExtra("barcode", product.barcode);
            intent.putExtra("quantity", product.quantity);
            intent.putExtra("description", product.description);
            intent.putExtra("imageUri", product.imageUri);
            Activity activity = (Activity) v.getContext();
            activity.startActivityForResult(intent, 123);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductBarcode, textViewProductName, textViewProductProducent, textViewProductQuantity, textViewProductQuantityOnList, textViewProductQuantityLeft, textFavourite, textViewProductApplication;
        ImageView imageViewProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductBarcode = itemView.findViewById(R.id.textViewProductBarcode);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductProducent = itemView.findViewById(R.id.textViewProductProducent);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            textViewProductQuantityOnList = itemView.findViewById(R.id.textViewProductQuantityOnList);
            textViewProductQuantityLeft = itemView.findViewById(R.id.textViewProductQuantityLeft);
            textFavourite = itemView.findViewById(R.id.textFavourite);
            imageViewProduct = itemView.findViewById(R.id.imageProduct);
            textViewProductApplication = itemView.findViewById(R.id.textViewProductApplication);
        }
    }
}