package com.mensa.zhmensa.models;


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

    abstract Observable getMensaUpdateForDayAndMeal(Mensa.Weekday day, Mensa.MenuCategory menuCategory);

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public abstract List<MensaListObservable> loadMensasFromAPI();
}
