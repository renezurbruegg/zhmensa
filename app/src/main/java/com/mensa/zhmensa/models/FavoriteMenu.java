package com.mensa.zhmensa.models;

public class FavoriteMenu implements IMenu {
    IMenu menu;
    Mensa mensa;

    public FavoriteMenu(IMenu menu, Mensa mensa) {
        this.mensa = mensa;
        this.menu = menu;
    }

    @Override
    public String getName() {
        return menu.getName();
    }

    @Override
    public String getDescription() {
        return menu.getDescription();
    }

    @Override
    public String getPrices() {
        return mensa.getDisplayName() + " : " + menu.getPrices();
    }

    @Override
    public String getAllergene() {
        return menu.getAllergene();
    }
}
