package com.mensa.zhmensa.filters;

import com.mensa.zhmensa.models.menu.IMenu;

/**
 * Filter interface for a Menu object
 */
public interface MenuFilter {

   boolean apply(IMenu menu);
}
