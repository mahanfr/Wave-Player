package com.hafniumsoftware.diapason;

import android.content.Context;
import android.content.SharedPreferences;


public class SettingsUtil {
    private final String STORAGE = " com.hafniumsoftware.Diapason.audioplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public SettingsUtil(Context context){
        this.context = context;
    }

    public boolean isPhonePermissionGained() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("PhonePermission",false);
    }

    public boolean isSotragePermissionGained() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("StoragePermission",false);
    }

    public void setPhonePermission(boolean phonePermission) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("PhonePermission",phonePermission);
        editor.apply();
    }

    public void setSotragePermission(boolean storagePermission) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("StoragePermission",storagePermission);
        editor.apply();
    }
}
