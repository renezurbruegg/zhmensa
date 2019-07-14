package com.mensa.zhmensa.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.filters.MenuIdFilter;
import com.mensa.zhmensa.models.EthMensaCategory;
import com.mensa.zhmensa.models.FavoriteMensa;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.models.Menu;
import com.mensa.zhmensa.models.UzhMensaCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Static Helper class that Manages all mensa objects.
 */
public class MensaManager {


    private static final String LAST_UPDATE_PREF = "last_update";
    public static int SELECTED_DAY = Helper.getCurrentDay();
    /**
     * Context used to get shared Preferences
     */
    @SuppressLint("StaticFieldLeak")
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
    @Nullable
    private static Set<String> favoriteIds = new HashSet<>();

    @NonNull
    public static final Mensa.MenuCategory MEAL_TYPE = Mensa.MenuCategory.LUNCH;

    private static final List<OnMensaLoadedListener> listeners = new ArrayList<>();

    private static final MensaCategory dummyHonggCat = new EthMensaCategory("ETH-Hönggerberg", Arrays.asList("FUSION coffee", "FUSION meal", "Rice Up!", "food market - green day", "food market - grill bbQ", "food market - pizza pasta", "BELLAVISTA", "Food market - pizza pasta"), 2) {
        @NonNull
        @Override
        public List<MensaListObservable> loadMensasFromAPI() {
            return Collections.emptyList();
        }
    };

    private static final MensaCategory dummyUzhIrchel = new UzhMensaCategory("UZH-Irchel", Arrays.asList("Irchel", "Tierspital"), 4) {
        @NonNull
        @Override
        public List<MensaListObservable> loadMensasFromAPI() {
            return Collections.emptyList();
        }

    };

    private static final MensaCategory ETH_ZENT = new EthMensaCategory("ETH-Zentrum", 1);
    private static final MensaCategory UZH_ZENT = new UzhMensaCategory("UZH-Zentrum",3);
    private static final MensaCategory[] categories = {ETH_ZENT, dummyHonggCat, UZH_ZENT , dummyUzhIrchel};

    private static final MensaCategory[] LOADABLE_CAT = {ETH_ZENT, UZH_ZENT};

    @Nullable
    private static Long lastUpdate = null;



    // ---------- Internal functions ---------------


 /*   private static void addNewMensaItem(List<Mensa> mensas) {
        for (Mensa mensa : mensas) {
            if(!put(mensa)) {
                Log.e("MensaManager.addNew(..)", "current mensa allready stored in map. ID: " + mensa.getUniqueId());
            }
        }
    }
*/


    /**
     * Adds a new mensa for a given category, given day and given mealtype.
     *
     * @param mensas Mensa items to store
     * @param category MenuCategory
     * @param day
     * @param mealType
     */
    private static void addNewMensaItem(List<Mensa> mensas, MensaCategory category, Mensa.Weekday day, Mensa.MenuCategory mealType) {
        boolean changedFav = false;
        for (Mensa mensa : mensas) {
            mensa.setMensaCategory(getCategoryForMensa(mensa, category));

            // New loaded Menus
            List<IMenu> newMenus = mensa.getMenusForDayAndCategory(day, mealType);


            putAndUpdate(mensa, day, mealType, newMenus);

            // Check if any item is a favorite Menu. If yes, add it to the favorite Mensa-

            for (IMenu menu : newMenus) {
                if (favoriteIds.contains(menu.getId())) {
                    changedFav = true;
                    Log.e("MensaManager-Observable", "Menu: " + menu.getName() + " will be added to the favorite Mensa");
                    favoriteMensa.addMenu(mensa.getDisplayName(),day, mealType, menu);

                }
            }
        }
        if(changedFav) {
            for (OnMensaLoadedListener l : listeners)
                l.onMensaUpdated(favoriteMensa);
        }
    }



