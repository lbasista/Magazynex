package pl.lbasista.magazynex.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Product.class, Order.class, OrderProduct.class, ApplicationCategory.class, User.class}, version = 16, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderProductDao orderProductDao();
    public abstract ApplicationCategoryDao applicationCategoryDao();
    public abstract UserDao userDao();

    private static AppDatabase instance;

    static final Migration MIGRATION_ANY_TO_16 = new Migration(1, 16) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            //Nowa baza danych
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "magazynex_db")
                    .addMigrations(MIGRATION_ANY_TO_16)
                    .build();
        }
        return instance;
    }
}