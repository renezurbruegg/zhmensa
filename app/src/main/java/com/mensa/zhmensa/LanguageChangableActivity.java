package com.mensa.zhmensa;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.mensa.zhmensa.services.LocaleManager;

public class LanguageChangableActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new LocaleManager(base).setLocale(base));
        Log.d("", "attachBaseContext");
    }

}
