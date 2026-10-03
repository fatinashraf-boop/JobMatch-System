package com.example.jobmatch_frontend.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "JobMatchSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FULL_NAME = "full_name";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createLoginSession(String token, int userId, String role, String fullName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, null);
    }

    public String getFullName() {
        return preferences.getString(KEY_FULL_NAME, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty() && getUserId() != -1;
    }

    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}