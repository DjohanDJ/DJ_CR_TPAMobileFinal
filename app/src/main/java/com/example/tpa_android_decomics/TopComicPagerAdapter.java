package com.example.tpa_android_decomics;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TopComicPagerAdapter extends FragmentPagerAdapter {

    private int numTabs;

    public TopComicPagerAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.numTabs = numTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                TopComic t1 = new TopComic("Romance");
                return t1;
            case 1:
                TopComic t2 = new TopComic("Fantasy");
                return t2;
            case 2:
                TopComic t3 = new TopComic("Horror");
                return t3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