    public static void storeAllMensasToCache() {
            Map<MensaCategory, List<Mensa>> mensaMap = new HashMap<>();
            Log.d("Iterating over mensa", "Size: " + getAllMensasAsCollection().size());
            for(Mensa mensa : getAllMensasAsCollection()) {
                List<Mensa> storedList = Helper.firstNonNull(mensaMap.get(mensa.getCategory()), new ArrayList<Mensa>());
                storedList.add(mensa);

                Log.d("Adding", mensa.toString() + " for cat " + mensa.getCategory().getDisplayName());
                mensaMap.put(mensa.getCategory(), storedList);
            }

            for(MensaCategory cat : mensaMap.keySet()) {
                storeMensasForCategory(cat, mensaMap.get(cat));
            }
    }

    private static void storeMensasForCategory(@NonNull MensaCategory category, List<Mensa> mensas) {
        Set<String> mensaJson = new HashSet<>();

        for (Mensa mensa : mensas) {
            mensaJson.add(Helper.convertMensaToJsonString(mensa));
            Log.d("MensaMang.storemfc", "Adding " + mensa.getDisplayName() + " to store");
        }

        //Log.d("MensaManager.storeMensa", "Storing:" + mensaJson);
        activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(category.getDisplayName(), mensaJson)
                .apply();

        if(category.lastUpdated() != null) {
            activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(category.getDisplayName() + LAST_UPDATE_PREF, category.lastUpdated())
                    .apply();
        }

    }
    //private static void
    private static void loadCachedMensaForCategory(MensaCategory category) {

        Long lastUpdated = activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(category.getDisplayName() + LAST_UPDATE_PREF, 0);

        Log.d("MensaManager.loadCache", "Loading cached Mensas for category: " + category.getDisplayName());
        Set<String> mensaJsons = activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                .getStringSet(category.getDisplayName(), new HashSet<String>());

        if(mensaJsons.isEmpty()){
            Log.d("MensaManager.loadCache", "Mensajson was empty");
        }

        if(lastUpdated == null || lastUpdated == 0){
            Log.d("MensaManager.loadCache", "last updated was empty for cat " + category.getDisplayName());
            return;
        }


        if(category.lastUpdated() == null)
            category.setLastUpdated(lastUpdated);

        if(!Helper.isDataStillValid(lastUpdated)) {
            Log.d("MensaManager.loadCache", "Data too old for category " + category.getDisplayName());
            return;
        }



        for (String mensaJson : mensaJsons) {
            Mensa mensa = Helper.getMensaFromJsonString(mensaJson);
            Log.d("MensaManager.loadCache", "Found Mensa: " + mensa.getDisplayName());
            mensa.setMensaCategory(category);

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
            if(mensa == favoriteMensa)
                continue;

            for (Mensa.Weekday day : Mensa.Weekday.values()) {
                for (Mensa.MenuCategory mealType : Mensa.MenuCategory.values()) {
                    // Add Menus that match the menuId to the favorite Menu.
                    List<IMenu> favMenus = mensa.getMenusForDayAndCategory(day, mealType, filter);
                    for(IMenu menu : favMenus) {

                        if(!Helper.firstNonNull(menu.getName(),"").contains(mensa.getDisplayName()))
                            menu.setName(mensa.getDisplayName() + ": " + menu.getName());
                    }
                    favoriteMensa.addMenuForDayAndCategory(day, mealType, favMenus);
                }
            }

            for(OnMensaLoadedListener listener : listeners){
                listener.onMensaUpdated(favoriteMensa);
            }
        }
    }


    private static boolean put(@NonNull Mensa mensa) {
       return put(mensa, true);
    }

