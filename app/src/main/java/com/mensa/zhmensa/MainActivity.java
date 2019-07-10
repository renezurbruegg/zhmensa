package com.mensa.zhmensa;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.mensa.zhmensa.component.InterceptAllVerticalSwipesViewPager;
import com.mensa.zhmensa.component.MensaOverviewFragment;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.navigation.NavigationExpandableListAdapter;
import com.mensa.zhmensa.navigation.NavigationFavoritesHeader;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.navigation.NavigationMenuHeader;
import com.mensa.zhmensa.services.MensaManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MensaManager.OnMensaLoadedListener{



    private int selectedMensaNr = 0;
    private int selectedGroupNr = 0;
    private InterceptAllVerticalSwipesViewPager viewPager;

    private ViewPagerAdapter viewPagerAdapter;

    private int mensaCount = 0;
    private ExpandableListView expandableListView;
    private List<NavigationMenuHeader> headerList = new ArrayList<>();
    private Map<NavigationMenuHeader, List<NavigationMenuChild>> childList = new TreeMap<>();

    // ------ End Navigation Drawer -----------------

    private Toolbar toolbar;

    //public final static MensaOverviewFragment.WeekdayObservable WEEKDAY_SELECTION_OBSERVABLE = new MensaOverviewFragment.WeekdayObservable();




    private void initializeSidebarDrawer() {
        // Set up sidebar navigation
        expandableListView = findViewById(R.id.expandableListView);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Zürich Mensa");

        initializeSidebarDrawer();

        // Set up context for MensaManager
        MensaManager.setActivityContext(getApplicationContext());
        // Listen on new mensa loaded callback
        MensaManager.addOnMensaLoadListener(this);

        viewPager = findViewById(R.id.OverviewViewPager);

        reloadData();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Set title to current mensa name
                Mensa mensa = MensaManager.getMensaForId(getMensaIdForPosition(position));
                String title = mensa.getDisplayName();

                getSupportActionBar().setTitle(title);

                // Remove all listeners from viewpager
                viewPagerAdapter.removeAllDayChangedListeners();

                // Add listener if day changes, to update all hidden fragments to select current day
                ((MensaOverviewFragment) viewPagerAdapter.getItem(position)).setDayChangedListener(new MensaOverviewFragment.DayChangedListener() {
                    @Override
                    public void onDayChanged(int newDay) {
                        MensaManager.SELECTED_DAY = newDay;
                        viewPagerAdapter.notifyDayChanged();
                    }
                });
            }

        });

        ErrorMessageModel model = ViewModelProviders.of(this).get(ErrorMessageModel.class);
        model.getMessage().observe(this, new Observer<Pair<String, String>>() {
            @Override
            public void onChanged(Pair<String, String> msg) {
                Toast.makeText(getApplicationContext(), msg.first + ": " + msg.second, Toast.LENGTH_SHORT);
            }
        });


    }

    @Override
    public void onNewMensaLoaded(List<Mensa> mensas) {
        Log.d("OnNewMensaLoaded", "listener triggered");
        for(Mensa mensa: mensas) {
            Log.d("OnNewMensaLoaded", "Mensa: " + mensa.getDisplayName() +" cat: " + (mensa.getCategory() == null ? "null" : mensa.getCategory().getDisplayName()) + " size hrader: " + headerList.size());
            boolean found = false;
            for (NavigationMenuHeader header : headerList) {
                Log.d("header:", header.toString());
                if (mensa.getCategory() != null && header.hasChildren() && mensa.getCategory().equals(header.category)) {
                    found = true;
                    Log.d("OnNewMensaLoaded", "Found header: " + header.category.getDisplayName() + " for " + mensa.getDisplayName());
                    childList.get(header).add(new NavigationMenuChild(mensa));
                    mensaCount++;

                }
            }
            if(!found){
                Log.d("onnml", "Not found for mensa: "  + mensa.toString());
            }


            if(viewPagerAdapter != null)
                viewPagerAdapter.notifyDataSetChanged();
        }

        populateExpandableList();

        selectMensa(getSelectedMensa());
    }


    @Override
    public void onMensaUpdated(Mensa mensa) {
        Log.d("MainActivity.omu", "Got update for Mensa: " + mensa.getDisplayName());
        // Notify recycler view to reload cards for this mensa
        viewPagerAdapter.notifyFragmentForIdChanged(mensa.getUniqueId());

        Mensa selectedMensa = getSelectedMensa();
        if(selectedMensa != null && selectedMensa.getUniqueId().equals(mensa.getUniqueId())){
            selectMensa(selectedMensa);
        }
    }


    /**
     *
     * @return the mensa that is current selected
     */
    @Nullable
    private Mensa getSelectedMensa() {
        NavigationMenuHeader header = headerList.get(selectedGroupNr);
        if(childList.get(header) != null && childList.get(header).size() != 0) {
            return childList.get(header).get(selectedMensaNr).mensa;
        }
        return null;
    }


    public void reloadData() {
        prepareMenuData();
        populateExpandableList();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity.onDest", "Destroying activity. Saving Mensa list to shared preferences");
        // Store mensa to cache
        MensaManager.storeAllMensasToCache();

    }

    /**
     * Loads all categories + mensa from factory and stores them in list/map
     */
    private void prepareMenuData() {
        NavigationFavoritesHeader fav = new NavigationFavoritesHeader();

        headerList.clear();
        childList.clear();

        headerList.add(fav);
        childList.put(fav, Collections.singletonList(new NavigationMenuChild(MensaManager.getFavoritesMensa())));

        int pos = 0;
        for(MensaCategory category : MensaManager.getMensaCategories()) {

            final NavigationMenuHeader headItem = new NavigationMenuHeader(category, !category.getDisplayName().equals("Favorites"), pos++);
            headerList.add(headItem);

            Log.d("starting loading","loading mensaas for " + category.getDisplayName());
            childList.put(headItem, new ArrayList<NavigationMenuChild>());
        }

        for(MensaCategory category : MensaManager.getMensaCategories()) {
            MensaManager.loadMensasForCategory(category);
        }
    }



    /**
     * Sets up sidebar navigation. Adds items and listener to it
     */
    private void populateExpandableList() {
        Log.d("tostr", childList.toString());
        // -------- Navigation Drawer -------------
        NavigationExpandableListAdapter expandableListAdapter = new NavigationExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        // Add click listener for favorite
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(!headerList.get(groupPosition).hasChildren()) {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);

                    selectMensa(childList.get(headerList.get(groupPosition)).get(0).mensa);
                }
                return false;
            }
        });

        // Add click listener for Mensas
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                NavigationMenuChild model = childList.get(headerList.get(groupPosition)).get(childPosition);
                if (model != null) {
                    selectedGroupNr = groupPosition;
                    selectedMensaNr = childPosition;
                    selectMensa(model.mensa);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
                return false;
            }
        });
    }

    /**
     * Selects a given mensa and displays its menus inside the cards.
     * Gets called when a mensa from the sidebar is selected
     * @param mensa the mensa which should be shown
     */
    private void selectMensa(Mensa mensa) {
        if(mensa == null) {
            Log.d("selectMensa", "Mensa was null");
            return;
        }

        Log.d("Select Mensa: ", "Mensa: " + mensa.getDisplayName());

        getSupportActionBar().setTitle(mensa.getDisplayName());
        int pos = getMensaPosForId(mensa.getUniqueId());
        if(pos == -1) {
            Log.e("MainActivity.select", "got invalid mensa id: " + mensa.getUniqueId());
            return;
        }

        viewPager.setCurrentItem(pos);

    }

    private String getMensaIdForPosition(int position) {
        Log.d("MainActivity", "getMensaIdForPos: pos " +position);
        for(List<NavigationMenuChild> child: childList.values()) {
            if(child.size() <= position) {
                position -= child.size();
            } else {
                return child.get(position).mensa.getUniqueId();
            }
        }
        return null;
    }

    private int getMensaPosForId(String mensaId) {
        Log.d("MainActivity", "getMensaIdForPos: id " + mensaId);
        int pos = 0;
        for(List<NavigationMenuChild> children: childList.values()) {
            for (NavigationMenuChild child: children) {
                if(mensaId.equals(child.mensa.getUniqueId())){
                    Log.d("MainActivity", "getMensaIdForPos: pos " + pos);
                    return pos;
                }
                pos ++;
            }
        }
        return -1;
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final Map<Integer, MensaOverviewFragment> positionToFragment;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            positionToFragment = new HashMap<>();


        }

        @Override
        public Fragment getItem(int position) {

            Log.d("ViewPagerAdapte.getitem", "get item called. pos: " + position);
            if(positionToFragment.get(position) == null){

                Log.d("ViewPagerAdapte.getitem", "no cached item found");
                positionToFragment.put(position, MensaOverviewFragment.newInstance(getMensaIdForPosition(position)));
            }

            // Retrun cached fragment
            return positionToFragment.get(position);
        }

        void notifyFragmentForIdChanged(String mensaId) {
            // Do stuff
            for(MensaOverviewFragment frag: positionToFragment.values()){
                if(frag.getMensaId().equals(mensaId)){
                    Log.d("MainActivity.notifyfrag","found mensa fragment going to notify changes");
                    frag.notifyDatasetChanged();
                }
            }
           notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // mensa count + one favorite mensa
            return mensaCount + 1;
        }

        void removeAllDayChangedListeners() {
            for(MensaOverviewFragment f : positionToFragment.values())
                f.setDayChangedListener(null);
        }

        void notifyDayChanged() {
            Log.d("mact,ndc", "ndc");
            for(MensaOverviewFragment f : positionToFragment.values())
                f.notifyDayChanged();
        }
    }


    public class ErrorMessageModel extends ViewModel {
        private MutableLiveData<Pair<String,String>> errorMessage = new MutableLiveData<Pair<String,String>>();

        public MutableLiveData<Pair<String,String>>  getMessage() {
            return errorMessage;
        }

        public void pushMessage(String mensa, String error) {
            errorMessage.setValue(new Pair(mensa, error));
        }
    }


}
