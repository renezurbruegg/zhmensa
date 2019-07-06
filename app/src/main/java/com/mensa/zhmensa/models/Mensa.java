package com.mensa.zhmensa.models;

import com.mensa.zhmensa.services.MensaManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class that defines a mensa.
 * The display Name will be displayed as title and in the navigation drawer.
 */
public class Mensa {

    private String displayName;
    private List<IMenu> menus = new ArrayList<>();
    private final String mensaId;

    public Mensa(String displayName) {
        this(displayName, Collections.<IMenu>emptyList());
    }

    public Mensa(String displayName, List<IMenu> menus) {
        this.displayName = displayName;
        this.menus.addAll(menus);
        mensaId = MensaManager.getUniqueIdForMensa(this);
    }
    /**
     *
     * @return a list with all menus currently server by this mensa
     */
    public List<IMenu> getMenusForDayAndCategory(Weekday weekday, MenuCategory category) {
        return menus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUniqueId() {
        return mensaId;
    }


    public enum MenuCategory {
        LUNCH, DINNER
    }
    public enum Weekday {
        MONDAY,TUESDAY,WEDNESDAY, THURSDAY, FRIDAY
    }

}
