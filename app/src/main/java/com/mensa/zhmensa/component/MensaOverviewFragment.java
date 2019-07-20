package com.mensa.zhmensa.component;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mensa.zhmensa.MainActivity;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;


public class MensaOverviewFragment extends Fragment implements Observer<Mensa.Weekday> {

    private View rootView;
    private MainActivity.DayUpdatedModel weekdayModel;

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
    public void onChanged(Mensa.Weekday weekday) {
        Log.d("MensaOv.onCh", "notify day changed for " + (mensa == null ? "null" : mensa.getUniqueId()) + " to " + weekday);
        if (viewpager != null && viewpager.getCurrentItem() != weekday.day) {
            viewpager.setCurrentItem(weekday.day);
        }
    }


    @Nullable
    private Mensa mensa;

    @Nullable
    private WeekdayFragmentAdapter mAdapter;

    @Nullable
    private NonSwipeableViewPager viewpager;

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String MENSA_ARGUMENT = "mensa";

    @NonNull
    public String getMensaId() {
        if (getArguments() == null)
            return "null";

        return getArguments().getString(MENSA_ARGUMENT, "");
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

        Bundle bdl = new Bundle();
        MensaOverviewFragment frag = new MensaOverviewFragment();
        bdl.putString(MENSA_ARGUMENT, mensaId);
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
    }


    public void onStop() {
        super.onStop();
        Log.d("MensaOVERfrag.onStop", "onStop for " + getMensaId());
        if (weekdayModel != null)
            weekdayModel.getChangedDay(getMensaId()).removeObserver(this);

    }

    public void onStart() {
        super.onStart();
        Log.d("MensaOVERfrag.onSTart", "onstart for " + getMensaId());
        weekdayModel = ViewModelProviders.of(getActivity()).get(MainActivity.DayUpdatedModel.class);
        weekdayModel.getChangedDay(getMensaId()).observe(this, this);
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
        TabLayout tabLayout = rootView.findViewById(R.id.main_tablayout);
        tabLayout.setupWithViewPager(viewpager);
        setUpAdapters(mensaId);

        viewpager.setCurrentItem(MensaManager.SELECTED_DAY);

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

        WeekdayFragmentAdapter(@NonNull FragmentManager fm, String mensaId) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mensaId = mensaId;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return MensaTab.MensaWeekdayTabFragment.getInstance(mensaId, Mensa.Weekday.of(position));
        }

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
