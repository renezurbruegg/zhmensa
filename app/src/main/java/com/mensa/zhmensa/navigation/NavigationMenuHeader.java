package com.mensa.zhmensa.navigation;

import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.services.Helper;

public class NavigationMenuHeader implements  Comparable<NavigationMenuHeader>{


    public MensaCategory category;
    private boolean hasChildren;
    private Integer position;

    public NavigationMenuHeader(MensaCategory category, int position) {
        this(category, true, position);
    }

    public NavigationMenuHeader(MensaCategory category, boolean hasChildren, int position) {
        this.category = category;
        this.hasChildren = hasChildren;
        this.position = position;
    }

    public String getDisplayName() {
        return category.getDisplayName();
    }

    public boolean hasChildren(){
        return hasChildren;
    }

    @Override
    public String toString() {
        return "Cat: " + (category == null ? "null" : category.getDisplayName())  + " - hasChildren: " + hasChildren;
    }

    @Override
    public int compareTo(NavigationMenuHeader o) {
       return Helper.firstNonNull(position,new Integer(-1)).compareTo(o.position);
    }
}
