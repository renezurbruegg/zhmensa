package com.mensa.zhmensa.models;


import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Observable;


/**
 * Mensa Category. Defines the group that can be expanded in the navigation drawer.
 * E.G. UZH, ETH ...
 */
public abstract class MensaCategory {
    /**
     * Name that is displayed inside the drawer
     */
    private String displayName;

    public MensaCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public Integer getCategoryIconId() {
        return null;
    }
    public abstract List<MensaListObservable> loadMensasFromAPI();
}
