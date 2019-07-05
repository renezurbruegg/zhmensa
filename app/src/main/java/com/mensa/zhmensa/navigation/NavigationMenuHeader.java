package com.mensa.zhmensa.navigation;

import com.mensa.zhmensa.models.MensaCategory;

public class NavigationMenuHeader {


    public MensaCategory category;
    private boolean hasChildren;
    public NavigationMenuHeader(MensaCategory category) {
        this(category, true);
    }

    public NavigationMenuHeader(MensaCategory category, boolean hasChildren) {
        this.category = category;
        this.hasChildren = hasChildren;
    }

    public String getDisplayName() {
        return category.getDisplayName();
    }

    public boolean hasChildren(){
        return hasChildren;
    }

}
