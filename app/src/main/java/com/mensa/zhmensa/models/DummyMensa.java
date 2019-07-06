package com.mensa.zhmensa.models;

import java.util.Arrays;
import java.util.List;


/**
 * Dummy implementation for a mensa. only for testing
 */
public class DummyMensa extends Mensa {

    public DummyMensa(String displayName) {
        super(displayName);
    }

    @Override
    public List<IMenu> getMenusForDayAndCategory(Weekday day, MenuCategory category) {
        return Arrays.<IMenu>asList(new Menu("WOK STREET", "GAENG PED\n" +
                "with Swiss chicken or beef liver in\n" +
                "spiced red Thai curry sauce with yellow carrots, beans, carrots, sweet Thai basil\n" +
                "and jasmine rice ", "8.50 / 9.50/ 10.50", "keine Allergene"),
                new Menu("TestMenu2",  getDisplayName(), "8.50 / 9.50/ 10.50", "keine Allergene"),
                new Menu("TestMenu3", "Description", "8.50 / 9.50/ 10.50", "keine Allergene"));

    }
}
