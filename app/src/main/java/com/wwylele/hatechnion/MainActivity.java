package com.wwylele.hatechnion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    public String ticket, username, real;

    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        ticket = extras.getString("ticket");
        username = extras.getString("username");
        real = extras.getString("real");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(real + " (" + username + ")");
        setSupportActionBar(toolbar);


        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

    }

    class PagerAdapter extends FragmentPagerAdapter {
        String[] pageNames = {
                getString(R.string.title_fragment_grades),
                getString(R.string.title_fragment_exams)
        };
        Class[] pageFragments = {
                GradesFragment.class,
                ExamsFragment.class
        };
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            try {
                return (Fragment) pageFragments[i].newInstance();
            } catch (Exception ignored) {
                return null;
            }
        }

        @Override
        public int getCount() {
            return pageFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int i) {
            return pageNames[i];
        }
    }

}
