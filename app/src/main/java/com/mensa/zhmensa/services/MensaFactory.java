package com.mensa.zhmensa.services;

import com.mensa.zhmensa.filters.MensaFilter;
import com.mensa.zhmensa.models.DummyMensa;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static factory class to supply mensa instances.
 * */
public class MensaFactory {

    /**
     *
     * @return List containing all available Mensai ;)
     */
    public static List<Mensa> getMensaList() {

        // TODO
        return null;
    }

    /**
     *
     * @return all defined categories.
     */
    public static List<MensaCategory> getMensaCategories() {
        return Arrays.asList(
                new MensaCategory("ETH"),
                new MensaCategory("UZH")
        );
    }

    public static List<Mensa> getMensaListForCategory(MensaCategory category) {
        // TODO
        List<Mensa> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            list.add(new DummyMensa(category.getDisplayName() + "-Mensa: " + i));
        }
        return list;
    }


    public static Mensa getTestMensa() {
        return new DummyMensa("Dummy");
    }

    /**
     *
     * @return List containing all available Mensai :) that match the given filter
     */
    public static List<Mensa> getMensaList(MensaFilter filter) {
        // TODO
        return null;
    }
}
