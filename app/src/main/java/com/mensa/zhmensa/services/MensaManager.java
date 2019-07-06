package com.mensa.zhmensa.services;

import android.util.Log;

import com.mensa.zhmensa.filters.MensaFilter;
import com.mensa.zhmensa.models.DummyMensa;
import com.mensa.zhmensa.models.EthMensaCategory;
import com.mensa.zhmensa.models.FavoriteMensa;
import com.mensa.zhmensa.models.FavoriteMenu;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.models.Menu;
import com.mensa.zhmensa.models.UzhMensaCategory;
import com.mensa.zhmensa.navigation.NavigationMenuChild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Static factory class to supply mensa instances.
 * */
public class MensaManager {


    private final static Map<String, Mensa> IdToMensaMapping = new HashMap<>();

    private static Mensa getMensaFromId(String id) {
        Mensa mensa = IdToMensaMapping.get(id);
        if (mensa == null) {
            Log.d("not found", "mensa not found id: " + id);
            return new DummyMensa("Dummy");

        }
        return mensa;
    }

    private static boolean put(Mensa mensa) {
        if (IdToMensaMapping.get(mensa.getUniqueId()) != null){
            return false;
        }
        IdToMensaMapping.put(mensa.getUniqueId(), mensa);
        // New mensa was inserted
        Log.d("new Mensa added", "New mensa added: " + mensa.getDisplayName() + " triggering listener");
        for(OnMensaLoadedListener listener: listeners) {
            listener.onNewMensaLoaded(Arrays.asList(mensa));
        }
        return true;
    }

    private static final List<OnMensaLoadedListener> listeners = new ArrayList<>();

    private final static AtomicInteger idGenerator = new AtomicInteger(0);

    /**
     * @return all defined categories.
     */
    public static List<MensaCategory> getMensaCategories() {
        return Arrays.<MensaCategory>asList(
                //   new MensaCategory("Favorites"),
                new EthMensaCategory("ETH"),
               new UzhMensaCategory("UZH")
        );
    }

    public static void addOnMensaLoadListener(OnMensaLoadedListener listener) {
        listeners.add(listener);
    }



    public static Mensa getTestMensa() {
        return new Mensa("Dummy","test");/*
                Arrays.<IMenu>asList(new Menu("WOK STREET", "GAENG PED\n" +
                                "with Swiss chicken or beef liver in\n" +
                                "spiced red Thai curry sauce with yellow carrots, beans, carrots, sweet Thai basil\n" +
                                "and jasmine rice ", "8.50 / 9.50/ 10.50", "keine Allergene"),
                        new Menu("TestMenu2",  "whatever", "8.50 / 9.50/ 10.50", "keine Allergene"),
                        new Menu("TestMenu3", "Description", "8.50 / 9.50/ 10.50", "keine Allergene")));*/
    }


    public static List<IMenu> getMenusForIdWeekAndCat(String menuId, Mensa.MenuCategory category, Mensa.Weekday weekday) {
        return MensaManager.getMensaFromId(menuId).getMenusForDayAndCategory(weekday, category);
    }

    public static String getUniqueIdForMensa(Mensa mensa) {
        return mensa.getDisplayName();// + idGenerator.incrementAndGet();
    }

    public static String printMensa(String uniqueId) {
        return getMensaFromId(uniqueId).toString();
    }


    /**
     * Loads Mensas for async for the given category.
     * Whenever a mensa is obtained that is not stored in the cache, the associated listeners get called
     * @param category The cattegory which mensas shoudl be loaded.
     */
    public static void loadMensasForCategory(final MensaCategory category) {
        for(final MensaListObservable observable : category.loadMensasFromAPI()) {
            observable.addObserver(new Observer() {
                @Override
                public void update(Observable obs, Object o) {
                    Log.e("obs","GOT notification");

                    if(observable.mealType == Mensa.MenuCategory.DINNER) {
                        Log.d("found dinner!", "!dinner!");
                    }

                    for(Mensa mensa : ((MensaListObservable) observable).getNewItems()) {
                        Log.e("i", "put mensa to list " + mensa.getUniqueId());
                        mensa.setMensaCategory(category);
                        if(!put(mensa)){
                            getMensaFromId(mensa.getUniqueId()).addMenuForDayAndCategory(observable.day,observable.mealType, mensa.getMenusForDayAndCategory(observable.day, observable.mealType));
                        }
                    }
                }
            });
        }
    }


    public interface OnMensaLoadedListener{

        /**
         * Gets called every time a new Mensa is loaded
         * @param mensa List containing all new loaded mensas
         */
        void onNewMensaLoaded(List<Mensa> mensa);
    }
}
