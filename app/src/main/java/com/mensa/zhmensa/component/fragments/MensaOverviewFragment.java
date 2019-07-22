package com.mensa.zhmensa.component.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mensa.zhmensa.activities.MainActivity;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.NonSwipeableViewPager;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import org.joda.time.DateTime;


public class MensaOverviewFragment extends Fragment implements Observer<Object> {

    public static final String LAST_UPDATED = "LAST_UPDATED";

    private View rootView;

    final int modValue = 1000 * 60 * 60 * 24;

    private TabLayout tabLayout;
    private MainActivity.DayUpdatedModel weekdayModel;
    private MainActivity.MensaUpdateModel mensaModel;

    public void setMensaId(String shouldBeId) {
        getArguments().putString(MENSA_ARGUMENT, shouldBeId);
        if (isAdded())
            setUpAdapters(shouldBeId);

    }

    public Mensa.MenuCategory getSelectedMealType() {
        if (viewpager == null)
            return null;
        int selectedItem = viewpager.getCurrentItem();
        Log.d("MensaOverviewFrag", "Selected weekday is : " + selectedItem);

        if (mAdapter != null) {
            Fragment frag = mAdapter.getItem(selectedItem);
            if (frag instanceof MensaTab.MensaWeekdayTabFragment) {
                return ((MensaTab.MensaWeekdayTabFragment) frag).getSelectedMealType();
            }
        }
        return null;
    }

    @Override
    public void onChanged(Object obj){
            if(obj instanceof  Mensa.Weekday)
                onChangeDay((Mensa.Weekday) obj);
            else
                onChangeMensa((String) obj);
    }


    @Nullable
    private Mensa mensa;

    private long lastUpdated;

    @Nullable
    private WeekdayFragmentAdapter mAdapter;

    @Nullable
    private NonSwipeableViewPager viewpager;

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String MENSA_ARGUMENT = "mensa";

    public long getLastUpdated() {
        if (getArguments() == null)
            return 0;
        return getArguments().getLong(LAST_UPDATED, 0);
    }

    @NonNull
    public String getMensaId() {
        if (getArguments() == null)
            return "null";

        return getArguments().getString(MENSA_ARGUMENT, "");
    }

    private void onChangeDay(Mensa.Weekday weekday) {
        Log.d("MensaOv.onCh", "notify day changed for " + (mensa == null ? "null" : mensa.getUniqueId()) + " to " + weekday);
        if (viewpager != null && viewpager.getCurrentItem() != weekday.day) {
            viewpager.setCurrentItem(weekday.day);
        }
    }

    private void onChangeMensa(String mensaName) {

        if(mensaName.equals(getMensaId())) {
            Mensa m = MensaManager.getMensaForId(mensaName);
            if(m != null && (m.getLastUpdated() % modValue) != (getLastUpdated() % modValue)) {

                if(mAdapter != null) {
                    mAdapter.refreshUpdateTime(m.getLastUpdated());
                    if(tabLayout != null) {
                        for (int i = 0; i < tabLayout.getTabCount(); i++) {
                            TabLayout.Tab tab = tabLayout.getTabAt(i);
                            if(tab != null){
                                View v =tab.getCustomView();
                                if(v != null){
                                    TextView tv =  v.findViewById(R.id.tab_weekday_number);
                                    if(tv != null)
                                        tv.setText(Helper.getDayForPatternAndStart(mAdapter.getUpdatedDateTime().plusDays(i), "dd.MM"));
                                }
                            }
                        }
                    }

                }
            }
        }
    }



    /**
     * Notifies that the dataset of this fragment has changed and triggers a reload in the listener
     */
    public void notifyDatasetChanged() {
        Log.d("MensaOverViewFrag", " added? " + isAdded());
        if (viewpager == null) {
            Log.e("MensaOverview.ndc", "viewpager was null for mensa id: " + getMensaId());
            return;
        }

        viewpager.getAdapter().notifyDataSetChanged();
    }


    public static MensaOverviewFragment newInstance(@Nullable String mensaId) {
        Log.d("movf", "Creating frag instance for id: " + ((mensaId == null) ? "null" : mensaId));
        if (mensaId == null)
            return null;

        Mensa m = MensaManager.getMensaForId(mensaId);

        Bundle bdl = new Bundle();

        MensaOverviewFragment frag = new MensaOverviewFragment();

        bdl.putString(MENSA_ARGUMENT, mensaId);

        if(m != null) {
            bdl.putLong(LAST_UPDATED, m.getLastUpdated());
        }

        frag.setArguments(bdl);

        return frag;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("onresume", "on resume called in  " + (mensa == null ? "null" : mensa.getDisplayName()));
        Log.d("Viewpager:", "viewpager: " + (viewpager == null ? "null" : "nonull"));
        if (viewpager != null && viewpager.getCurrentItem() != MensaManager.SELECTED_DAY) {
            viewpager.setCurrentItem(MensaManager.SELECTED_DAY);
            notifyDatasetChanged();
        }
    }


