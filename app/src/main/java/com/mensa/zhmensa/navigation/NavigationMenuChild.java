package com.mensa.zhmensa.navigation;

import com.mensa.zhmensa.models.Mensa;

public class NavigationMenuChild {


    public Mensa mensa;

    public NavigationMenuChild(Mensa mensa) {
        this.mensa = mensa;
    }

    public String getDisplayName() {
        return mensa.getDisplayName();
    }
}
