package com.mensa.zhmensa.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
    private final String displayName;
    private Long lastUpdated;
    private int position;

    public MensaCategory(String displayName, int position) {
        this(displayName, Collections.<String>emptyList(), position);
    }

    public MensaCategory(@Nullable String catName, @NonNull List<String> mensaIds, int position) {
        this.displayName = Helper.firstNonNull(catName,"");
        knownMensas = new ArrayList<>(mensaIds);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public Integer getCategoryIconId() {
        return R.drawable.ic_eth_2;
    }

    @NonNull
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


    @Override
    public int hashCode() {
        return getDisplayName().hashCode();
    }

    @Nullable
    public Long lastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
