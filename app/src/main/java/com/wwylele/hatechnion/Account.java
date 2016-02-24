package com.wwylele.hatechnion;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// FIXME : currently store the password in plain text!
public class Account {
    static void set(Context context, String username, String password) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    static String getUsername(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context).getString("username", "");
    }

    static String getPassword(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context).getString("password", "");
    }
}
