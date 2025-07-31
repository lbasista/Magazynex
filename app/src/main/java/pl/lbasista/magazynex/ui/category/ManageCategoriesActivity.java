package pl.lbasista.magazynex.ui.category;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.ui.product.AddCategoryBottomSheet;

public class ManageCategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCategories;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.appBarCategories);
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.addObject) {
                AddCategoryBottomSheet sheet = new AddCategoryBottomSheet();
                sheet.setOnCategoryAddedListener(() -> loadCategories());
                sheet.show(getSupportFragmentManager(), "AddCategorySheet");
                return true;
            }
            return false;
        });
        loadCategories();
    }

    private void loadCategories() {
        new Thread(() -> {
            List<ApplicationCategory> categories = AppDatabase.getInstance(this).applicationCategoryDao().getAllSync();
            runOnUiThread(() -> recyclerViewCategories.setAdapter(new CategoryAdapter(categories)));
        }).start();
    }
}
