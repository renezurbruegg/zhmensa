package com.mensa.zhmensa.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation for a favorite mensa.
 * Offers remove Menu from list function
 */
public class FavoriteMensa extends Mensa {

    public FavoriteMensa(String displayName) {
        super(displayName, displayName);

        setMensaCategory(new MensaCategory(Helper.getFavoriteTitle(), 0) {
            @Override
            public Integer getCategoryIconId() {
                return R.drawable.ic_favorite_black_24dp;
            }

            @NonNull
            @Override
            public List<MensaListObservable> loadMensasFromAPI() {
                return Collections.emptyList();
            }
        });

    }

    public void removeMenuFromList(@NonNull IMenu menu) {
        for(Weekday day: Weekday.values()) {
            Map<MenuCategory,Set<IMenu>> mealTypeToMenu = meals.get(day);

            if(mealTypeToMenu == null) {
                Log.d("FavMensa.remove", "Could not find menu for day: " + day);
                continue;
            }

            for (MenuCategory cat: MenuCategory.values() ) {

                Set<IMenu> menus = mealTypeToMenu.get(cat);

                if(menus == null) {
                    Log.d("FavMensa.remove", "Could not find menus for day: and cat: " + day + " " + cat);
                    continue;
                }
                Log.d("FavMensa.remove", "Current menus:" +  menus.toString());
                IMenu storedMenu = null;
                for(IMenu sMenu : menus) {
                    if(sMenu.getId().equals(menu.getId()))
                        storedMenu = sMenu;
                }
                if(storedMenu == null)
                    continue;

                menus.remove(storedMenu);

                Log.d("FavMensa.remove", "New menus:" +  menus.toString());
                mealTypeToMenu.put(cat, menus);
                meals.put(day, mealTypeToMenu);
                /*ListIterator<IMenu> iter = menus.listIterator();
                while(iter.hasNext()){
                    if(iter.next().getId().equals(menu.getId())){
                        iter.remove();
                    }
                }*/
            }
        }
    }

    public void addMenu(String mensaName, Weekday day, MenuCategory mealType, @NonNull IMenu menu) {
        addMenuForDayAndCategory(day, mealType, Collections.<IMenu>singletonList(new FavoriteMenu(mensaName, menu)));
    }
}
