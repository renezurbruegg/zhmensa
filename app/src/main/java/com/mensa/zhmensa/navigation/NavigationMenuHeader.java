package com.mensa.zhmensa.navigation;

import com.mensa.zhmensa.models.MensaCategory;

public class NavigationMenuHeader {


    public MensaCategory category;

    public NavigationMenuHeader(MensaCategory category) {
        this.category = category;
    }

    public String getDisplayName() {
        return category.getDisplayName();
    }

}
