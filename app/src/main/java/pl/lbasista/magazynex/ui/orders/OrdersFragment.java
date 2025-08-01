package pl.lbasista.magazynex.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.Order;
import pl.lbasista.magazynex.ui.user.RoleChecker;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class OrdersFragment extends Fragment {
    private FloatingActionButton buttonAddOrder;
    private RecyclerView recyclerView;
    private TextView textEmpty;
    private OrderAdapter adapter;
    private final ArrayList<Order> orderLists = new ArrayList<>();

    @NonNull @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle bs) {
        return inf.inflate(R.layout.activity_orders, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        textEmpty = view.findViewById(R.id.textViewEmptyOrder);
        buttonAddOrder = view.findViewById(R.id.buttonAddOrder);

        //Ukryj przycisk dla przeglądającego
        if (RoleChecker.isViewer(new SessionManager(requireContext()))) buttonAddOrder.setVisibility(View.GONE);

        view.post(() -> { //Pozycja przycisku nad menu
            View menuBar = requireActivity().findViewById(R.id.bottom_navigation);
            if (menuBar != null) {
                int menuHeight = menuBar.getHeight();
                int extraSpacing = (int) getResources().getDisplayMetrics().density * 16;
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) buttonAddOrder.getLayoutParams();
                lp.bottomMargin = menuHeight + extraSpacing;
                buttonAddOrder.setLayoutParams(lp);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(orderLists, order -> Toast.makeText(requireContext(), "Kliknąłeś: " + order.getName(), Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);

        loadOrdersFromDb();

        buttonAddOrder.setOnClickListener(v -> {
            AddOrderBottomSheet sheet = new AddOrderBottomSheet();
            sheet.setOnOrderAddedListener(newOrder -> {
                // dodajemy do lokalnej listy i informujemy adaptera
                orderLists.add(newOrder);
                adapter.notifyItemInserted(orderLists.size() - 1);
                updateUI();
            });
            sheet.show(getChildFragmentManager(), "addOrder");
        });
    }

    private void loadOrdersFromDb() {
        new Thread(() -> {
            List<Order> fromDb = AppDatabase.getInstance(requireContext())
                    .orderDao()
                    .getAllOrders();
            requireActivity().runOnUiThread(() -> {
                orderLists.clear();
                orderLists.addAll(fromDb);
                adapter.notifyDataSetChanged();
                updateUI();
            });
        }).start();
    }

    private void updateUI() {
        boolean empty = orderLists.isEmpty();
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE  : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrdersFromDb();
    }
}