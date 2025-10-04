package pl.lbasista.magazynex.data.repo;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.ApplicationCategory;

public class RoomCategoryRepository implements CategoryRepository {
    private final Context context;

    public RoomCategoryRepository(Context context) {this.context = context.getApplicationContext();}

    @Override
    public List<ApplicationCategory> getAllCategories() {return AppDatabase.getInstance(context).applicationCategoryDao().getAllSync();}

    @Override
    public ApplicationCategory getById(int id) {return AppDatabase.getInstance(context).applicationCategoryDao().getById(id);}

    @Override
    public long insertCategory(ApplicationCategory category) {return AppDatabase.getInstance(context).applicationCategoryDao().insert(category);}

    @Override
    public boolean updateCategory(ApplicationCategory category) {
        try {
            AppDatabase.getInstance(context).applicationCategoryDao().update(category);
            return true;
        } catch (Throwable t) {return false;}
    }
}
