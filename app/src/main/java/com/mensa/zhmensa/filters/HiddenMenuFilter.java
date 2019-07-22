package com.mensa.zhmensa.filters;

import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.services.MensaManager;

public class HiddenMenuFilter implements MenuFilter{
    public boolean vegi = false;

    @Override
    public boolean apply(IMenu menu) {
        return !(MensaManager.HIDDEN_MENU_IDS.contains(menu.getId())) && (!vegi || menu.isVegi());
    }
}
