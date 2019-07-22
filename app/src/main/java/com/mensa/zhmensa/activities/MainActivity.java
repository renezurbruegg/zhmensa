package com.mensa.zhmensa.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.InterceptAllVerticalSwipesViewPager;
import com.mensa.zhmensa.component.fragments.MensaOverviewFragment;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.navigation.NavigationExpandableListAdapter;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.HttpUtils;
import com.mensa.zhmensa.services.MensaManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends LanguageChangableActivity
        implements NavigationView.OnNavigationItemSelectedListener, MensaManager.OnMensaLoadedListener {


    private InterceptAllVerticalSwipesViewPager viewPager;


    private TextView currentDayTextView;

    private MensaUpdateModel updateModel;


    private ViewPagerAdapter viewPagerAdapter;

    private int mensaCount = 0;

    private ExpandableListView expandableListView;


    private Toolbar toolbar;

    private NavigationExpandableListAdapter expandableListAdapter;

    @Nullable
    private Mensa selectedMensa;


    public void onDestroy() {
        super.onDestroy();
    }

    private void initializeSidebarDrawer() {
        // Set up sidebar navigation
        expandableListView = findViewById(R.id.expandableListView);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View myLayout = navigationView.getHeaderView(0);
        //View myView = myLayout.findViewById( R.id.someinnerview ); // id of a view
        if (myLayout != null)
            currentDayTextView = myLayout.findViewById(R.id.current_day_navgiation);

        if (currentDayTextView != null)
            currentDayTextView.setText(String.format(getString(R.string.week_of), Helper.getHumanReadableDay(0)));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

    }

    // This flag should be set to true to enable VectorDrawable support for API < 21
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity.onCr", "on create called in main activity");

        setContentView(R.layout.activity_main);

        // Set up context for MensaManager
        MensaManager.initManager(getApplicationContext());

        updateModel = ViewModelProviders.of(this).get(MensaUpdateModel.class);

        toolbar = findViewById(R.id.toolbar);

        HttpUtils.setupClient(getApplicationContext());
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.favorites_title));

        initializeSidebarDrawer();

        expandableListAdapter = new NavigationExpandableListAdapter(getApplicationContext());

        populateExpandableList();
        // Listen on new mensa loaded callback
        MensaManager.setOnMensaLoadListener(this);


        for (MensaCategory category : MensaManager.getMensaCategories()) {
            MensaManager.loadMensasForCategory(category, true, getApplicationContext());
        }


        viewPager = findViewById(R.id.OverviewViewPager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setSaveEnabled(false);

        final SwipeRefreshLayout swipeView = findViewById(R.id.refresh_layout);

        viewPager.setAdapter(viewPagerAdapter);

       // viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Set title to current mensa name
                selectedMensa = MensaManager.getMensaForId(getMensaIdForPosition(position));

                if (selectedMensa == null) {
                    Log.e("On page change listener", "Mensa was null");
                    return;
                }
                String title = selectedMensa.getDisplayName();

                getSupportActionBar().setTitle(title);
                /*
                // Remove all listeners from viewpager
                viewPagerAdapter.removeAllDayChangedListeners();

                // Add listener if day changes, to update all hidden fragments to select current day
                ((MensaOverviewFragment) viewPagerAdapter.getItem(position)).setDayChangedListener(new MensaOverviewFragment.DayChangedListener() {
                    @Override
                    public void onDayChanged(int newDay) {
                        MensaManager.SELECTED_DAY = newDay;
                        if(dayChangedModel != null)
                            dayChangedModel.pushUpdate(Mensa.Weekday.of(newDay));
                        //viewPagerAdapter.notifyDayChanged();
                    }
                }); */
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (swipeView != null) {
                    swipeView.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
                }
            }

        });

        int padding = (int) (50 * getResources().getDisplayMetrics().density);
        swipeView.setProgressViewOffset(true, padding, (int) (padding * 1.5));
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                Log.d("Swipview", "Got refresh action + adapter: " + viewPagerAdapter == null ? "null" : "nonull");

                final Handler handler = new Handler();
                if (viewPagerAdapter != null) {
                    if(viewPagerAdapter.getCount() == 0) {
                        MensaManager.clearState();
                    }
                    //MensaManager.clearState();
                    if (selectedMensa != null) {
                        MensaManager.invalidateMensa(getSelectedMensa().getUniqueId());

//                        viewPagerAdapter.notifyFragmentForIdChanged(selectedMensa.getUniqueId());

                        final Observable onLoadedObservable = MensaManager.loadMensasForCategoryFromInternet(selectedMensa.getCategory(), getApplicationContext());
                        //viewPagerAdapter.clearCache();
                        //selectMensa(getSelectedMensa());

                        onLoadedObservable.addObserver(new Observer() {
                            @Override
                            public void update(Observable observable, Object o) {
                                Log.d("MainACt, updObs", "Updated all mensas succesfully");
                                swipeView.setRefreshing(false);
                                if (currentDayTextView != null)
                                    currentDayTextView.setText(String.format(getString(R.string.week_of), Helper.getHumanReadableDay(0)));
                                handler.removeCallbacksAndMessages(null);

                            }
                        });
                    }

                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeView.isRefreshing()) {
                            Log.d("postDelayed", "Swipe view was still refreshing after 5 seconds");

                            swipeView.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), R.string.error_download_mensas, Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        });


        selectedMensa = MensaManager.getFavoritesMensa();