    /**
     * Adds a mensa to the IdToMensa Mapping and notifies all listeners
     * If a mensa allready exists, returns false
     * @param mensa The Mensa to add
     * @return true if mensa was added succesfully
     */
    private static boolean put(@NonNull Mensa mensa, @SuppressWarnings("SameParameterValue") boolean notifyListener) {
        if (IdToMensaMapping.get(mensa.getUniqueId()) == null) {
            IdToMensaMapping.put(mensa.getUniqueId(), mensa);

            // New mensa was inserted
            Log.d("MensaManager", "New mensa added: " + mensa.getDisplayName() + " triggering listener");
            for (OnMensaLoadedListener listener : listeners) {
                listener.onNewMensaLoaded(Collections.singletonList(mensa));
            }

            return true;
        } else if(notifyListener){
            for (OnMensaLoadedListener listener : listeners) {
                listener.onMensaUpdated(mensa);
            }
        }

        return false;
    }


    private static void putAndUpdate(Mensa mensa, Mensa.Weekday day, Mensa.MenuCategory mealType, @NonNull List<IMenu> newMenus) {
        Mensa storedMensa = getMensaForId(mensa.getUniqueId());
        Log.d("putAndUpdaate", "Mensa: " + mensa.toString());
        if(storedMensa == null) {
            put(mensa);
            return;
        }


        List<IMenu> storedItems = storedMensa.getMenusForDayAndCategory(day, mealType);

      /*  StringBuilder sb = new StringBuilder();
        for (IMenu menu : storedItems)
            sb.append(menu.getName() + " : " + menu.getDescription() + " , ");
        Log.d("MensaManager.putAU", "old Menus: " + sb.toString());

        sb = new StringBuilder();
        for (IMenu menu : newMenus)
            sb.append(menu.getName() + " : " + menu.getDescription() + " , ");
        Log.d("MensaManager.putAU", "New Menus: " + sb.toString());
*/
        if(storedItems.equals(newMenus)) {
            Log.d("MensaManager.putAU", "got updated for mensa and day: " + mensa.getUniqueId() + " day : " + day + " mealType " + mealType + " but meals are allready stored");


        } else {
            Log.d("MensaManager.putAU", "Added new menus for mensa" + mensa.getUniqueId() + " day : " + day + " mealType " + mealType + " items: " + newMenus.size());
            storedMensa.setMenuForDayAndCategory(day, mealType, newMenus);

        }
        for (OnMensaLoadedListener listener : listeners) {
            listener.onMensaUpdated(mensa);
        }


        // If Mensa was already stored, add the new menu to the one mensa that is allready stored.
        // /

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
        favoriteIds = new HashSet<>(activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getStringSet(FAVORITE_STORE_ID, new HashSet<String>()));
    }

