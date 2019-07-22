package com.mensa.zhmensa.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.filters.HiddenMenuFilter;
import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.filters.MenuIdFilter;
import com.mensa.zhmensa.models.categories.EthMensaCategory;
import com.mensa.zhmensa.models.FavoriteMensa;
import com.mensa.zhmensa.models.menu.FavoriteMenu;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.models.menu.Menu;
import com.mensa.zhmensa.models.categories.UzhMensaCategory;

import org.joda.time.DateTime;

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


    @SuppressWarnings("HardCodedStringLiteral")
    public static final String DELETED_MENUS_STORE_ID = "deleted_menus" ;
    public static final String UPDATE_MENU_FILTER_KEY = "update_menu_filter";
    public static Set<String> HIDDEN_MENU_IDS = new HashSet<>();

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String LAST_UPDATE_PREF = "last_update";
    public static int SELECTED_DAY = Helper.getCurrentDay();
    /**
     * Context used to get shared Preferences
     */
    // private static Context activityContext;

    /**
     * Key to get shared preferences
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private final static String SHARED_PREFS_NAME = "com.zhmensa.favorites";

    /**
     * Key to find favorite Menu id String set
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private final static String FAVORITE_STORE_ID = "favorite";

    /**
     * Map that maps a mensaId to a Mensa object
     */
    private final static Map<String, Mensa> IdToMensaMapping = new HashMap<>();

    /**
     * Favorite Mensa. Contains all meals that are marked as favorites
     */
    private static FavoriteMensa favoriteMensa;

    /**
     * List containing ID's of menus that are marked as favorites
     */
    @Nullable
    private static Set<String> favoriteIds = new HashSet<>();


    @NonNull
    public static final Mensa.MenuCategory MEAL_TYPE = Mensa.MenuCategory.LUNCH;

    private static OnMensaLoadedListener listener = new OnMensaLoadedListener() {
        @Override
        public void onNewMensaLoaded(List<Mensa> mensa) {

        }

        @Override
        public void onMensaUpdated(Mensa mensa) {

        }

        @Override
        public void onMensaOpeningChanged() {

        }
    };

    @SuppressWarnings("HardCodedStringLiteral")
    private static final MensaCategory dummyHonggCat = new EthMensaCategory("ETH-Hönggerberg", Arrays.asList("FUSION coffee", "FUSION meal", "Rice Up!", "food market - green day", "food market - grill bbQ", "food market - pizza pasta", "BELLAVISTA", "Food market - pizza pasta"), 2) {
        @NonNull
        @Override
        public List<MensaListObservable> loadMensasFromAPI(String code) {
            return Collections.emptyList();
        }
    };

    @SuppressWarnings("HardCodedStringLiteral")
    private static final MensaCategory dummyUzhIrchel = new UzhMensaCategory("UZH-Irchel", Arrays.asList("Irchel", "Tierspital"), 4) {
        @NonNull
        @Override
        public List<MensaListObservable> loadMensasFromAPI(String code) {
            return Collections.emptyList();
        }

    };

    @SuppressWarnings("HardCodedStringLiteral")
    private static final MensaCategory ETH_ZENT = new EthMensaCategory("ETH-Zentrum", 1);

    @SuppressWarnings("HardCodedStringLiteral")
    private static final MensaCategory UZH_ZENT = new UzhMensaCategory("UZH-Zentrum",3);
    private static final MensaCategory[] categories = {ETH_ZENT, dummyHonggCat, UZH_ZENT , dummyUzhIrchel};

    private static final MensaCategory[] LOADABLE_CAT = {ETH_ZENT, UZH_ZENT};

    @Nullable
    private static Long lastUpdate = null;
    private static HiddenMenuFilter filter;


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
    private static void addNewMensaItem(List<Mensa> mensas, MensaCategory category, Mensa.Weekday day, Mensa.MenuCategory mealType, boolean loadedFromInternet) {
        boolean changedFav = false;

        final long currentTime = new DateTime(System.currentTimeMillis()).withDayOfWeek(1).getMillis();


        for (Mensa mensa : mensas) {

            mensa.setLastUpdated(currentTime);

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
            favoriteMensa.setLastUpdated(currentTime);
            listener.onMensaUpdated(favoriteMensa);
        }
    }



    public static void storeAllMensasToCache(Context activityContext) {
            Map<MensaCategory, List<Mensa>> mensaMap = new HashMap<>();
            Log.d("Iterating over mensa", "Size: " + getAllMensasAsCollection().size());
            for(Mensa mensa : getAllMensasAsCollection()) {
                List<Mensa> storedList = Helper.firstNonNull(mensaMap.get(mensa.getCategory()), new ArrayList<Mensa>());
                storedList.add(mensa);

                Log.d("Adding", mensa.toString() + " for cat " + mensa.getCategory().getDisplayName());
                mensaMap.put(mensa.getCategory(), storedList);
            }

            for(MensaCategory cat : mensaMap.keySet()) {
                storeMensasForCategory(cat, mensaMap.get(cat), activityContext);
            }




            //Log.d("MensaManager.storeMensa", "Storing:" + mensaJson);
           PreferenceManager.getDefaultSharedPreferences(activityContext)
                    .edit()
                    .putStringSet(DELETED_MENUS_STORE_ID, HIDDEN_MENU_IDS)
                    .apply();

        activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .commit();


    }

    private static void storeMensasForCategory(@NonNull MensaCategory category, List<Mensa> mensas, Context activityContext) {
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
    private static boolean loadCachedMensaForCategory(MensaCategory category, Context activityContext) {

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
            return false;
        }


        if(category.lastUpdated() == null)
            category.setLastUpdated(lastUpdated);

        if(!Helper.isDataStillValid(lastUpdated)) {
            Log.d("MensaManager.loadCache", "Data too old for category " + category.getDisplayName());
            return false;
        }



        // List<Mensa> loadedMensas = new ArrayList<>();

        lastUpdated = new DateTime(lastUpdated).withDayOfWeek(1).getMillis();
        if(favoriteMensa.getLastUpdated() != lastUpdated)
            favoriteMensa.setLastUpdated(lastUpdated);

        for (String mensaJson : mensaJsons) {
            Mensa mensa = Helper.getMensaFromJsonString(mensaJson);
            mensa.setLoadedFromCache(true);
            mensa.setLastUpdated(lastUpdated);
            Log.d("MensaManager.loadCache", "Found Mensa: " + mensa.getDisplayName());

            mensa.setMensaCategory( getCategoryForMensa(mensa, category));
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
        return true;
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
                    List<IMenu> favoriteMenus = new ArrayList<>();
                    for(IMenu menu : favMenus)
                        favoriteMenus.add(new FavoriteMenu(mensa.getDisplayName(), menu));
                    /*for(IMenu menu : favMenus) {

                        if(!Helper.firstNonNull(menu.getName(),"").contains(mensa.getDisplayName()))
                            menu.setName(mensa.getDisplayName() + ": " + menu.getName());
                    }*/
                    favoriteMensa.addMenuForDayAndCategory(day, mealType, favoriteMenus);
                }
            }

            listener.onMensaUpdated(favoriteMensa);

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
        Mensa storedMensa = IdToMensaMapping.get(mensa.getUniqueId());
        if (storedMensa == null) {
            IdToMensaMapping.put(mensa.getUniqueId(), mensa);

            // New mensa was inserted
            Log.d("MensaManager", "New mensa added: " + mensa.getDisplayName() + " triggering listener");
            listener.onNewMensaLoaded(Collections.singletonList(mensa));


            return true;
        } else if(notifyListener){
                listener.onMensaUpdated(mensa);
        }


        return false;
    }


    private static void putAndUpdate(Mensa mensa, Mensa.Weekday day, Mensa.MenuCategory mealType, @NonNull List<IMenu> newMenus) {
        Mensa storedMensa = getMensaForId(mensa.getUniqueId());
        Log.d("putAndUpdaate", "Mensa: " + mensa.toString());
        if(storedMensa == null) {
            put(mensa, true);
            return;
        }
        storedMensa.setLastUpdated(mensa.getLastUpdated());


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

        if(storedMensa.loadedFromCache()) {
            storedMensa.setLoadedFromCache(false);
            storedMensa.setClosed(mensa.isClosed());
            listener.onMensaOpeningChanged();
        } else if(storedMensa.isClosed() && !mensa.isClosed()) {
            storedMensa.setClosed(false);
            listener.onMensaOpeningChanged();
        }
        listener.onMensaUpdated(mensa);


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
    public static void initManager(Context activityContext) {

            favoriteMensa = new FavoriteMensa(activityContext.getString(R.string.favorites_title));
            // Load favorite Ids from shared Preferences.
            favoriteIds = new HashSet<>(activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getStringSet(FAVORITE_STORE_ID, new HashSet<String>()));
            //HIDDEN_MENU_IDS = new HashSet<>(activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getStringSet(DELETED_MENUS_STORE_ID, new HashSet<String>()));
            HIDDEN_MENU_IDS = new HashSet<>(PreferenceManager.getDefaultSharedPreferences(activityContext).getStringSet(DELETED_MENUS_STORE_ID, new HashSet<String>()));
    }


    /**
     * Sets a Menu as favorit or removes it from favorites
     * @param favMenu The Menu
     */
    public static void toggleMenuFav(IMenu favMenu, Context activityContext) {

        // Load favorite id from memory
        SharedPreferences prefs = activityContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        favoriteIds = new HashSet<>(prefs.getStringSet(FAVORITE_STORE_ID, new HashSet<String>()));

        if (favMenu.isFavorite()) {
            //Remove from favorites
            Log.d("MensaManager", "Toggle favorite for Menu: " + favMenu.getId());
            favoriteMensa.removeMenuFromList(favMenu);
            favoriteIds.remove(favMenu.getId());

            listener.onMensaUpdated(favoriteMensa);

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
    public static void setOnMensaLoadListener(OnMensaLoadedListener listener) {
        MensaManager.listener = listener;
    }

    /**
     * Returns a dummy mensa for test purposes
     * @return dummy mensa
     */
    @Deprecated
    @SuppressWarnings("HardCodedStringLiteral")
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
    @SuppressWarnings("HardCodedStringLiteral")
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
    public static void loadMensasForCategory(@NonNull final MensaCategory category, boolean loadFromInternet, Context ctx) {

        boolean loaded = loadCachedMensaForCategory(category, ctx);

        if(loadFromInternet || loaded)
            loadMensasForCategoryFromInternet(category, ctx);

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

    public static void clearCache(Context activityContext) {
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


    @SuppressWarnings("HardCodedStringLiteral")
    public static List<IMenu> getPlaceholderForEmptyMenu(String mensaId, Context ctx) {
        if(isFavoriteMensa(mensaId)) {
            return Collections.<IMenu>singletonList(new Menu("dummy-np-items-fav", "", ctx.getString(R.string.no_fav_msg), "", "", "dummy"));
        }
        return Collections.<IMenu>singletonList(new Menu("dummy-no-items", "", ctx.getString(R.string.no_menu_msg), "", "", "dummy"));
    }

    public static Observable loadMensasForCategoryFromInternet(final MensaCategory category, Context ctx) {

        if(category.getDisplayName().equals(favoriteMensa.getCategory().getDisplayName())){
            Log.d("MensaManager,loadfint", "Fav mensa to load");
            final OnLoadedObservable onLoadedObservable = new OnLoadedObservable(LOADABLE_CAT.length);

            for(MensaCategory cat: LOADABLE_CAT){
                if(!cat.equals((favoriteMensa.getCategory()))) {
                    loadMensasForCategoryFromInternet(cat, ctx).addObserver(new Observer() {
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
            obs = ETH_ZENT.loadMensasFromAPI(Helper.getLanguageCode(ctx));
        } else if (category instanceof  UzhMensaCategory){
            obs = UZH_ZENT.loadMensasFromAPI(Helper.getLanguageCode(ctx));
            Log.d("MensaManager,loadfint", "UZH ZENT category to load");
        } else {
            // Fallback
            obs = ETH_ZENT.loadMensasFromAPI(Helper.getLanguageCode(ctx));
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
                    addNewMensaItem(observable.getNewItems(), category, observable.day, observable.mealType, true);

                    onLoadedObservable.loadingFinished();
                }
            });
        }

        return onLoadedObservable;
    }

    public static void invalidateMensa(String mensaId) {
        Mensa mensa = IdToMensaMapping.get(mensaId);
        if(mensa != null)
            mensa.clearMenus();
    }

    public static void hideMenu(IMenu menu, String mensaId, Context activityContext) {
        Log.d("MensaManager.hideMenu", "Hiding menu: " + menu.getId() + " in mensa: " + mensaId);

        if(HIDDEN_MENU_IDS.contains(menu.getId())) {
            Log.e("MensaMan.hideMenu", "Menu allready hidden");
        } else {
            HIDDEN_MENU_IDS.add(menu.getId());
        }

        PreferenceManager.getDefaultSharedPreferences(activityContext)
                .edit()
                .putStringSet(DELETED_MENUS_STORE_ID, HIDDEN_MENU_IDS)
                .apply();

        Mensa mensa = getMensaForId(mensaId);
        if(mensa == null)
            return;


        listener.onMensaUpdated(mensa);

    }

    public static void showMenu(IMenu menu, String mensaId, Context activityContext) {
        HIDDEN_MENU_IDS.remove(menu.getId());
        Mensa mensa = getMensaForId(mensaId);

        if(mensa == null)
            return;

        listener.onMensaUpdated(mensa);


        PreferenceManager.getDefaultSharedPreferences(activityContext)
                .edit()
                .putStringSet(DELETED_MENUS_STORE_ID, HIDDEN_MENU_IDS)
                .apply();
    }

    public static void updateMenuFilter(boolean checked, Context activityContext) {
        PreferenceManager.getDefaultSharedPreferences(activityContext)
                .edit()
                .putBoolean(UPDATE_MENU_FILTER_KEY, checked)
                .apply();

        getMenuFilter(activityContext).vegi = checked;


        for(Mensa m : IdToMensaMapping.values())
            listener.onMensaUpdated(m);

    }

    public static HiddenMenuFilter getMenuFilter(Context context) {
        if(filter == null) {
            filter = new HiddenMenuFilter();
            filter.vegi = isVegiFilterEnabled(context);
        }
        return filter;
    }

    public static boolean isVegiFilterEnabled(Context activityContext) {
        return PreferenceManager.getDefaultSharedPreferences(activityContext)
                .getBoolean(UPDATE_MENU_FILTER_KEY, false);
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

        void onMensaOpeningChanged();
    }

    public static class OnLoadedObservable extends Observable {
        private int count = 0;
        private final int maxCount;

        OnLoadedObservable(int maxLoads) {
            maxCount = maxLoads;
        }


        void loadingFinished() {
            count += 1;
            if(count == maxCount) {
                setChanged();
                notifyObservers();
            }
        }

    }
}
