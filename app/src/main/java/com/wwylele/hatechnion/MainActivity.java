package com.wwylele.hatechnion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    public String ticket, username, real;
    public boolean doTranslate;

    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doTranslate = PreferenceManager
                .getDefaultSharedPreferences(this).getBoolean("do_translate", true);

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

    private String getFragmentTag(int viewPagerId, int fragmentPosition) {
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_redo_translation) {
            HebrewTranslator.clearCache(this);
            ((GradesFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, 0))).beginTranslate();
            ((ExamsFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, 1))).beginTranslate();
            return true;
        } else if (id == R.id.action_check_translation) {
            item.setChecked(doTranslate = !item.isChecked());
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(this).edit();
            editor.putBoolean("do_translate", doTranslate);
            editor.apply();
            ((GradesFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, 0))).beginTranslate();
            ((ExamsFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, 1))).beginTranslate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_check_translation).setChecked(doTranslate);
        return true;
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
