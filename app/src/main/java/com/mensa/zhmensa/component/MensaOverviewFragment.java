package com.mensa.zhmensa.component;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;
import com.mensa.zhmensa.services.TabManager;


public class MensaOverviewFragment extends Fragment {


    public void setMensaId(String shouldBeId) {
        getArguments().putString(MENSA_ARGUMENT, shouldBeId);
        if(isAdded())
            setUpAdapters(shouldBeId);

    }

    public Mensa.MenuCategory getSelectedMealType() {
        if(viewpager == null)
            return null;
        int selectedItem = viewpager.getCurrentItem();
        Log.d("MensaOverviewFrag", "Selected weekday is : " + selectedItem);

        if(mAdapter != null) {
           Fragment frag =  mAdapter.getItem(selectedItem);
           if(frag instanceof MensaTab.MensaWeekdayTabFragment) {
               return ((MensaTab.MensaWeekdayTabFragment ) frag).getSelectedMealType();
           }
        }
        return null;
    }

    /**
     * Listener that gets triggered when a new day is selected
     */
    public interface DayChangedListener {
        void onDayChanged(int newDay);
    }

    @Nullable
    private Mensa mensa;

    @Nullable
    private DayChangedListener dayChangedListener;

    @Nullable
    private TabAdapter mAdapter;

    @Nullable
    private NonSwipeableViewPager viewpager;

    private static final String MENSA_ARGUMENT = "mensa";

    @NonNull
    public String getMensaId() {
        if(getArguments() == null)
            return "null";

        return getArguments().getString(MENSA_ARGUMENT, "");
    }


    public void setDayChangedListener(@Nullable DayChangedListener dayChangedListener) {
        this.dayChangedListener = dayChangedListener;
    }


    /**
     * Notifies that the dataset of this fragment has changed and triggers a reload in the listener
     */
    public void notifyDatasetChanged() {
        if (mAdapter == null) {
            Log.e("MensaOverview.ndc", "MAdapter was null for mensa id: " + getMensaId());
            return;
        }
        mAdapter.notifyDataSetChanged();
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
        Log.e("onresume", "on resume called in  " + (mensa == null ? "null" : mensa.getDisplayName()));
        if (viewpager != null)
            viewpager.setCurrentItem(MensaManager.SELECTED_DAY);
        notifyDatasetChanged();
    }

    public void notifyDayChanged() {
        Log.d("notifyDayChanged", "mensa: " + (mensa == null ? "null" : mensa.getDisplayName()) + " day: " + MensaManager.SELECTED_DAY);
        if (viewpager != null) {
            viewpager.setCurrentItem(MensaManager.SELECTED_DAY);
            viewpager.getAdapter().notifyDataSetChanged();
        }
    }


    private void setUpAdapters(String mensaId) {
        mensa = MensaManager.getMensaForId(mensaId);

        if (mensa == null) {
            Log.e("MensaOverViewFramgnet", "Mensa " + mensaId + " not found");
            return;
        }

        MensaTab tab = TabManager.getTabForMensa(mensa);

        Log.d("MensaOverviewFragment", "adding fragments id: " + mensaId);

        mAdapter = new TabAdapter(getChildFragmentManager());


        for (Mensa.Weekday day : Mensa.Weekday.values()) {
            Log.d("adding", "adding day: " + day);
            mAdapter.addFragment(tab.getFragmentForWeekday(day), Helper.getNameForDay(day));
        }


        viewpager.setAdapter(mAdapter);


    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.content_main, container, false);

        if(getArguments() == null) {
            Log.e("MensaOverviewFrag.ocw", "Arguments were null for fragment");
            return rootView;
        }

        String mensaId = getArguments().getString(MENSA_ARGUMENT);
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
                if (dayChangedListener != null)
                    dayChangedListener.onDayChanged(position);
            }
        });

        return rootView;
    }

}
