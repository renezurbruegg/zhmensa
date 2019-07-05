package com.mensa.zhmensa.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that defines a mensa.
 * The display Name will be displayed as title and in the navigation drawer.
 */
public class Mensa {

    private String displayName;
    private List<IMenu> menus = new ArrayList<>();

    public Mensa(String displayName) {
        this.displayName = displayName;
    }

    public Mensa(String displayName, List<IMenu> menus) {
        this.displayName = displayName;
        this.menus.addAll(menus);
    }
    /**
     *
     * @return a list with all menus currently server by this mensa
     */
    public List<IMenu> getMenus() {
        return menus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
