package com.mensa.zhmensa;

import android.os.Bundle;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.mensa.zhmensa.navigation.NavigationExpandableListAdapter;
import com.mensa.zhmensa.component.MenuCardAdapter;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.navigation.NavigationFavoritesHeader;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.navigation.NavigationMenuHeader;
import com.mensa.zhmensa.services.MensaFactory;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Main view that contains all cards.
    private RecyclerView recyclerView;

    // -------- Navigation Drawer -------------
    NavigationExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<NavigationMenuHeader> headerList = new ArrayList<>();
    Map<NavigationMenuHeader, List<NavigationMenuChild>> childList = new HashMap<>();
    // ------ End Navigation Drawer -----------------


    TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up sidebar navigation
        expandableListView = findViewById(R.id.expandableListView);
        prepareMenuData();
        populateExpandableList();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Display test mensa on startup. Likely change to favorites
        Mensa testMensa = MensaFactory.getTestMensa();
        MenuCardAdapter adapter = MenuCardAdapter.forMensa(testMensa);

        // Set up recycler view.
        recyclerView = (RecyclerView)findViewById(R.id.menus);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        // Bind title view
        titleView = (TextView) findViewById(R.id.mensa_title);

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
        headerList.add(fav);
        childList.put(fav, Collections.<NavigationMenuChild>emptyList());

        for(MensaCategory category : MensaFactory.getMensaCategories()) {
            NavigationMenuHeader headItem = new NavigationMenuHeader(category);
            headerList.add(headItem);

            ArrayList<NavigationMenuChild> catChildList = new ArrayList<>();
            for(Mensa mensa : MensaFactory.getMensaListForCategory(category)) {
                catChildList.add(new NavigationMenuChild(mensa));
            }
            childList.put(headItem, catChildList);
        }
    }

    /**
     * Sets up sidebar navigation. Adds items and listener to it
     */
    private void populateExpandableList() {

        expandableListAdapter = new NavigationExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        // Add click listener for favorite
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(headerList.get(groupPosition) instanceof NavigationFavoritesHeader) {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    selectFavorites();
                };
                return false;
            }
        });

        // Add click listener for Mensas
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                NavigationMenuChild model = childList.get(headerList.get(groupPosition)).get(childPosition);
                if (model != null) {
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
    private void selectFavorites(){
        Toast.makeText(this, "Favorites Selected", Toast.LENGTH_SHORT).show();
        // recyclerView.setAdapter(MenuCardAdapter.forMensa(mensa));
        titleView.setText("Favorites");
    }

    /**
     * Selects a given mensa and displays its menus inside the cards.
     * Gets called when a mensa from the sidebar is selected
     * @param mensa the mensa which should be shown
     */
    private void selectMensa(Mensa mensa) {
        Toast.makeText(this, mensa.getDisplayName(), Toast.LENGTH_SHORT).show();
        recyclerView.setAdapter(MenuCardAdapter.forMensa(mensa));
        titleView.setText(mensa.getDisplayName());
    }
}
