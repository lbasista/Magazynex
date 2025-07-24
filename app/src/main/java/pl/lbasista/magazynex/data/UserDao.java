package pl.lbasista.magazynex.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE login = :login AND password = :password LIMIT 1")
    User getLogin(String login, String password);

    @Query("SELECT * FROM users WHERE id = :id")
    User getById(int id);
}