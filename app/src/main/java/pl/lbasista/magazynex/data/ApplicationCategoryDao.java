package pl.lbasista.magazynex.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApplicationCategoryDao {
    //Nowe zastosowanie (ignore - brak duplikatów)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ApplicationCategory category);

    //Zwracanie alfabetycznie zastosowań
    @Query("SELECT * FROM ApplicationCategory ORDER BY name ASC")
    LiveData<List<ApplicationCategory>> getAll();

    //Zwracanie po ID
    @Query("SELECT * FROM ApplicationCategory WHERE id = :id LIMIT 1")
    ApplicationCategory getById(int id);
}