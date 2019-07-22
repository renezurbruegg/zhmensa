package com.mensa.zhmensa.models;

import android.content.Context;

import androidx.annotation.Nullable;

import com.mensa.zhmensa.services.MensaManager;

/**
 * Implementation for a menu in the favorite tab.
 * Get name returns not only it's name but also appends the mensa name
 */
public class FavoriteMenu extends Menu {
    private final String mensaName;
    private final IMenu menu;

    public FavoriteMenu(String mensaName, IMenu menu) {
        super(menu.getId(), menu.getName(), menu.getDescription(), menu.getPrices(), "", menu.getMeta());
        this.menu = menu;
        this.mensaName = mensaName;
    }

    @Override
    public boolean isVegi() {
        return menu.isVegi();
    }

    @Nullable
    @Override
    public String getAllergene(Context ctx) {
        return menu.getAllergene(ctx);
    }

    @Override
    public boolean hasAllergene() {
        return menu.hasAllergene();
    }

    @Override
    void setAllergene(@Nullable String allergene) {
        if(menu instanceof Menu)
            ((Menu)menu).setAllergene(allergene);
    }

    public String getName() {
        return mensaName + ": " + super.getName();
    }

}
