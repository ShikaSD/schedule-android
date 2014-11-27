package ru.shika.mamkschedule.mamkschedule;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import org.json.JSONArray;
import ru.shika.mamkschedule.mamkschedule.Interfaces.groupFragmentCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements Interfaces.needDownload, groupFragmentCallback
{
    HashMap<String, Interfaces.Download> interfaces = new HashMap<String, Interfaces.Download>();

    private ArrayList<Lesson.DrawerItem> drawerItems = new ArrayList<Lesson.DrawerItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private ActionBarDrawerToggle toggle;

    protected static DBHelper dbh;
    protected static SharedPreferences pref;
    protected static SharedPreferences.Editor editor;

    protected Date lastUpdate;
    protected ListDownloader groupDownloader;
    protected ScheduleDownloader scheduleDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //deleteDatabase("scheduleDB");

        //Let's find last update date
        pref = getPreferences(MODE_PRIVATE);
        editor = pref.edit();
        lastUpdate = new Date(0);//pref.getLong("lastUpdate", 0));

        //Parse init
        Parse.initialize(this, "eR4X3CWg0H0dQiykPaWPymOLuceIj7XlCWu3SLLi", "tZ8L3pIHV1nXUmXj5GASyM2JdbwKFHUDYDuqhKR7");
        //looking for last updates
        groupDownloader = new ListDownloader();
        groupDownloader.execute();
        Log.w("Shika", "lastUpdate at" + lastUpdate.toString());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Database init
        dbh = new DBHelper(this);

        //Init drawer list
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.schedule),
            getResources().getDrawable(R.drawable.ic_action_paste)));
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.teachers),
            getResources().getDrawable(R.drawable.ic_action_group)));
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.edit),
            getResources().getDrawable(R.drawable.ic_action_search)));
        drawerItems.add(new Lesson.DrawerItem(getResources().getString(R.string.groups), null));

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
        Fragment fragment = ScheduleViewGroupFragment.newInstance(true, "");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .add(R.id.main_container, fragment, "My schedule")
            .addToBackStack("My schedule")
            .commit();

        interfaces.put("My schedule", (Interfaces.Download) fragment);

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

   /* @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }*/

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(scheduleDownloader != null)
            scheduleDownloader.cancel(true);
        if(groupDownloader != null)
            groupDownloader.cancel(true);
    }

    @Override
    public void groupSelected(String s)
    {
        String titles[] = {"My schedule", "Teachers", "Edit", "Groups"};

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for(int i = 0; i < titles.length; i++)
        {
            Fragment temp = getSupportFragmentManager().findFragmentByTag(titles[i]);
            if(temp != null && temp.isVisible())
            {
                ft.detach(temp);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                break;
            }
        }
        ft.add(R.id.main_container, ScheduleViewGroupFragment.newInstance(false, s), "Groups");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack("Groups");
        ft.commit();
    }

    private class onDrawerItemClickListener implements ListView.OnItemClickListener
    {
        String titles[] = {"My schedule", "Teachers", "Edit", "Groups"};

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
                if(tag.equals(getResources().getString(R.string.groups)))
                {
                    fragment = new GroupFragment();
                    interfaces.put(tag, (Interfaces.Download) fragment);
                }
                else
                {
                    //TODO: change fragments
                    fragment = ScheduleViewGroupFragment.newInstance(false, "T5614SN");
                    interfaces.put(tag, (Interfaces.Download) fragment);
                }
                fTrans.add(R.id.main_container, fragment, tag);
                Log.w("Shika", "New Fragment made");
            }

            fTrans.attach(fragment);
            fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fTrans.commit();
        }
    }

    //Interface method called from fragments
    @Override
    public void needDownload(String group, Calendar date)
    {
        date.setFirstDayOfWeek(Calendar.MONDAY);
        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Lesson[] lessons = new Lesson[7];
        String dateFormat;

        scheduleDownloader = new ScheduleDownloader();

        for(int i = 0; i < 7; i++)
        {
            dateFormat = date.get(Calendar.YEAR) - 2000 + "" +
                (date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
                (date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
            lessons[i] = new Lesson(null, null, null, null, null, dateFormat, group, 0);
            date.add(Calendar.DATE, 1);
        }

        scheduleDownloader.execute(lessons);
    }



    /*AsyncTasks classes to download group's and lesson's rows*/
    public class ListDownloader extends AsyncTask<String, Void, Void>
    {
        boolean isDatabaseEmpty = false;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.w("Shika", "GroupDownloader exec");
        }

        @Override
        protected Void doInBackground(String... params)
        {
            ArrayList<ParseObject> parseObjects;
            ArrayList<String> groupNames = new ArrayList<String>();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Groups");

            try
            {
                int amount = query.count();

                query.setLimit(amount);
                query.whereGreaterThan("createdAt", params[0]);
                query.whereDoesNotExist("last");
                query.addAscendingOrder("name");

                parseObjects = (ArrayList<ParseObject>) query.find();
                SQLiteDatabase db = dbh.getWritableDatabase();
                ContentValues cv = new ContentValues();

                for (ParseObject i : parseObjects)
                {
                    groupNames.add(i.getString("name"));
                    cv.put("name", i.getString("name"));
                    db.insert("groups", null, cv);
                }

                db.close();
            } catch (Exception e)
            {
            }

            SQLiteDatabase db = dbh.getReadableDatabase();
            Cursor c = db.query("groups", null, null, null, null, null, null);
            isDatabaseEmpty = !c.moveToNext();

            return null;
        }

        @Override
        protected void onPostExecute(Void voids)
        {
            super.onPostExecute(voids);

            long time;
            if (isDatabaseEmpty) time = 0;
            else time = Calendar.getInstance().getTimeInMillis();

            editor.putLong("lastUpdate", time);
            editor.commit();
            Log.w("Shika", "ListDownloader finish " + Calendar.getInstance().getTimeInMillis());
            dbh.close();
        }
    }

    public class ScheduleDownloader extends AsyncTask <Lesson, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.w("Shika", "ScheduleDownloader exec");
        }

        @Override
        protected String doInBackground(Lesson... lessons)
        {
            int counter = 0;
            for(Lesson lesson : lessons)
            {
                ArrayList<ParseObject> schedule;
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Schedule");
                query.whereStartsWith("group", lesson.group);
                query.whereStartsWith("date", lesson.date);

                Log.w("Shika", "Downloading: " + lesson.group + " " + lesson.date);
                try
                {
                    schedule = (ArrayList<ParseObject>) query.find();
                    SQLiteDatabase db = dbh.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    for (ParseObject i : schedule)
                    {
                        cv.put("groups", i.getString("group"));
                        cv.put("date", i.getString("date"));
                        cv.put("lesson", i.getString("lesson"));
                        cv.put("teacher", i.getString("teacher"));
                        cv.put("room", i.getString("room"));
                        cv.put("start", i.getString("start"));
                        cv.put("end", i.getString("end"));
                        cv.put("lessonId", i.getString("lessonId"));
                        db.insert("schedule", null, cv);
                        counter++;
                    }
                } catch (Exception e)
                {
                    return "error";
                }
            }

            Log.w("Shika", "Schedule counter: " + counter+"");

            if(counter == 0)
            {
                return "nothing";
            }

            return "success";
        }

        @Override
        protected void onPostExecute(String string)
        {
            super.onPostExecute(string);
            Log.w("Shika", "ScheduleDownloader finished");

            //Let's tell fragment that we have done something
            for(Interfaces.Download iFace : interfaces.values())
            {
                iFace.onDownloadEnd(string);
            }

            dbh.close();
        }
    }
}