package com.mensa.zhmensa.models;

import android.util.Log;

import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.services.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class that defines a mensa.
 * The display Name will be displayed as title and in the navigation drawer.
 */
public class Mensa implements Comparable<Mensa> {

    private String displayName;
    private List<IMenu> menus = new ArrayList<>();
    private final String mensaId;
    final Map<Weekday, Map<MenuCategory, Set<IMenu>>> meals;
    private MensaCategory category;

    public Mensa(String displayName, String id) {
        meals = new HashMap<>();
        this.mensaId = id;

        // Convert first character to upper case
        if(displayName.length() > 0 && Character.isLowerCase(displayName.charAt(0))) {
            displayName = Character.toUpperCase(displayName.charAt(0)) + displayName.substring(1);
        }
        this.displayName = displayName;
        this.menus.addAll(menus);

    }

    public void setMensaCategory(MensaCategory category) {
        this.category = category;
    }

    public MensaCategory getCategory(){
        return this.category;
    }

    public void setMenuForDayAndCategory(Weekday weekday, MenuCategory category, List<IMenu> menus) {
        Log.d("Adding menus " + getDisplayName(), "cat " + String.valueOf(category) + " menus: " + menus) ;

        Map<MenuCategory,Set<IMenu>> map = Helper.firstNonNull(meals.get(weekday), new HashMap<MenuCategory, Set<IMenu>>());

        //Map<MenuCategory,List<IMenu>> map = new HashMap<>();
        map.put(category, new HashSet<IMenu>(menus));

        meals.put(weekday,map);
    }
    public void addMenuForDayAndCategory(Weekday weekday, MenuCategory category, List<IMenu> menus) {
        Log.d("Adding menus " + getDisplayName(), "cat " + String.valueOf(category) + " menus: " + menus) ;

        Map<MenuCategory,Set<IMenu>> map = Helper.firstNonNull(meals.get(weekday), new HashMap<MenuCategory, Set<IMenu>>());

        //Map<MenuCategory,List<IMenu>> map = new HashMap<>();

        Set<IMenu> storedMenus = Helper.firstNonNull(map.get(category), new HashSet<IMenu>());

        Set<IMenu> set = new HashSet<>(storedMenus);

        for (IMenu menu : menus) {
            set.add(menu);
        }

        storedMenus.addAll(set);
        map.put(category, set);

        meals.put(weekday,map);
    }
    /**
     *
     * @return a list with all menus currently server by this mensa. Returns never null, but empty list if nothing is found
     */
    public List<IMenu> getMenusForDayAndCategory(Weekday weekday, MenuCategory category) {
        Map<MenuCategory, Set<IMenu>> map = Helper.firstNonNull(meals.get(weekday), Collections.<MenuCategory, Set<IMenu>>emptyMap());
        List<IMenu> returnList = new ArrayList<>();
        returnList.addAll(Helper.firstNonNull(map.get(category), Collections.<IMenu>emptyList()));
        Collections.sort(returnList);
        return returnList;
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

    @Override
    public int compareTo(Mensa m) {
        return Helper.firstNonNull(getDisplayName(), "").toLowerCase().compareTo(Helper.firstNonNull((m.getDisplayName()),"").toLowerCase());
    }


    public enum MenuCategory {
        LUNCH, DINNER
    }
    public enum Weekday {
        MONDAY(0),TUESDAY(1),WEDNESDAY(2), THURSDAY(3), FRIDAY(4);

        public final int day;
        Weekday(int day){
            this.day = day;
        }
    }

    public String toString() {
        String ret =  "Name: " + getDisplayName() + " Id: " + getUniqueId() + " \n ";
        return ret + meals.toString();
    }

    public List<IMenu> getMenusForDayAndCategory(Weekday weekday, MenuCategory category, MenuFilter filter) {
        Map<MenuCategory, Set<IMenu>> map = Helper.firstNonNull(meals.get(weekday), Collections.<MenuCategory, Set<IMenu>>emptyMap());

        List<IMenu> returnList = new ArrayList<>();
        for (IMenu menu: Helper.<Set<IMenu>>firstNonNull(map.get(category), Collections.<IMenu>emptySet())) {
            if(filter.apply(menu))
                returnList.add(menu);
        }
        return returnList;
    }

}
