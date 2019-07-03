package com.mensa.zhmensa.services;

import com.mensa.zhmensa.filters.MensaFilter;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.Menu;

import java.util.Arrays;
import java.util.Collections;
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

    public static Mensa getTestMensa() {
        return new Mensa() {
            @Override
            public List<Menu> getMenus() {  
               return Arrays.asList(new Menu("WOK STREET", "GAENG PED\n" +
                               "with Swiss chicken or beef liver in\n" +
                               "spiced red Thai curry sauce with yellow carrots, beans, carrots, sweet Thai basil\n" +
                               "and jasmine rice ", 1.25, "keine Allergene"),
                       new Menu("TestMenu2", "Description", 1.25, "keine Allergene"),
                       new Menu("TestMenu3", "Description", 1.25, "keine Allergene"));
            }
        };
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
