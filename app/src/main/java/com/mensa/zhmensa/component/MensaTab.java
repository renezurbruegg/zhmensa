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

import java.util.List;

public class MensaTab {
   private final String mensaId;

    public MensaTab(String mensaId){
        this.mensaId = mensaId;
    }

    public Fragment getFragmentForWeekday(Mensa.Weekday weekday) {
        return MensaWeekdayTabFragment.getInstance(mensaId, weekday);
    }








    public static class MensaWeekdayTabFragment extends  Fragment {

        public static MensaWeekdayTabFragment getInstance(String mensaId, Mensa.Weekday weekday) {
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
            ViewPager vp = view.findViewById(R.id.viewpager_weekday_content);


            TabAdapter adapter = new TabAdapter(getChildFragmentManager());
            TabLayout tabLayout = view.findViewById(R.id.tablayout_weekday_content);

            String mensaId = getArguments().getString("mensaId");
            String weekdayStr = getArguments().getString("weekday");


            // Mensa mensa = MensaManager.getMensaForId(getArguments().getString("mensaId"));

            for(Mensa.MenuCategory category : Mensa.MenuCategory.values()) {
                String categoryStr = new Gson().toJson(category);
                Fragment f = MenuTabContentFragment.newInstance(mensaId, weekdayStr, categoryStr);
                adapter.addFragment(f, String.valueOf(category));
                Log.e("add", "adding categories to frag");
            }

            vp.setAdapter(adapter);
            tabLayout.setupWithViewPager(vp);

            return view;
        }

    }

    public static class MenuTabContentFragment extends Fragment {


        public static MenuTabContentFragment newInstance(String mensaId, String weekdayStr, String categoryStr){
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
         //   return inflater.inflate(R.layout.fragment, container, false);

            RecyclerView recyclerView = view.findViewById(R.id.menusRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            String mensaId = getArguments().getString("mensaId");
            Mensa.MenuCategory category = new Gson().fromJson(getArguments().getString("category"), Mensa.MenuCategory.class);
            Mensa.Weekday weekday = new Gson().fromJson(getArguments().getString("weekday"), Mensa.Weekday.class);

            List<IMenu> menuList = MensaManager.getMenusForIdWeekAndCat(mensaId, category, weekday);


            Log.e("Fuck this shit", menuList.size() + " _ size");

          recyclerView.setAdapter(new MenuCardAdapter(menuList));
          //recyclerView.();
          return view;
        }
    }
}
