package com.mensa.zhmensa.models;


import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;


/**
 * Mensa Category. Defines the group that can be expanded in the navigation drawer.
 * E.G. UZH, ETH ...
 */
public abstract class MensaCategory {
    @NonNull
    private final List<String> knownMensas;
    /**
     * Name that is displayed inside the drawer
     */
    @NonNull
    private String displayName;

    public MensaCategory(String displayName) {
        this(displayName, Collections.<String>emptyList());
    }

    public MensaCategory(String catName, List<String> mensaIds) {
        this.displayName = Helper.firstNonNull(catName,"");
        knownMensas = new ArrayList<>(mensaIds);
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public Integer getCategoryIconId() {
        return R.drawable.ic_eth_2;
    }

    public abstract List<MensaListObservable> loadMensasFromAPI();

    public boolean containsMensa(@Nullable Mensa mensa) {
        if(mensa == null)
            return false;
        // Default. Needs to be overriden by subclass
        return knownMensas.contains(mensa.getUniqueId());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  MensaCategory)
            return getDisplayName().equals(((MensaCategory)obj).getDisplayName());
        return false;
    }
}
