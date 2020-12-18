package me.xurround.remotecontrol;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings
{
    private static String SHARED_PREFERENCES = "RemoteControlPrefs";

    public static String IP_ADDRESS = "192.168.1.11";

    public static void loadSettings(Context context)
    {
        IP_ADDRESS = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).getString("ipAddress", IP_ADDRESS);
    }

    public static void saveSettings(Context context, String ipAddress)
    {
        IP_ADDRESS = ipAddress;
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString("ipAddress", IP_ADDRESS);
        editor.apply();
    }
}