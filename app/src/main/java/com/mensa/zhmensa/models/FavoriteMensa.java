package com.mensa.zhmensa.models;

import com.mensa.zhmensa.services.Helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;


/**
 * Dummy implementation for a mensa. only for testing
 */
public class FavoriteMensa extends Mensa {

    public FavoriteMensa(String displayName) {
        super(displayName, displayName);
        setMensaCategory(new MensaCategory("Favorites") {
            @Override
            Observable getMensaUpdateForDayAndMeal(Weekday day, MenuCategory menuCategory) {
                return new Observable();
            }

            @Override
            public List<MensaListObservable> loadMensasFromAPI() {
                return Collections.emptyList();
            }
        });
    }
/*
    @Override
    public List<IMenu> getMenusForDayAndCategory(Weekday day, MenuCategory category) {
        return Arrays.<IMenu>asList(new Menu("WOK STREET", "GAENG PED\n" +
                "with Swiss chicken or beef liver in\n" +
                "spiced red Thai curry sauce with yellow carrots, beans, carrots, sweet Thai basil\n" +
                "and jasmine rice ", "8.50 / 9.50/ 10.50", "keine Allergene"),
                new Menu("TestMenu2",  getDisplayName(), "8.50 / 9.50/ 10.50", "keine Allergene"),
                new Menu("TestMenu3", "Description", "8.50 / 9.50/ 10.50", "keine Allergene"));

    }
*/
    public void removeMenuFromList(IMenu menu) {
        for(Weekday day: Weekday.values()) {
            for (MenuCategory cat: MenuCategory.values() ) {
                Map<MenuCategory,List<IMenu>> mealTypeToMenu = Helper.firstNonNull(meals.get(day), Collections.<MenuCategory, List<IMenu>>emptyMap());
                List<IMenu> menus = Helper.firstNonNull(mealTypeToMenu.get(cat), Collections.<IMenu>emptyList());

                ListIterator<IMenu> iter = menus.listIterator();
                while(iter.hasNext()){
                    if(iter.next().getId().equals(menu.getId())){
                        iter.remove();
                    }
                }/*
                for (IMenu menuItem : menus) {
                    if(menuItem.getName().equals(menu.getName())) {
                        menus.remove(menuItem);
                    }
                }*/
            }
        }
    }
}
