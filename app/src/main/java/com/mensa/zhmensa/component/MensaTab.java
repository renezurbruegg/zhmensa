package com.mensa.zhmensa.component;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.MensaManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Class that holds information for a tab that belongs to one Mensa.
 * Layout:
 * |--------------------------------------|
 * |                                      |
 * |                                      |
 * |      Mensa Content                   |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |                                      |
 * |--------------------------------------|
 * | Mo | Tu   |   We   |   Th   |   Fr   |
 * |--------------------------------------|
 */

public class MensaTab {

    /**
     * Unqiue Mensa Id that links this tab to a mensa object.
     */
    private final String mensaId;

    /**
     * Create Mensatab for a given mensaId.
     * Mensa ID must map to a mensa stored in MensaManager
     *
     * @param mensaId the unique mensa id
     */
    public MensaTab(String mensaId) {
        this.mensaId = mensaId;
    }


    /**
     * @param weekday
     * @return a fragment that contains the view for a given weekday
     */
    @NonNull
    Fragment getFragmentForWeekday(Mensa.Weekday weekday) {
        return MensaWeekdayTabFragment.getInstance(mensaId, weekday);
    }


    /**
     * Fragment view for a given day.
     * <p>
     * Layout:
     * |--------------------------------------|
     * |      Lunch        |      Dinner      |
     * |--------------------------------------|
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |             MenuTabContent           |
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |                                      |
     * |--------------------------------------|
     */
    public static class MensaWeekdayTabFragment extends Fragment {

        private TabAdapter adapter;
        private ViewPager vp;

        /**
         * Creates a new instance. This function is used, since fragments get called with an empty constructor from android OS
         *
         * @param mensaId unique mensa id that maps to a mensa in mensa manager
         * @param weekday requested weekday
         * @return Fragment that displays to tabs with menus for lunch or dinner
         */
        @NonNull
        static MensaWeekdayTabFragment getInstance(String mensaId, Mensa.Weekday weekday) {
            MensaWeekdayTabFragment frag = new MensaWeekdayTabFragment();

            Bundle args = new Bundle();
            Gson gson = new Gson();

            args.putString("mensaId", mensaId);
            args.putString("weekday", gson.toJson(weekday));

            frag.setArguments(args);
            return frag;

        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.weekday_tab, container, false);
            vp = view.findViewById(R.id.viewpager_weekday_content);


            adapter = new TabAdapter(getChildFragmentManager());
            TabLayout tabLayout = view.findViewById(R.id.tablayout_weekday_content);
            if (getArguments() == null) {
                Log.e("MensaTab", "Arguemnts were null for fragment");
                return view;
            }

            String mensaId = getArguments().getString("mensaId");
            String weekdayStr = getArguments().getString("weekday");

            Log.e("MensaTab.ocw", "new viewpager: " + vp.toString() + "\n mensaId " + mensaId + " wekk:" + weekdayStr);

            for (Mensa.MenuCategory category : Mensa.MenuCategory.values()) {
                String categoryStr = new Gson().toJson(category);

                Fragment f = MenuTabContentFragment.newInstance(mensaId, weekdayStr, categoryStr);
                adapter.addFragment(f, String.valueOf(category));
            }

            vp.setAdapter(adapter);

            tabLayout.setupWithViewPager(vp);

            return view;
        }


        void notifyDatasetChanged() {

            if (adapter == null) {
                if (getArguments() == null)
                    return;
                // View not created yet
                Log.e("MensaTab.ndc", "Adapter was null for mensa and day: " + getArguments().getString("mensaId", "null") + " " + getArguments().getString("weekday", "null"));
                return;
            }
            adapter.notifyDataSetChanged();

        }

