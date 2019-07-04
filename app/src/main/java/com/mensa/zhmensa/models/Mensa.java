package com.mensa.zhmensa.models;

import java.util.List;

/**
 * Abstract class that defines a mensa.
 * The display Name will be displayed as title and in the navigation drawer.
 */
public abstract class Mensa {

    private String displayName;

    public Mensa(String displayName) {
        this.displayName = displayName;
    }
    /**
     *
     * @return a list with all menus currently server by this mensa
     */
    abstract public List<IMenu> getMenus();

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