    private void setUpAdapters(String mensaId) {

        mensa = MensaManager.getMensaForId(mensaId);


        if (mensa == null) {
            Log.e("MensaOverViewFramgnet", "Mensa " + mensaId + " not found");
            return;
        }

        mAdapter = new WeekdayFragmentAdapter(getChildFragmentManager(), mensaId);

        viewpager.setAdapter(mAdapter);

        LayoutInflater inflator = LayoutInflater.from(getContext());
        if(tabLayout != null) {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if(tab != null){
                    tab.setCustomView(mAdapter.getTabView(i, inflator));
                    tab.select();
                }
            }

           // tabLayout.setupWithViewPager(viewpager);
        }

    }


    public void onStop() {
        super.onStop();
        Log.d("MensaOVERfrag.onStop", "onStop for " + getMensaId());
        if (weekdayModel != null)
            weekdayModel.getChangedDay(getMensaId()).removeObserver(this);

        if(mensaModel != null)
            mensaModel.getUpdatedMensaId().removeObserver(this);

    }


    public void onStart() {
        super.onStart();

        Log.d("MensaOVERfrag.onSTart", "onstart for " + getMensaId());
        weekdayModel = ViewModelProviders.of(getActivity()).get(MainActivity.DayUpdatedModel.class);
        weekdayModel.getChangedDay(getMensaId()).observe(this, this);


        mensaModel = ViewModelProviders.of(getActivity()).get(MainActivity.MensaUpdateModel.class);
        mensaModel.getUpdatedMensaId().observe(this, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.content_main, container, false);
        else
            ((ViewGroup) rootView.getParent()).removeView(rootView);

        Log.d("MOVF.onCreateView", "on create view called mensa: " + getMensaId());

        Log.d("Viewpager: ", "VP" + (viewpager == null ? " null" : "nonull"));

        if (getArguments() == null) {
            Log.e("MensaOverviewFrag.ocw", "Arguments were null for fragment");
            return rootView;
        }

        final String mensaId = getArguments().getString(MENSA_ARGUMENT);
        if (mensaId == null) {
            Log.e("MensaOverViewFramgnet", "Got empty arguments");
            return rootView;
        }

        viewpager = rootView.findViewById(R.id.main_viewpager_week);
        tabLayout = rootView.findViewById(R.id.main_tablayout);
        tabLayout.setupWithViewPager(viewpager);
        setUpAdapters(mensaId);



        viewpager.setCurrentItem(MensaManager.SELECTED_DAY);

        viewpager.setOffscreenPageLimit(0);

        viewpager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("MEnsaOVerFrag", " page change for id " + mensaId + " new Day: " + Mensa.Weekday.of(position));

                if (weekdayModel != null)
                    weekdayModel.pushUpdate(Mensa.Weekday.of(position), mensaId);
                if (isAdded()) {
                    Log.d("MEnsaOVerFrag", " page change for id " + mensaId + " new Day: " + Mensa.Weekday.of(position));
                    MensaManager.SELECTED_DAY = position;
                }
            }
        });
        return rootView;
    }


    /**
     * Simple Adapter to show weekday fragments. Just calls newInstance
     */
    public class WeekdayFragmentAdapter extends FragmentStatePagerAdapter {

        private String mensaId;
        private DateTime updatedTime = null;
        WeekdayFragmentAdapter(@NonNull FragmentManager fm, String mensaId) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mensaId = mensaId;
        }

        Mensa getMensa() {
            if(mensa != null)
                return mensa;
            mensa = MensaManager.getMensaForId(mensaId);
            return  mensa;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return MensaTab.MensaWeekdayTabFragment.getInstance(mensaId, Mensa.Weekday.of(position));
        }

        void refreshUpdateTime(long newUpdateTime) {

            final DateTime lastUpTime = new DateTime(newUpdateTime);

            if(lastUpTime.getDayOfWeek() > 5)
                updatedTime = lastUpTime.plusWeeks(1).withDayOfWeek(1);
            else
                updatedTime = lastUpTime.withDayOfWeek(1);

        }


        private DateTime getUpdatedDateTime() {
            if(updatedTime == null) {
                Mensa m = MensaManager.getMensaForId(mensaId);
                refreshUpdateTime(m == null ? System.currentTimeMillis() : m.getLastUpdated());
            }

            return updatedTime;

        }

        View getTabView(int position, LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.custom_weekday_tab, null);

            // View Binding
            ((TextView) view.findViewById(R.id.tab_weekday_name)).setText(getPageTitle(position));
            DateTime date = getUpdatedDateTime();

            String weekday = Helper.getDayForPatternAndStart(date.plusDays(position), "dd.MM");

             ((TextView) view.findViewById(R.id.tab_weekday_number)).setText(weekday);




            /*
            TabItem item = mTabItems.get(position);

            // Data Binding
            thumb.setImageResource(item.icon);
            title.setText(item.title);
            subtitle.setText(item.subtitle); */

            return view;
        };

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return Helper.getNameForDay(Mensa.Weekday.of(position), getContext());
        }

        @Override
        public int getCount() {
            return Mensa.Weekday.values().length;
        }
    }

}
