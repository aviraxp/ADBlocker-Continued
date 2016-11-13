package com.aviraxp.adblocker.continued.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.aviraxp.adblocker.continued.BuildConfig;
import com.aviraxp.adblocker.continued.R;

import java.lang.reflect.Field;
import java.util.Map;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

public class SettingsActivity extends PreferenceActivity {

    static boolean isActivated = false;

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.pref_general);
        checkState();
        donate();
        disableXposed();
    }

    private void checkState() {
        if (!isActivated) {
            showNotActive();
        }
    }

    private void showNotActive() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage(R.string.hint_reboot_not_active)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        openXposed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openXposed() {
        Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
        if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("section", "modules").putExtra("fragment", 1).putExtra("module", BuildConfig.APPLICATION_ID);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    private void donate() {
        findPreference("DONATE").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (AlipayZeroSdk.hasInstalledAlipayClient(getApplicationContext())) {
                    AlipayZeroSdk.startAlipayClient(SettingsActivity.this, "aex00388woilyb9ln32hlfe");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.donate_failed, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    private void disableXposed() {
        try {
            Class<?> clazz = Class.forName("de.robv.android.xposed.XposedBridge", false, ClassLoader.getSystemClassLoader());
            Field field = clazz.getDeclaredField("sHookedMethodCallbacks");
            field.setAccessible(true);
            Map sHookedMethodCallbacks = (Map) field.get(null);
            Object doNothing = Class.forName("de.robv.android.xposed.XC_MethodReplacement", false, clazz.getClassLoader()).getField("DO_NOTHING").get(null);
            for (Object callbacks : sHookedMethodCallbacks.values()) {
                field = callbacks.getClass().getDeclaredField("elements");
                field.setAccessible(true);
                Object[] elements = (Object[]) field.get(callbacks);
                for (int i = 0; i < elements.length; ++i) {
                    elements[i] = doNothing;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}