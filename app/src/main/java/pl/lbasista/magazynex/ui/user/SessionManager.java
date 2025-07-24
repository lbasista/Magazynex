package pl.lbasista.magazynex.ui.user;

import static android.provider.Settings.System.putInt;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences preferences;
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";

    public SessionManager(Context context) {this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);}

    public void saveUserSession(int userId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {return preferences.getInt(KEY_USER_ID, -1);}

    void clearSession() {preferences.edit().clear().apply();}
}
