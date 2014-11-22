package ru.shika.mamkschedule.mamkschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.parse.Parse;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
{

    private ArrayList<Lesson.DrawerItem> drawerItems = new ArrayList<Lesson.DrawerItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private ActionBarDrawerToggle toggle;

    private DBHelper dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Parse init
        Parse.initialize(this, "eR4X3CWg0H0dQiykPaWPymOLuceIj7XlCWu3SLLi", "tZ8L3pIHV1nXUmXj5GASyM2JdbwKFHUDYDuqhKR7");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Database init
        dbh = new DBHelper(this);
        writeToDb();

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

        //Fragment init
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

    public void writeToDb()
    {
        SQLiteDatabase db = dbh.getWritableDatabase();

        Cursor cursor = db.query("schedule", null, null, null,null,null, null);

        Log.w("Shika", cursor.getCount() + "");

        if(cursor.getCount() > 0)
        {
            //db.delete("schedule", null, null);
            return;
        }

        for(int i = 0; i < 30; i++)
        {
            ContentValues cv = new ContentValues();
            cv.put("day", (i % 5)+1);
            cv.put("name", "PC Technology");
            cv.put("start", (int) Math.ceil(Math.random() * 20)+":00");
            cv.put("end", (int) Math.ceil(Math.random() * 20)+":00");
            cv.put("room", "Kas/MB304");
            cv.put("teacher", "Juutilainen Matti");
            db.insert("schedule", null, cv);
        }

        dbh.close();
    }


    public  static class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(Context ctx)
        {
            super(ctx, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL("create table schedule (id integer primary key autoincrement, day integer, name text, start text, end text, room text, teacher text);");
            sqLiteDatabase.execSQL("create table enrol (id integer primary key autoincrement, name text)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
        {

        }
    }

}
