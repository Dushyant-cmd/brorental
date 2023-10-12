package com.brorental.brorental.localdb;

import android.content.Context;
import android.content.SharedPreferences;

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
}
