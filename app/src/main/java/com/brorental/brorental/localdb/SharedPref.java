package com.brorental.brorental.localdb;

import android.content.Context;
import android.content.SharedPreferences;

import com.brorental.brorental.models.User;

public class SharedPref {
    private SharedPreferences sharedPreferences;
    private String TAG = "SharedPreferences.java";

    public SharedPref(Context ctx) {
        this.sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    }

    public boolean isFirstTime() {
        return sharedPreferences.getBoolean("isFirstTime", true);
    }

    public void setFirstTime(boolean isFirstTime) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("isFirstTime", isFirstTime);
        edit.apply();
    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean("isLogin", false);
    }

    public void setLogin(boolean isLogin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", isLogin);
        editor.apply();
    }

    public User getUser() {
        return new User(sharedPreferences.getString("name", ""), sharedPreferences.getString("mobile", "")
        , sharedPreferences.getString("pin", ""), sharedPreferences.getString("totalRent", ""),
                sharedPreferences.getString("totalRide", ""), sharedPreferences.getBoolean("termsCheck", false)
        , sharedPreferences.getString("profileUrl", ""));
    }
    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", user.getName());
        editor.putString("mobile", user.getMobile());
        editor.putString("totalRent", user.getTotalRent());
        editor.putString("totalRide", user.getTotalRide());
        editor.putString("pin", user.getPin());
        editor.putBoolean("termsCheck", user.isTermsCheck());
        editor.putString("profileUrl", user.getProfileUrl());
        editor.apply();
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}
