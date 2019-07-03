package com.mensa.zhmensa.models;


/**
 * Mensa Category. Defines the group that can be expanded in the navigation drawer.
 * E.G. UZH, ETH ...
 */
public class MensaCategory {
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
}