    /**
     * Sets a Menu as favorit or removes it from favorites
     * @param favMenu The Menu
     */
    public static void toggleMenuFav(IMenu favMenu) {

        // Load favorite id from memory
        SharedPreferences prefs = activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        favoriteIds = new HashSet<>(prefs.getStringSet(FAVORITE_STORE_ID, new HashSet<String>()));

        if (favMenu.isFavorite()) {
            //Remove from favorites
            Log.d("MensaManager", "Toggle favorite for Menu: " + favMenu.getId());
            favoriteMensa.removeMenuFromList(favMenu);
            favoriteIds.remove(favMenu.getId());
            for(OnMensaLoadedListener listener : listeners) {
                listener.onMensaUpdated(favoriteMensa);
            }
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
        return Arrays.asList(
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
    @NonNull
    public static List<IMenu> getMenusForIdWeekAndCat(String mensaId, Mensa.MenuCategory category, Mensa.Weekday weekday) {
        Mensa storedMensa = IdToMensaMapping.get(mensaId);
        if(storedMensa == null)
            return Collections.emptyList();
        return storedMensa.getMenusForDayAndCategory(weekday, category);
    }

    /**
     * Prints a Mensa in Human Readable format
     * @param mensaId MensaId
     * @return
     */
    @NonNull
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

    private static boolean isFavoriteMensa(String mensaId) {
        return (favoriteMensa != null && favoriteMensa.getUniqueId().equals(mensaId));
    }

    /**
     * Loads Mensas for async for the given category.
     * Whenever a mensa is obtained that is not stored in the cache, the associated listeners get called
     *
     * @param category The category which mensas shoudl be loaded.
     */
    public static void loadMensasForCategory(@NonNull final MensaCategory category, boolean loadFromInternet) {

        loadCachedMensaForCategory(category);

        if(loadFromInternet)
            loadMensasForCategoryFromInternet(category);

    }


    /**
     * Adds the favorite Mensa to the MensaIdMapping and returns it.
     * @return
     */
    @NonNull
    public static Mensa getFavoritesMensa() {
        put(favoriteMensa);
        return favoriteMensa;
    }

    @Nullable
    public static Mensa getMensaForId(String mensaId) {
        return IdToMensaMapping.get(mensaId);
    }

    public static void clearState() {
        IdToMensaMapping.clear();
    }

    public static void clearCache() {
        if(activityContext == null)
            return;

        for(MensaCategory cat : getMensaCategories()) {
            activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(cat.getDisplayName(), new HashSet<String>())
                    .apply();
        }
        clearState();
    }

    public static List<IMenu> getPlaceholderForEmptyMenu(String mensaId) {
        if(isFavoriteMensa(mensaId)) {
            return Collections.<IMenu>singletonList(new Menu("dummy-np-items-fav", "", "Keine Favoriten vorhanden", "", "", "dummy"));
        }
        return Collections.<IMenu>singletonList(new Menu("dummy-no-items", "", "Momentan kein Menü verfügbar", "", "", "dummy"));
    }

    public static Observable loadMensasForCategoryFromInternet(final MensaCategory category) {


        if(category.getDisplayName().equals(favoriteMensa.getCategory().getDisplayName())){
            Log.d("MensaManager,loadfint", "Fav mensa to load");
            final OnLoadedObservable onLoadedObservable = new OnLoadedObservable(LOADABLE_CAT.length);

            for(MensaCategory cat: LOADABLE_CAT){
                if(!cat.equals((favoriteMensa.getCategory()))) {
                    loadMensasForCategoryFromInternet(cat).addObserver(new Observer() {
                        @Override
                        public void update(Observable observable, Object o) {
                            onLoadedObservable.loadingFinished();
                        }
                    });
                }

            }
            return onLoadedObservable;
        }

        final List<MensaListObservable> obs;
        if(category instanceof  EthMensaCategory) {
            Log.d("MensaManager,loadfint", "ETH category to load");
            obs = ETH_ZENT.loadMensasFromAPI();
        } else if (category instanceof  UzhMensaCategory){
            obs = UZH_ZENT.loadMensasFromAPI();
            Log.d("MensaManager,loadfint", "UZH ZENT category to load");
        } else {
            // Fallback
            obs = ETH_ZENT.loadMensasFromAPI();
        }

        final OnLoadedObservable onLoadedObservable = new OnLoadedObservable(obs.size());

        for (final MensaListObservable observable : obs) {
            observable.addObserver(new Observer() {
                // If new Mensas were loaded.
                @Override
                public void update(Observable obs, Object o) {
                    Log.e("MensaManager-Observable", "Got Updates for loadMensaFromAPI()");
                    category.setLastUpdated(Helper.getStartOfWeek().getMillis());

                    // Add every new mensa to the mensa mapping. This will notify all listeners and they will appear in the sidebar.
                    addNewMensaItem(observable.getNewItems(), category, observable.day, observable.mealType);
                    onLoadedObservable.loadingFinished();
                }
            });
        }

        return onLoadedObservable;
    }

    public static void invalidateMensa(String mensaId) {
        IdToMensaMapping.get(mensaId).clearMenus();
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

    public static class OnLoadedObservable extends Observable {
        private int count = 0;
        private final int maxCount;

        public OnLoadedObservable(int maxLoads) {
            maxCount = maxLoads;
        }


        public void loadingFinished() {
            count += 1;
            if(count == maxCount) {
                setChanged();
                notifyObservers();
            }
        }

    }
}
