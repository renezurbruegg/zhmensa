package com.mensa.zhmensa.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.filters.MenuIdFilter;
import com.mensa.zhmensa.models.EthMensaCategory;
import com.mensa.zhmensa.models.FavoriteMensa;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.models.UzhMensaCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Static Helper class that Manages all mensa objects.
 */
public class MensaManager {

    /**
     * Context used to get shared Preferences
     */
    private static Context activityContext;

    /**
     * Key to get shared preferences
     */
    private final static String SHARED_PREFS_NAME = "com.zhmensa.favorites";
    /**
     * Key to find favorite Menu id String set
     */
    private final static String FAVORITE_STORE_ID = "favorite";

    /**
     * Map that maps a mensaId to a Mensa object
     */
    private final static Map<String, Mensa> IdToMensaMapping = new HashMap<>();

    /**
     * Favorite Mensa. Contains all meals that are marked as favorites
     */
    private final static FavoriteMensa favoriteMensa = new FavoriteMensa("Favorites");

    /**
     * List containing ID's of menus that are marked as favorites
     */
    private static Set<String> favoriteIds = new HashSet<>();

    private static final List<OnMensaLoadedListener> listeners = new ArrayList<>();

    private static final MensaCategory dummyHonggCat = new EthMensaCategory("ETH-Höngg", Arrays.<String>asList("FUSION coffee", "FUSION meal", "Rice Up!", "food market - green day", "food market - grill bbQ", "food market - plaza pasta")) {
        @Override
        public List<MensaListObservable> loadMensasFromAPI() {
            return Collections.emptyList();
        }
    };

    private static final MensaCategory[] categories = {new EthMensaCategory("ETH"), new UzhMensaCategory("UZH"), dummyHonggCat};



    // ---------- Internal functions ---------------


    private static void addNewMensaItem(List<Mensa> mensas) {
        for (Mensa mensa : mensas) {
            if(!put(mensa)) {
                Log.e("MensaManager.addNew(..)", "current mensa allready stored in map. ID: " + mensa.getUniqueId());
            }
        }
    }



    /**
     * Adds a new mensa for a given category, given day and given mealtype.
     *
     * @param mensas Mensa items to store
     * @param category MenuCategory
     * @param day
     * @param mealType
     */
    private static void addNewMensaItem(List<Mensa> mensas, MensaCategory category, Mensa.Weekday day, Mensa.MenuCategory mealType) {

        for (Mensa mensa : mensas) {
            mensa.setMensaCategory(getCategoryForMensa(mensa, category));

            // New loaded Menus
            List<IMenu> newMenus = mensa.getMenusForDayAndCategory(day, mealType);
            if (!put(mensa)) {
                // If Mensa was already stored, add the new menu to the one mensa that is allready stored.
                Helper.firstNonNull(IdToMensaMapping.get(mensa.getUniqueId()), new Mensa("Dummy","Dummy")).setMenuForDayAndCategory(day, mealType, newMenus);
            }

            // Check if any item is a favorite Menu. If yes, add it to the favorite Mensa-
            for (IMenu menu : newMenus) {
                if (favoriteIds.contains(menu.getId())) {
                    Log.e("MensaManager-Observable", "Menu: " + menu.getName() + " will be added to the favorite Mensa");
                    favoriteMensa.addMenu(mensa.getDisplayName(),day, mealType, menu);
                }
            }
        }
    }


    public static void storeAllMensasToCache() {
            Map<MensaCategory, List<Mensa>> mensaMap = new HashMap<>();
            Log.d("Iterating over mensa", "Size: " + getAllMensasAsCollection().size());
            for(Mensa mensa : getAllMensasAsCollection()) {
                List<Mensa> storedList = Helper.firstNonNull(mensaMap.get(mensa.getCategory()), new ArrayList<Mensa>());
                storedList.add(mensa);

                Log.d("Adding", mensa.getDisplayName() + " for cat " + mensa.getCategory().getDisplayName());
                mensaMap.put(mensa.getCategory(), storedList);
            }

            for(MensaCategory cat : mensaMap.keySet()) {
                storeMensasForCategory(cat, mensaMap.get(cat));
            }
    }

