package com.mensa.zhmensa.component;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;
import com.mensa.zhmensa.services.TabManager;

import java.util.Arrays;

public class MensaOverviewFragment extends Fragment {


    private Mensa mensa;
    private MensaTab tab;
    private TabAdapter mAdapter;

    public String getMensaId() {
        return getArguments().getString(MENSA_ARGUMENT, "");
    }


    public void notifyDatasetChanged() {
        if(mAdapter == null){
            Log.e("MensaOverview.ndc", "MAdapter was null for mensa id: " + getMensaId());
        }
        mAdapter.notifyDataSetChanged();
    }

    public static final String MENSA_ARGUMENT = "mensa";

    public static MensaOverviewFragment newInstance(String mensaId) {
        Log.d("movf", "Creating frag instance for id: " + ((mensaId == null) ? "null" : mensaId));
        if(mensaId == null)
            return null;

        Bundle bdl = new Bundle();
        MensaOverviewFragment frag = new MensaOverviewFragment();
        bdl.putString(MENSA_ARGUMENT, mensaId);
        frag.setArguments(bdl);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.content_main, container, false);
        String mensaId = getArguments().getString(MENSA_ARGUMENT);
        if(mensaId == null) {
            Log.e("MensaOverViewFramgnet", "Got empty arguments");
            return rootView;
        }
         mensa = MensaManager.getMensaForId(mensaId);
        if (mensa == null) {
            Log.e("MensaOverViewFramgnet", "Mensa " + mensaId + " not found");
            return rootView;
        }

         tab = TabManager.getTabForMensa(mensa);

        Log.d("MensaOverviewFragment", "adding fragments id: " + mensaId);
         mAdapter = new TabAdapter(getChildFragmentManager());

        addFragmentsForWeekToAdapter();

        ViewPager viewpager =  ((ViewPager) rootView.findViewById(R.id.main_viewpager));
        viewpager.setAdapter(mAdapter);



        TabLayout tabLayout = rootView.findViewById(R.id.main_tablayout);
        tabLayout.setupWithViewPager(viewpager);
        return rootView;
    }

    private void addFragmentsForWeekToAdapter() {
        // Add Tab for every day in a week
        for(Mensa.Weekday day : Mensa.Weekday.values()) {
            Log.d("adding", "adding day: " + day);
            mAdapter.addFragment(tab.getFragmentForWeekday(day), Helper.getNameForDay(day));
        }
    }
}
