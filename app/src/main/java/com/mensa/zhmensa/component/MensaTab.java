package com.mensa.zhmensa.component;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.mensa.zhmensa.MainActivity;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.filters.HiddenMenuFilter;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.Helper;
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

class MensaTab {

    

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
    public static class MensaWeekdayTabFragment extends Fragment implements Observer<String> {

        @SuppressWarnings("HardCodedStringLiteral")
        static final String MENSA_ID = "mensaId";

        @SuppressWarnings("HardCodedStringLiteral")
        static final String WEEKDAY = "weekday";
        private TabAdapter adapter;
        private ViewPager vp;
        private View view;


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

            args.putString(MENSA_ID, mensaId);
            args.putString(WEEKDAY, gson.toJson(weekday));

            frag.setArguments(args);
            return frag;

        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            Log.d("MensaTab.WeekdayTAbFr", "Attached fragment for " + getArguments().getString(MENSA_ID));

        }

        public void onStop() {
            super.onStop();
            ViewModelProviders.of(getActivity()).get(MainActivity.MensaUpdateModel.class).getUpdatedMensaId().removeObserver(this);
        }


        public void onStart() {
            super.onStart();
            Log.d("MensaOVERfrag.onSTart", "onstart for " + getArguments().getString(MENSA_ID));

            MainActivity.MensaUpdateModel model = ViewModelProviders.of(getActivity()).get(MainActivity.MensaUpdateModel.class);
            model.getUpdatedMensaId().observe(this, this);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            Log.d("MensaTab.WeekdayTAbFr", "Detached fragment for " + getArguments().getString(MENSA_ID));
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.weekday_tab, container, false);
            vp = view.findViewById(R.id.viewpager_weekday_content);
            Log.d("MensaTab.WeekdayTAbFr", "Creating view for " + getArguments().getString(MENSA_ID));


            TabLayout tabLayout = view.findViewById(R.id.tablayout_weekday_content);
            if (getArguments() == null) {
                Log.e("MensaTab", "Arguemnts were null for fragment");
                return view;
            }

            String mensaId = getArguments().getString(MENSA_ID);
            String weekdayStr = getArguments().getString(WEEKDAY);

            if (adapter == null) {
                adapter = new TabAdapter(getChildFragmentManager());

                for (Mensa.MenuCategory category : Mensa.MenuCategory.values()) {
                    String categoryStr = new Gson().toJson(category);

                    Fragment f = MenuTabContentFragment.newInstance(mensaId, weekdayStr, categoryStr);
                    adapter.addFragment(f, Helper.getLabelForMealType(category, getContext()));
                }
            }
            // Log.e("MensaTab.ocw", "new viewpager: " + vp.toString() + "\n mensaId " + mensaId + " wekk:" + weekdayStr);


            vp.setAdapter(adapter);

            vp.setOffscreenPageLimit(0);
            tabLayout.setupWithViewPager(vp);

            return view;
        }


        @Override
        public void onResume() {
            super.onResume();
            Log.d("onResumeMensaTab", "On Resume called in mensa tab " + getArguments().getString(MENSA_ID));
        }

        void notifyDatasetChanged() {
            /*
            if(view != null)
                ((TextView)view.findViewById(R.id.update_weekday_tab)).setText("Updated " + ++numb);
            else
                Log.d("view null", "view was null");

            if (adapter == null) {
                if (getArguments() == null)
                    return;
                // View not created yet
                Log.e("MensaTab.ndc", "Adapter was null for mensa and day: " + getArguments().getString("mensaId", "null") + " " + getArguments().getString("weekday", "null") + " \n Viewpager: " + (vp == null ? "null" : vp));

                Log.d("MensaWeekdayTab", "added? " + isAdded());
                return;
            }
            Log.d("MensaWeekdayTab", "added? " + isAdded());*/
            adapter.notifyDataSetChanged();

        }

        Mensa.MenuCategory getSelectedMealType() {
            Log.d("MensaTab.getSelMT", "get selected meal type. current item : " + (vp == null ? "null" : vp.getCurrentItem()));
            if (vp != null) {
                return vp.getCurrentItem() == 0 ? Mensa.MenuCategory.LUNCH : Mensa.MenuCategory.DINNER;
            }
            return null;
        }

        @Override
        public void onChanged(String s) {
            Log.d("MensaOVfrag", "got change for " + s);
            if (s.equals(getArguments().getString(MENSA_ID))) {
                notifyDatasetChanged();
            }
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
    public static class MenuTabContentFragment extends Fragment implements Observer<String> {

        @SuppressWarnings("HardCodedStringLiteral")
        static final String MENSA_ID = "mensaId";

        @SuppressWarnings("HardCodedStringLiteral")
        static final String WEEKDAY = "weekday";

        @SuppressWarnings("HardCodedStringLiteral")
        static final String CATEGORY = "category";

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

            args.putString(MENSA_ID, mensaId);
            args.putString(WEEKDAY, weekdayStr);
            args.putString(CATEGORY, categoryStr);


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

            mensaId = getArguments().getString(MENSA_ID);
            category = new Gson().fromJson(getArguments().getString(CATEGORY), Mensa.MenuCategory.class);
            weekday = new Gson().fromJson(getArguments().getString(WEEKDAY), Mensa.Weekday.class);

            menuList = MensaManager.getMenusForIdWeekAndCat(mensaId, category, weekday);
            if (menuList.isEmpty())
                menuList = new ArrayList<>();

            adapter = new MenuCardAdapter(menuList, mensaId, MensaManager.getMenuFilter(getContext()), getContext());
            recyclerView.setAdapter(adapter);

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d("on resume", "on resume in tab: " + mensaId + " day: " + weekday + " adapter: " + (adapter == null ? "null" : "nonull"));
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.d("adapter changed", "list: " + adapter.getItems());
            }
        }


        public void onStart() {
            super.onStart();
            Log.d("MensaOVERfrag.onSTart-2", "onstart for " + getArguments().getString(MENSA_ID) + "day: " + weekday);
            MainActivity.MensaUpdateModel model = ViewModelProviders.of(getActivity()).get(MainActivity.MensaUpdateModel.class);

            model.getUpdatedMensaId().observe(this, this);
        }

        void notifyDatasetChanged() {
            if (menuList != null) {
                Log.d("MenTabContentFrag", "notifyDatasetChanged() going to rebuild set. Mensa:" + mensaId + " day: " + weekday);
                menuList.clear();
                List<IMenu> menus = MensaManager.getMenusForIdWeekAndCat(mensaId, category, weekday);

                Log.d("NotifyDatasetChanged", "Mensa id: " + mensaId + " day: " + weekday + "Menus:" + menus.toString());
                if (menus.isEmpty()) {
                    menus = MensaManager.getPlaceholderForEmptyMenu(mensaId, getContext());
                }
                menuList.addAll(menus);
            }

            if (adapter == null) {
                Log.e("MensaTab.ndc", "adapter was null for " + mensaId + category + weekday);

                Log.d("MenTabContentFrag", "New Content: " + menuList);


                return;
            }

            if (menuList != null && adapter != null) {
                adapter.setItems(menuList, getContext());
            }

            adapter.notifyDataSetChanged();

        }

        @Override
        public void onChanged(String s) {
            if (s.equals(getArguments().getString(MENSA_ID))) {
                notifyDatasetChanged();
            }
        }
    }

}
