package pl.lbasista.magazynex.ui.orders;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
    public interface OnOrderClick {void onClick(Order order);}
    private final List<Order> orderList;
    private final OnOrderClick listener;

    public OrderAdapter(List<Order> data, OnOrderClick l) {
        this.orderList = data;
        this.listener = l;
    }

    public void add(Order order) {
        orderList.add(order);
        notifyItemInserted(orderList.size() - 1);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_order, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Order o = orderList.get(i);
        h.name.setText(o.getName());
        h.qty.setText(o.getQuantity() + " produktów");
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderDetailsActivity.class);
            intent.putExtra("orderId", o.getId());
            intent.putExtra("orderName", o.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override public int getItemCount() { return orderList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, qty;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.textViewOrderName);
            qty  = v.findViewById(R.id.textViewOrderQuantity);
        }
    }
}
