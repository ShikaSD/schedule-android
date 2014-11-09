package ru.shika.mamkschedule.mamkschedule;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
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

        toolbar.setNavigationIcon(R.drawable.ic_drawer);

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

        //Init actionbar toggle(left button)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(toggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .add(R.id.main_container, new ScheduleViewGroupFragment())
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

}
