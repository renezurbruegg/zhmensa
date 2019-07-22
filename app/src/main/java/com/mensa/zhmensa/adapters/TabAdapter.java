package com.mensa.zhmensa.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mensa.zhmensa.component.fragments.MensaTab;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that manages tabs in tab-layout
 */
public class TabAdapter extends FragmentStatePagerAdapter {


    public static final String MENSA_ID = "mensaId";
    private final List<MensaTab.MenuTabContentFragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public TabAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(MensaTab.MenuTabContentFragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (MensaTab.MenuTabContentFragment frag : mFragmentList) {
                Log.d("TabAdapter.ndsc", "MensaWeekdayTabFragment " + frag.getId() + " mensa " + frag.getArguments().getString(MENSA_ID) );
                frag.notifyDatasetChanged();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public int
    getCount() {
        return mFragmentList.size();
    }
}
