package io.moneytise.data;

import android.content.Context;
import android.content.SharedPreferences;

import io.moneytise.R;

public class DataStore {

    private static DataStore instance;

    private final Context context;

    public DataStore(Context context) {
        this.context = context;
    }

    public static synchronized DataStore getInstance(Context context) {
        if (instance == null) {
            instance = new DataStore(context);
        }
        return instance;
    }

    public boolean contains(String key) {
        return pref().contains(key);
    }

    public boolean is(String key) {
        return pref().getBoolean(key, false);
    }

    public boolean has(String key) {
        return pref().contains(key);
    }

    public String get(String key) {
        return pref().getString(key, null);
    }

    public void set(String key, String value) {
        pref().edit().putString(key, value).apply();
    }

    public void set(String key, boolean value) {
        pref().edit().putBoolean(key, value).apply();
    }


    private SharedPreferences pref() {
        return context.getSharedPreferences(context.getString(R.string.moneytiser_preference_file_key), Context.MODE_PRIVATE);
    }

}
