package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static UserSession instance;

    private String userName;
    private String password;
    private String privileges;

    // Private constructor to prevent instantiation from outside the class
    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        savePreferences();
    }

    // Public method to get an instance of UserSession in a thread-safe manner
    public static synchronized UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            instance = new UserSession(userName, password, privileges);
        }
        return instance;
    }

    // Overloaded method for simplicity
    public static synchronized UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    // Save user preferences in a thread-safe manner
    private synchronized void savePreferences() {
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    public synchronized String getUserName() {
        return this.userName;
    }

    public synchronized String getPassword() {
        return this.password;
    }

    public synchronized String getPrivileges() {
        return this.privileges;
    }

    // Make cleanUserSession method synchronized
    public synchronized void cleanUserSession() {
        this.userName = "";
        this.password = "";
        this.privileges = "";
        savePreferences();
    }

    @Override
    public synchronized String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges=" + this.privileges +
                '}';
    }
}
