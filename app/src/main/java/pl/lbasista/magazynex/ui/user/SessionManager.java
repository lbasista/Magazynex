package pl.lbasista.magazynex.ui.user;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences preferences;
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";

    public SessionManager(Context context) {this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);}

    public void saveUserSession(int userId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {return preferences.getInt(KEY_USER_ID, -1);}
    void clearSession() {preferences.edit().clear().apply();}

    public void saveUserRole(String role) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_ROLE, role).apply();
    }

    public String getUserRole() {return preferences.getString(KEY_USER_ROLE, "Przeglądający");}
    public void setRemoteMode(boolean isRemote) {preferences.edit().putBoolean("REMOTE_MODE", isRemote).apply();}
    public boolean isRemoteMode() {return preferences.getBoolean("REMOTE_MODE", false);}
    public void setApiUrl(String url) {preferences.edit().putString("API_URL", url).apply();}
    public String getApiUrl() {return preferences.getString("API_URL", null);}
}