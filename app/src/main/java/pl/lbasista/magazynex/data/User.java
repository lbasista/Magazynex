package pl.lbasista.magazynex.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String surname;
    public String login;
    public String password;
    public String role;

    public String getFullName() {
        return name + " " + surname;
    }

    //Role użytkowników
    public static final String ROLE_ADMIN = "administrator";
    public static final String ROLE_WORKER = "pracownik";
    public static final String ROLE_VIEWER = "przeglądajacy";
}
