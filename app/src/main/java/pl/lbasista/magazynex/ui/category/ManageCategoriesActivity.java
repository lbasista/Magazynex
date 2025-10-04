package pl.lbasista.magazynex.ui.category;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import pl.lbasista.magazynex.R;
import pl.lbasista.magazynex.data.ApplicationCategory;
import pl.lbasista.magazynex.data.repo.CategoryRepository;
import pl.lbasista.magazynex.data.repo.RemoteCategoryRepository;
import pl.lbasista.magazynex.data.repo.RoomCategoryRepository;
import pl.lbasista.magazynex.ui.product.AddCategoryBottomSheet;
import pl.lbasista.magazynex.ui.user.SessionManager;

public class ManageCategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCategories;
    private MaterialToolbar toolbar;
    private CategoryRepository categoryRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        toolbar = findViewById(R.id.appBarCategories);
        toolbar.setNavigationOnClickListener(v -> finish());

        SessionManager session = new SessionManager(this);
        if (session.isRemoteMode()) categoryRepository = new RemoteCategoryRepository(this, session.getApiUrl());
        else categoryRepository = new RoomCategoryRepository(this);

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
            List<ApplicationCategory> categories = categoryRepository.getAllCategories();
            runOnUiThread(() -> recyclerViewCategories.setAdapter(new CategoryAdapter(categories, categoryRepository)));
        }).start();
    }
}