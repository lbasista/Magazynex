package pl.lbasista.magazynex.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    User getByLogin(String login);

    @Query("SELECT * FROM users WHERE id = :id")
    User getById(int id);

    @Delete
    void deleteUser(User user);
}