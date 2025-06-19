package com.alyazin.quran;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.alyazin.tafsir.TafsirActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "QuranPrefs";
    private static final String KEY_LANGUAGE = "AppLanguage";
    private static final String KEY_DARK_MODE = "DarkMode";

    private FloatingActionButton fabMain, settingsButton, themeToggleButton;
    private boolean isMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        setLocale(getSavedLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button quranButton = findViewById(R.id.quranButton);
        Button tafsirButton = findViewById(R.id.tafsirButton);
        Button duaaButton = findViewById(R.id.duaaButton);

        if (quranButton != null)
            quranButton.setOnClickListener(v -> startActivity(new Intent(this, QuranActivity.class)));

        if (tafsirButton != null)
            tafsirButton.setOnClickListener(v -> startActivity(new Intent(this, TafsirActivity.class)));

        if (duaaButton != null)
            duaaButton.setOnClickListener(v -> startActivity(new Intent(this, DuaaActivity.class)));

        fabMain = findViewById(R.id.fabMain);
        settingsButton = findViewById(R.id.settingsButton);
        themeToggleButton = findViewById(R.id.themeToggleButton);

        if (fabMain == null || settingsButton == null || themeToggleButton == null) {
            Log.e("MainActivity", "One of the FABs is null");
            return;
        }

        settingsButton.setVisibility(View.GONE);
        themeToggleButton.setVisibility(View.GONE);
        settingsButton.setAlpha(0f);
        themeToggleButton.setAlpha(0f);

        fabMain.setOnClickListener(v -> {
            if (isMenuOpen) closeMenu();
            else openMenu();
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            closeMenu();
        });

        themeToggleButton.setOnClickListener(v -> {
            toggleTheme();
            closeMenu();
        });
    }

    private void openMenu() {
        settingsButton.setVisibility(View.VISIBLE);
        themeToggleButton.setVisibility(View.VISIBLE);

        settingsButton.setTranslationY(100f);
        themeToggleButton.setTranslationY(200f);

        settingsButton.animate().alpha(1f).translationY(0f).setDuration(200).start();
        themeToggleButton.animate().alpha(1f).translationY(0f).setDuration(200).start();

        fabMain.animate().rotation(45f).setDuration(200).start();
        isMenuOpen = true;
    }

    private void closeMenu() {
        settingsButton.animate().alpha(0f).translationY(100f).setDuration(200)
                .withEndAction(() -> settingsButton.setVisibility(View.GONE)).start();

        themeToggleButton.animate().alpha(0f).translationY(200f).setDuration(200)
                .withEndAction(() -> themeToggleButton.setVisibility(View.GONE)).start();

        fabMain.animate().rotation(0f).setDuration(200).start();
        isMenuOpen = false;
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        boolean newDark = !isDark;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_DARK_MODE, newDark);
        editor.apply();

        AppCompatDelegate.setDefaultNightMode(
                newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        recreate();
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private String getSavedLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "ar");
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
