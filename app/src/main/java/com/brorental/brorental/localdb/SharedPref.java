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
        , sharedPreferences.getString("profileUrl", ""), sharedPreferences.getString("wallet", ""));
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
        editor.putString("wallet", user.getWallet());
        editor.apply();
    }

    public void setWallet(String wallet) {
        sharedPreferences.edit().putString("wallet", wallet).apply();
    }

    public void setState(String state) {
        sharedPreferences.edit().putString("state", state).apply();
    }

    public String getState() {
        return sharedPreferences.getString("state", "");
    }

    public String getAlternateMob() {
        return sharedPreferences.getString("alternateMob", "");
    }

    public void setAlternateMob(String mob) {
        sharedPreferences.edit().putString("alternateMOb", mob).apply();
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}
