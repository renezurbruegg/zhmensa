package com.mensa.zhmensa.navigation;

import androidx.annotation.Nullable;

import com.mensa.zhmensa.models.Mensa;

public class NavigationMenuChild implements  Comparable<NavigationMenuChild>{


    public Mensa mensa;

    public NavigationMenuChild(Mensa mensa) {
        this.mensa = mensa;
    }

    public String getDisplayName() {
        return mensa.getDisplayName();
    }

    @Override
    public int compareTo(@Nullable NavigationMenuChild navigationMenuChild) {
        if(navigationMenuChild == null)
            return 1;
        return mensa.compareTo(navigationMenuChild.mensa);
    }
}
