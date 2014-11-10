package ru.shika.mamkschedule.mamkschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
{

    private ArrayList<Lesson.DrawerItem> drawerItems = new ArrayList<Lesson.DrawerItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init drawer list
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.schedule),
            getResources().getDrawable(R.drawable.ic_action_paste)));
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.teachers),
            getResources().getDrawable(R.drawable.ic_action_group)));
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.edit),
            getResources().getDrawable(R.drawable.ic_action_search)));

        //Init drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerList = (ListView) findViewById(R.id.drawerList);

        drawerList.setAdapter(new DrawerListAdapter(this, drawerItems));
        drawerList.setOnItemClickListener(new onDrawerItemClickListener());

        //Init actionbar toggle(left button)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(toggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .add(R.id.main_container, new ScheduleViewGroupFragment(), "My schedule")
            .addToBackStack("My schedule")
            .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    private class onDrawerItemClickListener implements ListView.OnItemClickListener
    {
        String titles[] = {"My schedule", "Teachers", "Edit"};

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            replaceFragment(titles[position]);

            getSupportActionBar().setTitle(titles[position]);
            drawerLayout.closeDrawer(drawerList);
        }

        private void replaceFragment(String tag)
        {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fTrans = fm.beginTransaction();
            Fragment fragment;

            for(int i = 0; i < titles.length; i++)
            {
                Fragment temp = fm.findFragmentByTag(titles[i]);
                if(temp != null && temp.isVisible())
                {
                    fTrans.detach(temp);
                    fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    break;
                }
            }

            fragment = fm.findFragmentByTag(tag);
            if (fragment == null)
            {
                fragment = new ScheduleViewGroupFragment();
                fTrans.add(R.id.main_container, fragment, tag);
            }

            fTrans.attach(fragment);
            fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fTrans.commit();
        }
    }

}
