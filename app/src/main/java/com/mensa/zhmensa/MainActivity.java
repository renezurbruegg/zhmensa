package com.mensa.zhmensa;

import android.os.Bundle;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.mensa.zhmensa.component.MensaTab;
import com.mensa.zhmensa.component.TabAdapter;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.navigation.NavigationExpandableListAdapter;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.navigation.NavigationFavoritesHeader;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.navigation.NavigationMenuHeader;
import com.mensa.zhmensa.services.MensaManager;
import com.mensa.zhmensa.services.TabManager;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MensaManager.OnMensaLoadedListener {

    // Main view that contains all cards.
    //private RecyclerView recyclerView;
    TabLayout tabLayout;
    int selectedMensaNr = 0;
    int selectedGroupNr = 0;
    ViewPager viewPager;

    private MensaTab currentMensaTab;
    private TabAdapter adapter;

    // -------- Navigation Drawer -------------
    NavigationExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<NavigationMenuHeader> headerList = new ArrayList<>();
    Map<NavigationMenuHeader, List<NavigationMenuChild>> childList = new HashMap<>();
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
        tabLayout = findViewById(R.id.main_tablayout);
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
        viewPager = findViewById(R.id.main_viewpager);
        MensaTab tab = TabManager.getTabForMensa(MensaManager.getTestMensa());
        Log.d("adding", "adding fragments");
        for(Mensa.Weekday day : Mensa.Weekday.values()) {
            adapter.addFragment(tab.getFragmentForWeekday(day), String.valueOf(day));
        }


        Log.d("adding", "adding fragments done");
        //adapter.addFragment(TabManager.getTabForMensa(MensaManager.getTestMensa()).getFragagmentList().get(0), "Lunch");
        //adapter.addFragment(TabManager.getTabForMensa(MensaManager.getTestMensa()).getFragagmentList().get(0), "Dinner");
       // adapter.addFragment(new DummyFragment(), "t");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        reloadData();

     //    selectMensa(getSelectedMensa());
    }

    @Override
    public void onNewMensaLoaded(List<Mensa> mensas) {
        Log.d("OnNewMensaLoaded", "listener triggered");
        for(Mensa mensa: mensas) {
            for (NavigationMenuHeader header : headerList) {
                if (mensa.getCategory() == header.category) {
                    Log.d("OnNewMensaLoaded", "Found header: " + header.category.getDisplayName() + " for " + mensa.getDisplayName());
                    childList.get(header).add(new NavigationMenuChild(mensa));
                }
            }
        }

        populateExpandableList();
        selectMensa(getSelectedMensa());
    }

    public static class DummyFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment, container, false);
        }
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
        /*
        TabLayout.Tab tab = new TabLayout.Tab();
        TabLayout.Tab tab1 = tab.setCustomView(new MensaTab(null, null));
        tabLayout.addTab(new TabLayout.Tab());
        */
        //tabLayout.addV

      //  tabLayout.removeAllTabs();
        /*
        if(getSelectedMensa() != null) {
            currentMensaTab = TabManager.getTabForMensa(getSelectedMensa());
            replaceTabFragment(currentMensaTab.getFragagmentList().get(0));
        }
*/

        //recyclerView.setAdapter(MenuCardAdapter.forMensa(getSelectedMensa()));

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
       // Currently not needed since we have a custom navigation with custom listener
       /* int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/
        return true;
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

        for(MensaCategory category : MensaManager.getMensaCategories()) {

            final NavigationMenuHeader headItem = new NavigationMenuHeader(category, !category.getDisplayName().equals("Favorites"));
            headerList.add(headItem);

            Log.e("starting loading","loading mensaas for " + category.getDisplayName());
            MensaManager.loadMensasForCategory(category);
            childList.put(headItem, new ArrayList<NavigationMenuChild>());

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
     * Not implemented yet. Gets called when favorite tab from sidebar is selected
     * TODO
     */
   /* private void selectFavorites(){
        Toast.makeText(this, "Favorites Selected", Toast.LENGTH_SHORT).show();
        // recyclerView.setAdapter(MenuCardAdapter.forMensa(mensa));
        getSupportActionBar().setTitle("Favorites");
        recyclerView.setAdapter(new MenuCardAdapter(MensaManager.getFavoriteMenus()));
    }
*/
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

        Log.d("MENSA:", MensaManager.printMensa(mensa.getUniqueId()));
        Toast.makeText(this, mensa.getDisplayName() +" id: " + mensa.getUniqueId(), Toast.LENGTH_SHORT).show();
        getSupportActionBar().setTitle(mensa.getDisplayName());
        MensaTab tab = TabManager.getTabForMensa(mensa);
        Log.d("adding", "adding fragments");
        TabAdapter mAdapter = new TabAdapter(getSupportFragmentManager());

        for(Mensa.Weekday day : Mensa.Weekday.values()) {
            mAdapter.addFragment(tab.getFragmentForWeekday(day), getNameForDay(day));
        }
        viewPager.setAdapter(mAdapter);
       // currentMensaTab = TabManager.getTabForMensa(mensa);
      /*  TabAdapter mAdapter = new TabAdapter(getSupportFragmentManager());
        mAdapter.addFragment(currentMensaTab.getFragagmentList().get(0),"Lunch");
        mAdapter.addFragment(currentMensaTab.getFragagmentList().get(1),"Dinner");
        viewPager.setAdapter(mAdapter);
    */}

    private String getNameForDay(Mensa.Weekday day) {
        switch (day) {
            case MONDAY:
                return "Mo";
            case TUESDAY:
                return "Di";
            case WEDNESDAY:
                return "Mi";
            case THURSDAY:
                return "Do";
            case FRIDAY:
                return "Fr";
        }
        return String.valueOf(day);
    }
}
