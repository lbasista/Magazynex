package pl.lbasista.magazynex.ui.user;

public class RoleChecker {
    public static final String ADMIN = "Administrator";
    public static final String WORKER = "Pracownik";
    public static final String VIEWER = "Przeglądający";

    //Sprawdzanie ról
    public static boolean isViewer(String role) {return VIEWER.equals(role != null ? role.trim() : "");}
    public static boolean isAdmin(String role) {return ADMIN.equals(role != null ? role.trim() : "");}
    public static boolean isWorker(String role) {return WORKER.equals(role != null ? role.trim() : "");}

    public static boolean isViewer(SessionManager session) {return isViewer(session.getUserRole());}
    public static boolean isAdmin(SessionManager session) {return isAdmin(session.getUserRole());}
    public static boolean isWorker(SessionManager session) {return isWorker(session.getUserRole());}
}
