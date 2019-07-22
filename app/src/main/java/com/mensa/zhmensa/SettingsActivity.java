package com.mensa.zhmensa;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.mensa.zhmensa.component.SettingsFragment;
import com.mensa.zhmensa.services.MensaManager;

public class SettingsActivity extends LanguageChangableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment frag = new SettingsFragment();
        frag.setListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, frag)
                .commit();
        getSupportActionBar().setTitle(getString(R.string.settings));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MensaManager.clearState();
                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}