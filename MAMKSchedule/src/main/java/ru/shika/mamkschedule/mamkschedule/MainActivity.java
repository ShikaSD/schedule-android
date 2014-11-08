package ru.shika.mamkschedule.mamkschedule;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import ru.shika.android.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity
{


    WeekPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
    SlidingTabLayout tabLayout;

    ArrayList <Fragment> fragments = new ArrayList <Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init viewpager with adapter
        sectionsPagerAdapter = new WeekPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        //Init tabs
        tabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        tabLayout.setViewPager(viewPager);
        tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.light_blue));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class WeekPagerAdapter extends FragmentPagerAdapter
    {
        public WeekPagerAdapter(FragmentManager fm)
        {
            super(fm);
            for(int i = 0; i < days.length; i++)
            {
                fragments.add(ScheduleFragment.newInstance(days[i]));
            }
        }

        @Override
        public Fragment getItem(int position)
        {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return days[position];
        }
    }

}
