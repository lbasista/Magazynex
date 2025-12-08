package pl.lbasista.magazynex.data.repo;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import pl.lbasista.magazynex.data.AppDatabase;
import pl.lbasista.magazynex.data.User;
import pl.lbasista.magazynex.data.UserDao;

public class RoomUserRepository implements UserRepository {
    private final UserDao userDao;

    public RoomUserRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public User getById(int id) {
        return userDao.getById(id);
    }

    @Override
    public User getByLogin(String login) {
        return userDao.getByLogin(login);
    }

    @Override
    public long insertUser(User user) {
        return userDao.insert(user);
    }

    @Override
    public boolean updateUser(User user) {
        try {
            userDao.updateUser(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteUser(int id) {
        User user = getById(id);
        if (user == null) return false;
        try {
            userDao.deleteUser(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void fetchAllUsersFromApi() {}
}
