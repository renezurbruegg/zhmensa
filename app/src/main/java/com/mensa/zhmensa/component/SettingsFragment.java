package com.mensa.zhmensa.component;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.mensa.zhmensa.MainActivity;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.SettingsActivity;
import com.mensa.zhmensa.services.LocaleManager;
import com.mensa.zhmensa.services.MensaManager;

import java.util.HashSet;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsActivity parentActivity;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_view);

        Preference button = findPreference(getString(R.string.myDummyButton));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putStringSet(MensaManager.DELETED_MENUS_STORE_ID, new HashSet<String>())
                        .apply();
                Snackbar.make(getView(), getString(R.string.msg_menus_deleted), Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference(getString(R.string.myClearCacheButton)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MensaManager.clearCache(getContext());
                return true;
            }
        });

        findPreference("language_preference").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                new LocaleManager(getContext()).setNewLocale(getContext(), newValue.toString());
                Log.d("change", "changed " + newValue);
                Log.d("", PreferenceManager.getDefaultSharedPreferences(getContext()).getString("language_preference", "non"));

                getActivity().finish();
                startActivity(getActivity().getIntent());
                /*Intent i = new Intent(getContext(), MainActivity.class);
                startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                System.exit(0); */
                return true;
            }
        });

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d("ocpf", rootKey + "");
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d("ocpf", "prefernce cahgned " + s);

/*        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        System.exit(0);*/
    }

    public void setListener(SettingsActivity settingsActivity) {
            parentActivity = settingsActivity;
    }
}