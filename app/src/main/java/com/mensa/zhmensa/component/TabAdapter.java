package com.mensa.zhmensa.component;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that manages tabs in tab-layout
 */
class TabAdapter extends FragmentStatePagerAdapter {


    public static final String MENSA_ID = "mensaId";
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    TabAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (Fragment frag : mFragmentList) {
            if(frag instanceof MensaTab.MensaWeekdayTabFragment) {
                Log.d("TabAdapter.ndsc", "MensaWeekdayTabFragment " + frag.getId() + " mensa " + frag.getArguments().getString(MENSA_ID) );
                ((MensaTab.MensaWeekdayTabFragment) frag).notifyDatasetChanged();
            }
            else if(frag instanceof MensaTab.MenuTabContentFragment) {
                Log.d("TabAdapter.ndsc", "MensaWeekdayTabFragment " + frag.getId() + " mensa " + frag.getArguments().getString(MENSA_ID) );

                ((MensaTab.MenuTabContentFragment) frag).notifyDatasetChanged();
            }
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
