package pl.lbasista.magazynex.data.repo;

import java.util.List;

import pl.lbasista.magazynex.data.ApplicationCategory;

public interface CategoryRepository {
    List<ApplicationCategory> getAllCategories();
    ApplicationCategory getById(int id);
    long insertCategory(ApplicationCategory category);
    boolean updateCategory(ApplicationCategory category);
}
