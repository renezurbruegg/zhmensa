package com.mensa.zhmensa.models;

/**
 * Implementation for a menu in the favorite tab.
 * Get name returns not only it's name but also appends the mensa name
 */
public class FavoriteMenu extends Menu {
    private final String mensaName;

    public FavoriteMenu(String mensaName, IMenu menu) {
        super(menu.getId(), menu.getName(), menu.getDescription(), menu.getPrices(), menu.getAllergene(), menu.getMeta());
        this.mensaName = mensaName;
    }

    public String getName() {
        return mensaName + ": " + super.getName();
    }

}