    private static void storeMensasForCategory(MensaCategory category, List<Mensa> mensas) {
        Set<String> mensaJson = new HashSet<>();

        for (Mensa mensa : mensas) {
            mensaJson.add(Helper.convertMensaToJsonString(mensa));
        }

        Log.d("MensaManager.storeMensa", "Storing:" + mensaJson);
        activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(category.getDisplayName(), mensaJson)
            .apply();

    }
    //private static void
    private static void loadCachedMensaForCategory(MensaCategory category) {
        Log.d("MensaManager.loadCache", "Loading cached Mensas for category: " + category.getDisplayName());
        Set<String> mensaJsons = activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                                                        .getStringSet(category.getDisplayName(), new HashSet<String>());
        for (String mensaJson : mensaJsons) {
            Mensa mensa = Helper.getMensaFromJsonString(mensaJson);
            Log.d("MensaManager.loadCache", "Found Mensa: " + mensa.getDisplayName());

            if(!put(mensa)) {
                Log.e("MensaManager-lcmfc", "Tried to store mensa " + mensa.getDisplayName() + " but mensa was allready known");
            }

            for(Mensa.Weekday day : Mensa.Weekday.values()){
                for(Mensa.MenuCategory cat: Mensa.MenuCategory.values()){
                    for (IMenu menu : mensa.getMenusForDayAndCategory(day, cat)) {
                        if (favoriteIds.contains(menu.getId())) {
                            Log.e("MensaManager-loadCache", "Menu: " + menu.getName() + " will be added to the favorite Mensa");
                            favoriteMensa.addMenu(mensa.getDisplayName(),day, cat, menu);
                        }
                    }
                }
            }

        }
    }


    /**
     * Returns the category for a given mensa. if no matching one is found, the given default category is returned
     * @param mensa
     * @param defaultCategory
     * @return
     */
    private static MensaCategory getCategoryForMensa(Mensa mensa, MensaCategory defaultCategory) {
        for (MensaCategory cat: categories ) {
            if(cat.containsMensa(mensa)) {
                return cat;
            }
        }

        return defaultCategory;
    }


    /**
     *
     * @return All stored Mensas as collection
     */
    private static Collection<Mensa> getAllMensasAsCollection() {
        return IdToMensaMapping.values();
    }


    /**
     * Adds the current id to the favorite Set.
     * Then iterates through every Mensa and adds the meals that match the menuId as Menus to the favorite Mensa
     * @param menuId The Menu Id that was marked as favorites
     */
    private static void addFavMenuById(String menuId) {
        MenuFilter filter = new MenuIdFilter(menuId);
        favoriteIds.add(menuId);

        for (Mensa mensa : getAllMensasAsCollection()) {
            for (Mensa.Weekday day : Mensa.Weekday.values()) {
                for (Mensa.MenuCategory mealType : Mensa.MenuCategory.values()) {
                    // Add Menus that match the menuId to the favorite Menu.
                    favoriteMensa.addMenuForDayAndCategory(day, mealType, mensa.getMenusForDayAndCategory(day, mealType, filter));
                }
            }
        }
    }

    /**
     * Adds a mensa to the IdToMensa Mapping and notifies all listeners
     * If a mensa allready exists, returns false
     * @param mensa The Mensa to add
     * @return true if mensa was added succesfully
     */
    private static boolean put(Mensa mensa) {
        if (IdToMensaMapping.get(mensa.getUniqueId()) == null) {

            IdToMensaMapping.put(mensa.getUniqueId(), mensa);

            // New mensa was inserted
            Log.d("MensaManager", "New mensa added: " + mensa.getDisplayName() + " triggering listener");
            for (OnMensaLoadedListener listener : listeners) {
                listener.onNewMensaLoaded(Arrays.asList(mensa));
            }

            return true;
        } else {
            for (OnMensaLoadedListener listener : listeners) {
                listener.onMensaUpdated(mensa);
            }
        }

        return false;
    }


    // -------- END Internal Functions



    /**
     * MUST BE CALLED BEFORE ANY FUNCTION OF MENSAMANAGER IS USED!
     * Sets context to get Shared Preferences.
     *
     * @param context Context of the current activity
     */
    public static void setActivityContext(Context context) {
        activityContext = context;

        // Load favorite Ids from shared Preferences.
        favoriteIds = new HashSet<String>(activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getStringSet(FAVORITE_STORE_ID, new HashSet<String>()));
    }

