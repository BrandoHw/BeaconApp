package org.altbeacon.WorkTracking;


import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class TabPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Log", "Map", "Settings" };
    private Context context;
    private FragmentManager mFragmentManager;
    private Fragment mFragmentAtPos1;

    public TabPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        mFragmentManager = fm;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                LocationTimestampFragment myFragment = LocationTimestampFragment.newInstance(position + 1);
                mFragmentList.add(myFragment);
                mFragmentTitleList.add("LTS_FRAG");
                return myFragment;
            case 1:
                MapsFragment myFragment2 = MapsFragment.newInstance(position + 1);
                mFragmentList.add(myFragment2);
                mFragmentTitleList.add("MAPS_FRAG");
                return myFragment2;
            case 2:
                SettingsFragment myFragment3 = SettingsFragment.newInstance(position + 1);
                mFragmentList.add(myFragment3);
                mFragmentTitleList.add("SETTINGS_FRAG");
                return myFragment3;
            default:
                return null;

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }


    public Fragment returnFragment(String fragmentName){
        if (mFragmentTitleList.contains(fragmentName)){
            Log.i("TabPager","mFragmentTitleList check");
            return mFragmentList.get(mFragmentTitleList.indexOf(fragmentName));
        }
        return null;
    }


}
