package com.mensa.zhmensa.services;

import com.mensa.zhmensa.filters.MensaFilter;
import com.mensa.zhmensa.models.DummyMensa;
import com.mensa.zhmensa.models.EthMensaCategory;
import com.mensa.zhmensa.models.FavoriteMensa;
import com.mensa.zhmensa.models.FavoriteMenu;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.models.UzhMensaCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

/**
 * Static factory class to supply mensa instances.
 * */
public class MensaFactory {

    /**
     *
     * @return all defined categories.
     */
    public static List<MensaCategory> getMensaCategories() {
        return Arrays.<MensaCategory>asList(
            //    new MensaCategory("Favorites"),
                new EthMensaCategory("ETH"),
                new UzhMensaCategory("UZH")
        );
    }

    public static Observable getObservableForCategory(MensaCategory category) {
        /**
        if(category.getDisplayName().equals("Favorites")) {
            return Arrays.<Mensa>asList(new FavoriteMensa("Favorites"));
        }
        // TODO
        List<Mensa> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            list.add(new DummyMensa(category.getDisplayName() + "-Mensa: " + i));
        }
        return list;*/

        // TODO cache
        return category.loadMensasFromAPI();
    }


    public static Mensa getTestMensa() {
        return new DummyMensa("Dummy");
    }
}
