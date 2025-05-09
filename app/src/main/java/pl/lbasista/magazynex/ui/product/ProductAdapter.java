package pl.lbasista.magazynex.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final ProductViewModel viewModel;

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
        holder.textViewProductDetails.setText(product.producer + " • Na stanie: " + product.quantity);

        //Zmiana koloru gwiazdki
        int starColorRes = product.favourite ? R.color.gold : R.color.light_gray;
        holder.textFavourite.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), starColorRes)
        );

        //Po kliknięciu gwiazdki
        holder.textFavourite.setOnClickListener(v -> viewModel.toggleFavourite(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName, textViewProductDetails, textFavourite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDetails = itemView.findViewById(R.id.textViewProductDetails);
            textFavourite = itemView.findViewById(R.id.textFavourite);
        }
    }
}