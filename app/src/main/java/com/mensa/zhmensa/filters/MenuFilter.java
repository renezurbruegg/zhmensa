package com.mensa.zhmensa.filters;

import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;

/**
 * Filter interface for a Menu object
 */
public interface MenuFilter {

   public boolean apply(IMenu menu);
}
