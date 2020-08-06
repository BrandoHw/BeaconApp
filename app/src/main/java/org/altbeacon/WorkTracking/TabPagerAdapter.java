package org.altbeacon.WorkTracking;


import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;

import org.altbeacon.bluetooth.AttendanceFragment;
import org.altbeacon.bluetooth.BluetoothFragment;

import java.util.ArrayList;
import java.util.List;

public class TabPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private final int PAGE_COUNT = 4;
    //private String tabTitles[] = new String[] { "Log", "Bluetooth", "Map", "Settings" };
    private String tabTitles[] = new String[] { "Log", "Bluetooth", "Record", "Settings" };
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
                BluetoothFragment btFrag = BluetoothFragment.newInstance();
                mFragmentList.add(btFrag);
                mFragmentTitleList.add("BT_FRAG");
                return btFrag;
            case 2:
//                MapsFragment myFragment2 = MapsFragment.newInstance(position + 1);
//                mFragmentList.add(myFragment2);
//                mFragmentTitleList.add("MAPS_FRAG");
                AttendanceFragment myFragment2 = AttendanceFragment.newInstance();
                mFragmentList.add(myFragment2);
                mFragmentTitleList.add("ATT_FRAG");
                return myFragment2;
            case 3:
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
