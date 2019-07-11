package com.mensa.zhmensa.services;

import com.mensa.zhmensa.component.MensaTab;
import com.mensa.zhmensa.models.Mensa;

public class TabManager {

    public static MensaTab getTabForMensa(Mensa mensa){
        return new MensaTab(mensa.getUniqueId());
    }
}
