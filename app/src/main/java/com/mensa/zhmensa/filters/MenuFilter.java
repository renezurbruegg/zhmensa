package com.mensa.zhmensa.filters;

import com.mensa.zhmensa.models.IMenu;

public interface MenuFilter {
    public boolean apply(IMenu menu);
}
