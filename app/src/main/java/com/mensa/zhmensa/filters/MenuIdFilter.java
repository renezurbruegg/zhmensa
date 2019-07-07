package com.mensa.zhmensa.filters;

import com.mensa.zhmensa.models.IMenu;

public class MenuIdFilter implements  MenuFilter {

    private final String menuId;

    public MenuIdFilter(String menuId) {
        this.menuId = menuId;
    }
    @Override
    public boolean apply(IMenu menu) {
        return menu.getId().equals(menuId);
    }
}
