package pl.lbasista.magazynex.data;

import android.provider.ContactsContract;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM Product WHERE name = :name AND producer = :producer LIMIT 1")
    Product getByNameAndProducer(String name, String producer); //Sprawdź czy jest w bazie

    @Update
    void update(Product product);

    @Insert
    void insert(Product product);

    @Query("SELECT * FROM Product")
    LiveData<List<Product>> getAll();

    @Query("SELECT * FROM Product WHERE favourite = 1")
    LiveData<List<Product>> getFavourites();

    @Query("UPDATE Product SET favourite = :fav WHERE id = :id")
    void updateFavourite(int id, boolean fav);

    //Szukaj po kodzie
    @Query("SELECT * FROM Product WHERE barcode = :barcode LIMIT 1")
    Product getByBarcode(String barcode);
}