        protected Mensa.MenuCategory getSelectedMealType() {
            Log.d("MensaTab.getSelMT", "get selected meal type. current item : " + (vp == null ? "null" : vp.getCurrentItem()));
            if(vp != null) {
                return vp.getCurrentItem() == 0 ? Mensa.MenuCategory.LUNCH : Mensa.MenuCategory.DINNER;
            }
            return null;
        }
    }

    /**
     * Fragment view for a given list of menus (Dinner / Lunch).
     * <p>
     * Layout:
     * |--------------------------------------|
     * |                                      |
     * |                                      |
     * | |----------------------------------| |
     * | |              Menu Card           | |
     * | |                                  | |
     * | |----------------------------------| |
     * | |----------------------------------| |
     * | |              Menu Card           | |
     * | |                                  | |
     * | |----------------------------------| |
     * | |----------------------------------| |
     * | |              Menu Card           | |
     * | |                                  | |
     * | |----------------------------------| |
     * |--------------------------------------|
     */
    public static class MenuTabContentFragment extends Fragment {

        @Nullable
        private MenuCardAdapter adapter;
        @Nullable
        private String mensaId;
        private Mensa.MenuCategory category;
        private Mensa.Weekday weekday;
        private List<IMenu> menuList;
        private RecyclerView recyclerView;

        /**
         * Creates a new instance. This function is used, since fragments get called with an empty constructor from android OS
         *
         * @param mensaId     unique mensa id that maps to a mensa in mensa manager
         * @param weekdayStr  requested weekday.  Gets Mapped using JSON to Weekday class
         * @param categoryStr requested category. Gets Mapped using JSON to Category class
         * @return Fragment that displays to tabs with menus for lunch or dinner
         */
        @NonNull
        static MenuTabContentFragment newInstance(String mensaId, String weekdayStr, String categoryStr) {
            Bundle args = new Bundle();

            args.putString("mensaId", mensaId);
            args.putString("weekday", weekdayStr);
            args.putString("category", categoryStr);


            MenuTabContentFragment frag = new MenuTabContentFragment();
            frag.setArguments(args);
            return frag;
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.mensa_menu_tab, container, false);
            if (getArguments() == null) {
                Log.e("MensaTab", "Arguemnts were null for fragment");
                return view;
            }

            recyclerView = view.findViewById(R.id.menusRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

            recyclerView.setItemAnimator(new DefaultItemAnimator());

            mensaId = getArguments().getString("mensaId");
            category = new Gson().fromJson(getArguments().getString("category"), Mensa.MenuCategory.class);
            weekday = new Gson().fromJson(getArguments().getString("weekday"), Mensa.Weekday.class);

            menuList = MensaManager.getMenusForIdWeekAndCat(mensaId, category, weekday);
            if(menuList.isEmpty())
                menuList = new ArrayList<>();

            adapter = new MenuCardAdapter(menuList, mensaId);
            recyclerView.setAdapter(adapter);

            return view;
        }

        @Override
        public void onResume() {
            Log.d("on resume", "on resume in tab: " + mensaId + " day: " + weekday);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.d("adapter changed", "list: " + adapter.getItems());
            }
            super.onResume();
        }

        void notifyDatasetChanged() {
            if (menuList != null) {
                Log.d("MenTabContentFrag", "notifyDatasetChanged() going to rebuild set. Mensa:" + mensaId + " day: " + weekday);
                menuList.clear();
                List<IMenu> menus = MensaManager.getMenusForIdWeekAndCat(mensaId, category, weekday);

                Log.d("NotifyDatasetChanged", "Mensa id: " + mensaId +" day: " + weekday + "Menus:" + menus.toString());
                if(menus.isEmpty()) {
                    menus = MensaManager.getPlaceholderForEmptyMenu(mensaId);
                }
                menuList.addAll(menus);
             }

            if (adapter == null) {
                Log.e("MensaTab.ndc", "adapter was null for " + mensaId + category + weekday);

                Log.d("MenTabContentFrag", "New Content: " + menuList);


                return;
            }

            if(menuList != null && adapter != null) {
                adapter.setItems(menuList);
            }

            adapter.notifyDataSetChanged();

        }
    }

}
