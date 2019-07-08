package com.mensa.zhmensa.models;


import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;


/**
 * Mensa Category. Defines the group that can be expanded in the navigation drawer.
 * E.G. UZH, ETH ...
 */
public abstract class MensaCategory {
    private final List<String> knownMensas;
    /**
     * Name that is displayed inside the drawer
     */
    private String displayName;

    public MensaCategory(String displayName) {
        this(displayName, Collections.<String>emptyList());
    }

    public MensaCategory(String catName, List<String> mensaIds) {
        this.displayName = catName;
        knownMensas = new ArrayList<>(mensaIds);
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

    public boolean containsMensa(Mensa mensa) {
        // Default. Needs to be overriden by subclass
        return knownMensas.contains(mensa.getUniqueId());
    }
}
