package pl.lbasista.magazynex.data.repo;

import java.util.List;

import pl.lbasista.magazynex.data.User;

public interface UserRepository {
    List<User> getAllUsers();
    User getById(int id);
    User getByLogin(String login);
    long insertUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int id);

    void fetchAllUsersFromApi();
}
