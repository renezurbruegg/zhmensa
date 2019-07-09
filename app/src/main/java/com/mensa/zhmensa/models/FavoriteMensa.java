package com.mensa.zhmensa.models;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;
import java.util.Set;


/**
 * Dummy implementation for a mensa. only for testing
 */
public class FavoriteMensa extends Mensa {

    public FavoriteMensa(String displayName) {
        super(displayName, displayName);
        setMensaCategory(new MensaCategory("Favorites") {

            @Override
            public Integer getCategoryIconId() {
                return R.drawable.ic_favorite_black_24dp;
            }

            @Override
            public List<MensaListObservable> loadMensasFromAPI() {
                return Collections.emptyList();
            }
        });
    }

    public void removeMenuFromList(IMenu menu) {
        for(Weekday day: Weekday.values()) {
            for (MenuCategory cat: MenuCategory.values() ) {
                Map<MenuCategory,Set<IMenu>> mealTypeToMenu = Helper.firstNonNull(meals.get(day), Collections.<MenuCategory, Set<IMenu>>emptyMap());
                Set<IMenu> menus = Helper.firstNonNull(mealTypeToMenu.get(cat), Collections.<IMenu>emptySet());

                menus.remove(menu);

                /*ListIterator<IMenu> iter = menus.listIterator();
                while(iter.hasNext()){
                    if(iter.next().getId().equals(menu.getId())){
                        iter.remove();
                    }
                }*/
            }
        }
    }

    public void addMenu(String mensaName, Weekday day, MenuCategory mealType, IMenu menu) {
        addMenuForDayAndCategory(day, mealType, Arrays.<IMenu>asList(new FavoriteMenu(mensaName, menu)));
    }
}