    /**
     * Sets a Menu as favorit or removes it from favorites
     * @param favMenu The Menu
     */
    public static void toggleMenuFav(IMenu favMenu) {

        // Load favorite id from memory
        SharedPreferences prefs = activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        favoriteIds = new HashSet<String>(prefs.getStringSet(FAVORITE_STORE_ID, new HashSet<String>()));

        if (favMenu.isFavorite()) {
            //Remove from favorites
            Log.d("MensaManager", "Toggle favorite for Menu: " + favMenu.getId());
            favoriteMensa.removeMenuFromList(favMenu);
            favoriteIds.remove(favMenu.getId());
        } else {
            // Add to favorites
            addFavMenuById(favMenu.getId());
        }

        prefs.edit().putStringSet(FAVORITE_STORE_ID, favoriteIds).apply();
    }


    /**
     * @return all defined categories.
     */
    public static List<MensaCategory> getMensaCategories() {
        return Arrays.<MensaCategory>asList(
              categories
        );
    }

    /**
     * Adds a listener that will be called every time a new Mensa is loaded
     * @param listener
     */
    public static void addOnMensaLoadListener(OnMensaLoadedListener listener) {
        listeners.add(listener);
    }

    /**
     * Returns a dummy mensa for test purposes
     * @return dummy mensa
     */
    @Deprecated
    public static Mensa getTestMensa() {
        return new Mensa("Dummy", "test");/*
                Arrays.<IMenu>asList(new Menu("WOK STREET", "GAENG PED\n" +
                                "with Swiss chicken or beef liver in\n" +
                                "spiced red Thai curry sauce with yellow carrots, beans, carrots, sweet Thai basil\n" +
                                "and jasmine rice ", "8.50 / 9.50/ 10.50", "keine Allergene"),
                        new Menu("TestMenu2",  "whatever", "8.50 / 9.50/ 10.50", "keine Allergene"),
                        new Menu("TestMenu3", "Description", "8.50 / 9.50/ 10.50", "keine Allergene")));*/
    }


    /**
     * Returns all mensas for a given day, category and Mensa
     * @param mensaId
     * @param category
     * @param weekday
     * @return
     */
    public static List<IMenu> getMenusForIdWeekAndCat(String mensaId, Mensa.MenuCategory category, Mensa.Weekday weekday) {
        return Helper.firstNonNull(IdToMensaMapping.get(mensaId), new Mensa("Dummy","Dummy")).getMenusForDayAndCategory(weekday, category);
    }

    /**
     * Prints a Mensa in Human Readable format
     * @param mensaId MensaId
     * @return
     */
    public static String printMensa(String mensaId) {
        return Helper.firstNonNull(IdToMensaMapping.get(mensaId), new Mensa("Dummy","Dummy")).toString();
    }


    /**
     * Returns if a given Menu is in the favorites list
     * @param menuId The id of the menu
     * @return true if menu is in the favorite Menu
     */
    public static boolean isFavorite(String menuId) {
        return favoriteIds.contains(menuId);
    }

    /**
     * Loads Mensas for async for the given category.
     * Whenever a mensa is obtained that is not stored in the cache, the associated listeners get called
     *
     * @param category The category which mensas shoudl be loaded.
     */
    public static void loadMensasForCategory(final MensaCategory category) {

        loadCachedMensaForCategory(category);

        for (final MensaListObservable observable : category.loadMensasFromAPI()) {
            observable.addObserver(new Observer() {
                // If new Mensas were loaded.
                @Override
                public void update(Observable obs, Object o) {
                    Log.e("MensaManager-Observable", "Got Updates for loadMensaFromAPI()");

                    // Add every new mensa to the mensa mapping. This will notify all listeners and they will appear in the sidebar.
                    addNewMensaItem(observable.getNewItems(), category, observable.day, observable.mealType);
                }
            });
        }
    }


    /**
     * Adds the favorite Mensa to the MensaIdMapping and returns it.
     * @return
     */
    public static Mensa getFavoritesMensa() {
        put(favoriteMensa);
        return favoriteMensa;
    }

    public static Mensa getMensaForId(String mensaId) {
        return IdToMensaMapping.get(mensaId);
    }


    /**
     * Class that is used to notify observers if a new mensa was loaded
     */
    public interface OnMensaLoadedListener {

        /**
         * Gets called every time a new Mensa is loaded
         *
         * @param mensa List containing all new loaded mensas
         */
        void onNewMensaLoaded(List<Mensa> mensa);

        void onMensaUpdated(Mensa mensa);
    }
}