/*
        ErrorMessageModel model = ViewModelProviders.of(this).get(ErrorMessageModel.class);
        model.getMessage().observe(this, new Observer<Pair<String, String>>() {
            @Override
            public void onChanged(@NonNull Pair<String, String> msg) {
                Toast.makeText(getApplicationContext(), msg.first + ": " + msg.second, Toast.LENGTH_SHORT).show();
            }
        });
*/
    }


    @Override
    public void onNewMensaLoaded(@NonNull List<Mensa> mensas) {
        Log.d("OnNewMensaLoaded", "listener triggered");

        if (expandableListAdapter != null) {
            expandableListAdapter.addAll(mensas);
            mensaCount = expandableListAdapter.getAllChildrenCount();
        } else {
            Log.e("OnNewMensaLoaded", "Adapter was null");
        }

        if (viewPagerAdapter != null) {
            viewPagerAdapter.syncMenuIds();

            viewPagerAdapter.notifyDataSetChanged();
        }

        // selectMensa(getSelectedMensa());
    }


    @Override
    public void onMensaUpdated(@NonNull Mensa mensa) {
        Log.d("MainActivity.omu", "Got update for Mensa: " + mensa.getDisplayName());

        Mensa selectedMensa = getSelectedMensa();
        Log.d("MainActivity.omu", "Got update for Mensa: " + mensa.getDisplayName());
        // Notify recycler view to reload cards for this mens
//        if (viewPagerAdapter != null)
//            viewPagerAdapter.notifyFragmentForIdChanged(mensa.getUniqueId());

        Log.d("selm,ensa", selectedMensa == null ? "null" : selectedMensa.toString());
        if (selectedMensa != null && selectedMensa.getUniqueId().equals(mensa.getUniqueId())) {
            selectMensa(selectedMensa);
        }

        if (updateModel != null)
            updateModel.pushUpdate(mensa.getUniqueId());
    }

    @Override
    public void onMensaOpeningChanged() {
        expandableListAdapter.notifyDataSetChanged();
    }


    /**
     * @return the mensa that is current selected
     */
    @Nullable
    private Mensa getSelectedMensa() {
        return selectedMensa;

    }


    /*private void reloadData(boolean loadFromInternet) {

        prepareMenuData(loadFromInternet);
    }*/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
       //
            MensaManager.clearState();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_only_vegi).setChecked(MensaManager.isVegiFilterEnabled(getBaseContext()));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            //SettingsFragment f = new SettingsFragment();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } /*else if(id == R.id.action_reload) {
            Log.d("MainACtivity-itemSel","triggering reload action");

            selectMensa(MensaManager.getFavoritesMensa());

            MensaManager.clearCache();
            viewPagerAdapter.clearCache();
            expandableListAdapter.clearCache();

            for(MensaCategory category : MensaManager.getMensaCategories()) {
                MensaManager.loadMensasForCategoryFromInternet(category);
            }

            //reloadData(true);

        } */ else if (id == R.id.action_share) {
            Log.d("MainACtivity-itemSel", "triggering share action");

            if (selectedMensa == null) {
                Log.e("MainACtivity-itemSel", "Mensa wasn null");
            } else {
                // Fetch and store ShareActionProvider
                // ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                if (selectedMensa != null) {
                    Mensa.Weekday day = Mensa.Weekday.of(MensaManager.SELECTED_DAY);
                    i.putExtra(android.content.Intent.EXTRA_TEXT, selectedMensa.getAsSharableString(day, Helper.firstNonNull(viewPagerAdapter.getSelectedMealType(selectedMensa.getUniqueId()), MensaManager.MEAL_TYPE)));
                }
                //noinspection HardCodedStringLiteral
                startActivity(Intent.createChooser(i, "Share"));
            }

        } else if(id == R.id.menu_only_vegi) {
            Log.d("v", "v gclick");

            item.setChecked(!item.isChecked());

            MensaManager.updateMenuFilter(item.isChecked(), getBaseContext());

            //viewPagerAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(viewPagerAdapter != null && viewPagerAdapter.getCount() == 0)
            MensaManager.clearState();
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity.onPause", "Pausing activity. Saving Mensa list to shared preferences");
        // Store mensa to cache
        MensaManager.storeAllMensasToCache(getBaseContext());

        super.onPause();
        // MensaManager.clearState();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity.Stop", "Stopping activity. Clearing state of mensa manager");
        //MensaManager.clearState();
    }


    /**
     * Loads all categories + mensa from factory and stores them in list/map
     */
   /* private void prepareMenuData(boolean loadFromInternet) {
        for(MensaCategory category : MensaManager.getMensaCategories()) {
            MensaManager.loadMensasForCategory(category, loadFromInternet);
        }
    } */


    /**
     * Sets up sidebar navigation. Adds items and listener to it
     */
    private void populateExpandableList() {
        // -------- Navigation Drawer -------------
        expandableListAdapter = new NavigationExpandableListAdapter(this);
        expandableListView.setAdapter(expandableListAdapter);

        // Add click listener for favorite
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (!expandableListAdapter.getGroup(groupPosition).hasChildren()) {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);

                    // TODO
                    selectMensa(MensaManager.getFavoritesMensa());
                }
                return false;
            }
        });

        // Add click listener for Mensas
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                NavigationMenuChild model = expandableListAdapter.getChild(groupPosition, childPosition);

                if (model != null) {
                    selectMensa(model.mensa);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    Log.e("MainAct.onchildclick", "Model for position group: " + groupPosition + " child " + childPosition + " was empty");
                }

                return false;
            }
        });
    }

    /**
     * Selects a given mensa and displays its menus inside the cards.
     * Gets called when a mensa from the sidebar is selected
     *
     * @param mensa the mensa which should be shown
     */
    private void selectMensa(@Nullable Mensa mensa) {
        if (mensa == null) {
            Log.d("selectMensa", "Mensa was null");
            return;
        }
        selectedMensa = mensa;

        Log.d("Select Mensa: ", "Mensa: " + mensa.toString());

        getSupportActionBar().setTitle(mensa.getDisplayName());
        int pos = getMensaPosForId(mensa.getUniqueId());
        if (pos == -1) {
            Log.e("MainActivity.select", "got invalid mensa id: " + mensa.getUniqueId());
            return;
        }

        if (viewPager.getAdapter().getCount() > pos)
            viewPager.setCurrentItem(pos);
        else
            Log.e("MainActivity.selectM", "Position : " + pos + " is not stored in veiwpager");

        onMensaSelected();
    }



    private void onMensaSelected() {
        /*
        if (shareActionProvider != null) {
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            if(selectedMensa != null) {
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, selectedMensa.getAsSharableString());
            }
     //       ctx.startActivity(Intent.createChooser(i, "Share"));

            shareActionProvider.setShareIntent(shareIntent);
        }*/
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d("ON RESTART", "RESATR");
        Log.d("on resume called--1", "on");
        Log.d("adapter:", viewPagerAdapter == null ? "null" : "notnull");
        Log.d("otheradapter", expandableListAdapter == null ? "null" : "notnull");
        // viewPagerAdapter.clearCache();

        //reloadData(false);
    }

    @Nullable
    private String getMensaIdForPosition(int position) {
      //Log.d("MainActivity", "getMensaIdForPos: pos " + position);

        if (expandableListAdapter == null) {
            Log.e("MainAct.getMensaidfp", "Expandable list adapter was null");
            return null;
        }


        return expandableListAdapter.getIdForPosition(position);
    }

    private int getMensaPosForId(@NonNull String mensaId) {

      //  Log.d("MainActivity", "getMensaIdForPos: id " + mensaId);

        if (expandableListAdapter == null) {
            Log.e("MainAct.getMensapfid", "Expandable list adapter was null");
            return -1;
        }

        return expandableListAdapter.getPositionForMensaId(mensaId);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        @NonNull
        private final Map<Integer, MensaOverviewFragment> positionToFragment;


        @SuppressLint("UseSparseArrays")
        ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            positionToFragment = new HashMap<>();
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {


            Log.d("ViewPagerAdapte.getitem", "get item called. pos: " + position);
            if (positionToFragment.get(position) == null) {

           //     Log.d("ViewPagerAdapte.getitem", "no cached item found going to get new item for id: " + getMensaIdForPosition(position));
                MensaOverviewFragment frag = MensaOverviewFragment.newInstance(getMensaIdForPosition(position));
                positionToFragment.put(position, frag);
                //        Log.d("tagxys", frag.getTag() + "") ;
            }

            // Retrun cached fragment
            return positionToFragment.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            //add this line to remove fragments wiped from ViewPager cache
            positionToFragment.remove(position);
        }

        @Nullable
        Mensa.MenuCategory getSelectedMealType(String mensaId) {

            for (MensaOverviewFragment frag : positionToFragment.values()) {
                if (frag.getMensaId().equals(mensaId)) {
                    return frag.getSelectedMealType();
                }
            }
            return null;
        }


        @Override
        public int getCount() {
            // mensa count + one favorite mensa
            // Log.d("getcount", "mc " + mensaCount);
            return mensaCount;
        }


        void syncMenuIds() {
            for (Integer key : positionToFragment.keySet()) {
                MensaOverviewFragment frag = positionToFragment.get(key);
                if (frag == null)
                    continue;

                String id = frag.getMensaId();
                String shouldBeId = getMensaIdForPosition(key);

                if (id != null && !id.equals(shouldBeId)) {
                    Log.d("MainACtivity.chmids", "Found wrong ids: " + id + " : " + shouldBeId);
                    frag.setMensaId(shouldBeId);
                    frag.notifyDatasetChanged();
                }
            }
        }
    }


    public static class DayUpdatedModel extends ViewModel {

        private final Map<String, MutableLiveData<Mensa.Weekday>> listeners = new HashMap<>();

        public DayUpdatedModel() {

        }

        @NonNull
        public MutableLiveData<Mensa.Weekday> getChangedDay(String mensaId) {
            MutableLiveData<Mensa.Weekday> list = listeners.get(mensaId);

            if (list == null) {
                list = new MutableLiveData<>();
                listeners.put(mensaId, list);
            }

            return list;
        }

        public void pushUpdate(Mensa.Weekday day, String mensaId) {
            Log.d("ViewModel", mensaId + " is pushing day update for day " + day);
            for (String key : listeners.keySet()) {
                if (!key.equals(mensaId)) {
                    getChangedDay(key).postValue(day);
                }
            }
        }
    }


    public static class MensaUpdateModel extends ViewModel {
        @NonNull
        private final MutableLiveData<String> updatedMensaId = new MutableLiveData<>();

        public MensaUpdateModel() {

        }

        @NonNull
        public MutableLiveData<String> getUpdatedMensaId() {
            return updatedMensaId;
        }

        void pushUpdate(String mensaId) {
            updatedMensaId.setValue(mensaId);
        }
    }


}
