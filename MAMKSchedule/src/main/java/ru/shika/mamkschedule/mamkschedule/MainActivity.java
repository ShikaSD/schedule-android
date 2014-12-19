package ru.shika.mamkschedule.mamkschedule;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.android.CalendarPickerView;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.mamkschedule.mamkschedule.Interfaces.groupFragmentCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements Interfaces.needDownload, groupFragmentCallback
{
    protected HashMap<String, Interfaces.Download> interfaces = new HashMap<String, Interfaces.Download>();

    protected CircleImageView progress;
    private int mCircleWidth;
    private int mCircleHeight;
    private MaterialProgressDrawable progressDrawable;

    protected CircleImageView calendarButton;
    CalendarPickerView calendar;

    private ArrayList<Lesson.DrawerItem> drawerItems = new ArrayList<Lesson.DrawerItem>();
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private ActionBarDrawerToggle toggle;

    protected String[] titles;
    protected TypedArray drawables;

    protected static DBHelper dbh;
    protected static SharedPreferences pref;
    protected static SharedPreferences.Editor editor;

    protected Date lastUpdate;
    protected ListDownloader listDownloader;
    protected ScheduleDownloader scheduleDownloader;
    protected Handler handler;

    protected RelativeLayout container;

    protected Date globalDate = new Date();

    protected String visibleFragmentTag;
    protected String prevFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //For debug
       // deleteDatabase("scheduleDB");

        //Let's find last update date
        pref = getPreferences(MODE_PRIVATE);
        editor = pref.edit();

        //Parse init
        Parse.initialize(this, "eR4X3CWg0H0dQiykPaWPymOLuceIj7XlCWu3SLLi", "tZ8L3pIHV1nXUmXj5GASyM2JdbwKFHUDYDuqhKR7");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Database init
        dbh = new DBHelper(this);

        //Get drawer's items from resources
        titles = getResources().getStringArray(R.array.drawer_strings);
        drawables = getResources().obtainTypedArray(R.array.drawer_drawables);

        //TODO: Find icons for the drawer
        //Init drawer list
        for (int i = 0; i < titles.length; i++)
        {
            drawerItems.add(new Lesson.DrawerItem(titles[i], drawables.getDrawable(i)));
        }
        //Recycle that array after using
        drawables.recycle();

        //Init drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer_list);

        drawerList.setAdapter(new DrawerListAdapter(this, drawerItems));
        drawerList.setOnItemClickListener(new onDrawerItemClickListener());

        //Init actionbar toggle(left button)
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(toggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Fragment init
        Fragment fragment = ScheduleViewGroupFragment.newInstance(true, null, null, null, globalDate);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .add(R.id.main_container, fragment, titles[0])
            .commit();

        interfaces.put(titles[0], (Interfaces.Download) fragment);

        visibleFragmentTag = titles[0];

        //Don't forget finding views
        createProgressView();

        //Calendar button
        createCalendarButton();

        //CalendarView init
        calendarInit();

        container = (RelativeLayout) findViewById(R.id.main_container);

        //Init threads handler
        handler = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                for (Interfaces.Download iFace : interfaces.values())
                    iFace.updateInProgress(msg.what);

                progressDrawable.stop();
                progress.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);

                if(msg.what == -1)
                {
                    //Show fragment
                    progressDrawable.stop();
                    progress.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);

                    Log.w("Shika", "ListDownloader finish " + new Date(Calendar.getInstance().getTimeInMillis()));
                }
            }
        };
    }

    private void createProgressView() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (56 * metrics.density);
        mCircleHeight = (int) (56 * metrics.density);

        progress = new CircleImageView(this, getResources().getColor(R.color.background), 56/2);
        progressDrawable = new MaterialProgressDrawable(this, progress);

        RelativeLayout.LayoutParams lParams =
            new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progress.setLayoutParams(lParams);

        progressDrawable.updateSizes(MaterialProgressDrawable.LARGE);
        progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
        progress.setImageDrawable(progressDrawable);

        ((ViewGroup)(findViewById(R.id.activity_container))).addView(progress, 0);
        progress.setVisibility(View.GONE);
    }

    private void createCalendarButton()
    {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int padding = 10;
        mCircleWidth = (int) (56 * metrics.density);
        mCircleHeight = (int) (56 * metrics.density);

        calendarButton = new CircleImageView(this, getResources().getColor(R.color.light_blue), 56/2);

        RelativeLayout.LayoutParams lParams =
            new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lParams.setMargins(0, 0, 30, 30);

        calendarButton.setLayoutParams(lParams);
        calendarButton.setPadding(padding, padding, padding, padding);

        calendarButton.setFocusable(true);
        calendarButton.setClickable(true);

        calendarButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar));
        ((ViewGroup)(findViewById(R.id.activity_container))).addView(calendarButton);

        calendarButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onCalendarClick(view);
            }
        });
    }

    private void calendarInit()
    {
        Calendar until = Calendar.getInstance();
        until.add(Calendar.MONTH, 5);
        until.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar from = Calendar.getInstance();
        from.set(2014, Calendar.SEPTEMBER, 1);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(from.getTime(), until.getTime())
            .withSelectedDate(today);
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
        if(calendarButton.getVisibility() == View.GONE)
        {
            FrameLayout container = (FrameLayout) findViewById(R.id.container_calendar);
            container.setVisibility(View.GONE);
            calendarButton.setVisibility(View.VISIBLE);
        }
        else
        if(visibleFragmentTag.endsWith("ViewGroup"))
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            ft.attach(getSupportFragmentManager().findFragmentByTag(prevFragment));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            visibleFragmentTag = prevFragment;

            prevFragment = null;
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(scheduleDownloader != null)
            scheduleDownloader.cancel(true);
        if(listDownloader != null)
            handler.removeCallbacks(listDownloader);
    }

    @Override
    public void listSelected(String type, String item)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;

        if(visibleFragmentTag.endsWith("ViewGroup"))
        {
            ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        }
        else
        {
            ft.detach(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        }

        prevFragment = visibleFragmentTag;

        if(type.equals(titles[1]))
            fragment = ScheduleViewGroupFragment.newInstance(false, item, null, null, globalDate);
        else
        if(type.equals(titles[2]))
            fragment = ScheduleViewGroupFragment.newInstance(false, null, item, null, globalDate);
        else
        if(type.equals(titles[3]))
            fragment = ScheduleViewGroupFragment.newInstance(false, null, null, item, globalDate);

        interfaces.put(type+"ViewGroup", (Interfaces.Download) fragment);
        ft.add(R.id.main_container, fragment, type+"ViewGroup");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(type);
        ft.commit();
        visibleFragmentTag = type+"ViewGroup";
    }

    private class onDrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            replaceFragment(titles[position]);

            getSupportActionBar().setTitle(titles[position]);
            drawerLayout.closeDrawer(drawerList);
        }

        private void replaceFragment(String tag)
        {
            if(visibleFragmentTag.equals(tag))
                return;

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment;

            if(visibleFragmentTag.endsWith("ViewGroup"))
            {
                ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            }
            else
            {
                ft.detach(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            }

            fragment = fm.findFragmentByTag(tag);
            if (fragment == null)
            {
                if(tag.equals(titles[0]))
                {
                    fragment = ScheduleViewGroupFragment.newInstance(true, null, null, null, globalDate);
                    interfaces.put(tag, (Interfaces.Download) fragment);
                }
                else
                {
                    Thread thread = new Thread(new ListDownloader(tag));
                    thread.start();

                    fragment = ListFragment.newInstance(tag);
                    interfaces.put(tag, (Interfaces.Download) fragment);
                }
                ft.add(R.id.main_container, fragment, tag);
                Log.w("Shika", "New Fragment made");
            }

            ft.attach(fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            visibleFragmentTag = tag;
        }
    }

    //Interface method called from fragments
    @Override
    public void needDownload(String group, String teacher, String course, Calendar date)
    {
        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Lesson[] lessons = new Lesson[5];
        String dateFormat;

        Log.w("Shika", "in needDownload");
        scheduleDownloader = new ScheduleDownloader();

        //Adding different items (some dirty code)
        if(group != null)
        {
            for(int i = 0; i < 5; i++)
            {
                dateFormat = date.get(Calendar.YEAR) - 2000 + "" +
                    (date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
                    (date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
                lessons[i] = new Lesson(null, null, null, null, null, dateFormat, group, 0);
                date.add(Calendar.DATE, 1);
            }
        }
        else
        if(teacher != null)
        {
            for(int i = 0; i < 5; i++)
            {
                dateFormat = date.get(Calendar.YEAR) - 2000 + "" +
                    (date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
                    (date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
                lessons[i] = new Lesson(null, null, null, null, teacher, dateFormat, null, 0);
                date.add(Calendar.DATE, 1);
            }
        }
        else
        if(course !=  null)
        {
            for(int i = 0; i < 5; i++)
            {
                dateFormat = date.get(Calendar.YEAR) - 2000 + "" +
                    (date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
                    (date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
                lessons[i] = new Lesson(null, null, null, course, null, dateFormat, null, 0);
                date.add(Calendar.DATE, 1);
            }
        }

        scheduleDownloader.execute(lessons);
    }



    /*AsyncTasks classes to download group's and lesson's rows*/
    public class ListDownloader implements Runnable
    {
        boolean isDatabaseEmpty = false;
        String param;

       public ListDownloader(String param)
        {
            //Show progress bar
            progressDrawable.start();
            progress.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);

            this.param = param;

            Log.w("Shika", "ListDownloader exec");
        }

        @Override
        public void run()
        {
            Log.w("Shika", "Param is: "+param);

            //looking for last updates
            lastUpdate = new Date(pref.getLong(param+"lastUpdate", 0));
            Log.w("Shika", "lastUpdate at " + lastUpdate.toString());

            ArrayList<ParseObject> parseObjects;
            ParseQuery<ParseObject> query = ParseQuery.getQuery(param);

            try
            {
                if(lastUpdate.getTime() > 0)
                    query.whereGreaterThan("createdAt", lastUpdate);

                int amount = query.count();

                int downloaded = 0;
                String lastDownloaded = "";

                while (amount > downloaded)
                {
                    query.setLimit(50);

                    query.whereGreaterThan("name", lastDownloaded);

                    query.whereDoesNotExist("last");
                    query.addAscendingOrder("name");

                    parseObjects = (ArrayList<ParseObject>) query.find();
                    SQLiteDatabase db = dbh.getWritableDatabase();
                    ContentValues cv = new ContentValues();

                    for (ParseObject i : parseObjects)
                    {
                        if(param.equals("Courses"))
                        {
                            cv.put("courseId", i.getString("courseId"));
                            cv.put("groups", i.getString("group"));
                            cv.put("teacher", i.getString("teacher"));
                        }

                        String name = i.getString("name");
                        cv.put("name", name);
                        db.insert(param, null, cv);

                        downloaded++;
                        lastDownloaded = i.getString("name");
                    }

                    if(parseObjects.size() == 0)
                        break;

                    db.close();
                    handler.sendEmptyMessage(downloaded);
                }
            } catch (Exception e)
            {
                return;
            }

            SQLiteDatabase db = dbh.getReadableDatabase();
            Cursor c = db.query(param, null, null, null, null, null, null);
            isDatabaseEmpty = !c.moveToNext();

            db.close();

            //Remove progress button
            handler.sendEmptyMessage(-1);

            long time;
            if (isDatabaseEmpty) time = 0;
            else time = Calendar.getInstance().getTimeInMillis();

            editor.putLong(param+"lastUpdate", time);
            editor.commit();
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
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Lessons");

                if(lesson == null){ Log.w("Shika", "lesson = null"); continue;}

                if(lesson.group != null)
                    query.whereStartsWith("group", lesson.group);
                else
                if(lesson.teacher != null)
                    query.whereStartsWith("teacher", lesson.teacher);
                else
                if(lesson.name != null)
                    query.whereStartsWith("name", lesson.name);

                query.whereStartsWith("date", lesson.date);

                Log.w("Shika", "Downloading in ScheduleDownloader from " + lesson.date);
                try
                {
                    schedule = (ArrayList<ParseObject>) query.find();
                    SQLiteDatabase db = dbh.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    for (ParseObject i : schedule)
                    {
                        cv.put("groups", i.getString("group"));
                        cv.put("date", i.getString("date"));
                        cv.put("lesson", i.getString("name"));
                        cv.put("teacher", i.getString("teacher"));
                        cv.put("room", i.getString("room"));
                        cv.put("start", i.getString("start"));
                        cv.put("end", i.getString("end"));
                        cv.put("lessonId", i.getString("lessonId"));
                        cv.put("courseId", i.getString("courseId"));
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

    public void onCalendarClick(View view)
    {
        FrameLayout container = (FrameLayout) findViewById(R.id.container_calendar);

        if(container.getVisibility() == View.GONE)
        {
            container.setVisibility(View.VISIBLE);
            calendarButton.setVisibility(View.GONE);
        }

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener()
        {
            @Override
            public void onDateSelected(Date date)
            {
                for(Interfaces.Download iFace : interfaces.values())
                {
                    Fragment temp = (Fragment)iFace;
                    if(temp.isVisible())
                        iFace.onDateChanged(date);

                    calendar.selectDate(date, true);
                }

                //globalDate = date;
            }

            @Override
            public void onDateUnselected(Date date)
            {

            }
        });
    }
}