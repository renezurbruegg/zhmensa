package com.mensa.zhmensa;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.mensa.zhmensa.component.MensaOverviewFragment;
import com.mensa.zhmensa.component.MensaTab;
import com.mensa.zhmensa.component.TabAdapter;
import com.mensa.zhmensa.navigation.NavigationExpandableListAdapter;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.navigation.NavigationFavoritesHeader;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.navigation.NavigationMenuHeader;
import com.mensa.zhmensa.services.MensaManager;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MensaManager.OnMensaLoadedListener {

    // Main view that contains all cards.
    //private RecyclerView recyclerView;
    //TabLayout tabLayout;
    int selectedMensaNr = 0;
    int selectedGroupNr = 0;
    ViewPager viewPager;

    private MensaTab currentMensaTab;
    private TabAdapter adapter;

    private ViewPagerAdapter viewPagerAdapter;

    private int mensaCount = 0;
    // -------- Navigation Drawer -------------
    NavigationExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<NavigationMenuHeader> headerList = new ArrayList<>();
    Map<NavigationMenuHeader, List<NavigationMenuChild>> childList = new TreeMap<>();
    // ------ End Navigation Drawer -----------------




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("start");


        MensaManager.setActivityContext(getApplicationContext());

        // Set up sidebar navigation
        expandableListView = findViewById(R.id.expandableListView);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        MensaManager.addOnMensaLoadListener(this);
        navigationView.setNavigationItemSelectedListener(this);

        // Display test mensa on startup. Likely change to favorites
        // Mensa testMensa = MensaManager.getTestMensa();
        // MenuCardAdapter adapter = new MenuCardAdapter(MensaManager.getFavoriteMenus());

        // Set up recycler view.
        //tabLayout = findViewById(R.id.main_tablayout);
        //tabLayout.add
        /*
        recyclerView = (RecyclerView)findViewById(R.id.menus);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
*       */

        adapter = new TabAdapter(getSupportFragmentManager());
        //adapter
        //<!--adapter.addFragment(new Tab1Fragment(), "Tab 1");
        //adapter.addFragment(new Tab2Fragment(), "Tab 2");
        //adapter.addFragment(new Tab3Fragment(), "Tab 3"); -->
        //viewPager.setAdapter(adapter);
        viewPager = findViewById(R.id.OverviewViewPager);

      /*  MensaTab tab = TabManager.getTabForMensa(MensaManager.getTestMensa());

        Log.d("adding", "adding fragments");

        for(Mensa.Weekday day : Mensa.Weekday.values()) {
            adapter.addFragment(tab.getFragmentForWeekday(day), String.valueOf(day));
        }


        Log.d("adding", "adding fragments done");
        //adapter.addFragment(TabManager.getTabForMensa(MensaManager.getTestMensa()).getFragagmentList().get(0), "Lunch");
        //adapter.addFragment(TabManager.getTabForMensa(MensaManager.getTestMensa()).getFragagmentList().get(0), "Dinner");
       // adapter.addFragment(new DummyFragment(), "t");
       */
       // tabLayout.setupWithViewPager(viewPager);

        reloadData();
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
     //    selectMensa(getSelectedMensa());
    }

    @Override
    public void onNewMensaLoaded(List<Mensa> mensas) {
        Log.d("OnNewMensaLoaded", "listener triggered");
        for(Mensa mensa: mensas) {
            Log.d("OnNewMensaLoaded", "Mensa: " + mensa.getDisplayName() +" cat: " + (mensa.getCategory() == null ? "null" : mensa.getCategory().getDisplayName()) + " size hrader: " + headerList.size());
            for (NavigationMenuHeader header : headerList) {
                Log.d("header:", header.toString());
                if (mensa.getCategory() != null && header.hasChildren() && mensa.getCategory().equals(header.category)) {
                    Log.d("OnNewMensaLoaded", "Found header: " + header.category.getDisplayName() + " for " + mensa.getDisplayName());
                    childList.get(header).add(new NavigationMenuChild(mensa));
                    mensaCount++;
                }
            }
        }

        populateExpandableList();
        selectMensa(getSelectedMensa());
    }

    @Override
    public void onMensaUpdated(Mensa mensa) {
        viewPagerAdapter.notifyFragmentForIdChanged(mensa.getUniqueId());
        /*if(getSelectedMensa().getUniqueId().equals(mensa.getUniqueId())) {
            selectMensa(getSelectedMensa());
        }*/
    }


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
    public boolean onOptionsItemSelected(MenuItem item) {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity.onDest", "Destroying activity. Saving Mensa list to shared preferences");
        MensaManager.storeAllMensasToCache();
        super.onDestroy();
    }

    /**
     * Loads all categories + mensa from factory and stores them in list/map
     */
    private void prepareMenuData() {
        NavigationFavoritesHeader fav = new NavigationFavoritesHeader();
        headerList.clear();
        childList.clear();

        headerList.add(fav);
        childList.put(fav, Arrays.asList(new NavigationMenuChild(MensaManager.getFavoritesMensa())));

        int pos = 0;
        for(MensaCategory category : MensaManager.getMensaCategories()) {

            final NavigationMenuHeader headItem = new NavigationMenuHeader(category, !category.getDisplayName().equals("Favorites"), pos++);
            headerList.add(headItem);

            Log.e("starting loading","loading mensaas for " + category.getDisplayName());
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
        expandableListAdapter = new NavigationExpandableListAdapter(this, headerList, childList);
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Mensa mensa = MensaManager.getMensaForId(getMensaIdForPosition(position));
                String title = mensa.getDisplayName();
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private String getMensaIdForPosition(int position) {
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
        int pos = 0;
        for(List<NavigationMenuChild> children: childList.values()) {
            for (NavigationMenuChild child: children) {
                if(mensaId.equals(child.mensa.getUniqueId())){
                    return pos;
                }
                pos ++;
            }
        }
        return -1;
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final Map<Integer, MensaOverviewFragment> positionToFragment = new HashMap<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("ViewPagerAdapte.getitem", "get item called. pos: " + position);
            if(positionToFragment.get(position) == null){
                positionToFragment.put(position, MensaOverviewFragment.newInstance(getMensaIdForPosition(position)));
            }

            // Retrun cached fragment
            return positionToFragment.get(position);
        }

        public void notifyFragmentForIdChanged(String mensaId) {
            // Do stuff
            for(MensaOverviewFragment frag: positionToFragment.values()){
                if(frag.getMensaId().equals(mensaId)){
                    frag.notifyDatasetChanged();
                }
            }
        }

        @Override
        public int getCount() {
            return mensaCount;
        }
    }


}
