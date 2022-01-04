package com.example.sdas.ViewHolder;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.sdas.Interface.DailyFragment;
import com.example.sdas.Interface.OverallFragment;
import com.example.sdas.Interface.WeeklyFragment;


import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        private Context myContext;
        int totalTabs;
        public ViewPagerAdapter(Context context, FragmentManager fm, int totalTabs) {
            super(fm);
            myContext = context;
            this.totalTabs = totalTabs;
        }
        // this is for fragment tabs
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    DailyFragment dailyFragment = new DailyFragment();
                    return dailyFragment;
                case 1:
                    WeeklyFragment weeklyFragment = new WeeklyFragment();
                    return weeklyFragment;
                case 2:
                    OverallFragment overallFragment = new OverallFragment();
                    return overallFragment;
                default:
                    return null;
            }
        }
        // this counts total number of tabs
        @Override
        public int getCount() {
            return totalTabs;
        }
}
