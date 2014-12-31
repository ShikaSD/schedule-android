package ru.shika.app;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.android.CalendarPickerView;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.*;


public class MainActivity extends ActionBarActivity implements Interfaces.needDownload, Interfaces.groupFragmentCallback,
                                                                ActionMode.Callback, Interfaces.dialogCallback
{
    public final static int LOADER_LIST = 1;
    public final static int LOADER_SCHEDULE = 0;
    public final static int LOADER_EDIT = 2;

    public final static int END_OF_DOWNLOAD = -1;

    public enum dialogs {DIALOG_ADD, DIALOG_REMOVE}

    protected HashMap<String, Interfaces.Download> interfaces;

    //ProgressBar in the center
    protected CircleImageView progress;
    private int mCircleWidth;
    private int mCircleHeight;
    private MaterialProgressDrawable progressDrawable;

    //Function button
    protected CircleImageView functionButton;
    protected boolean isFunctionButtonVisible;

    //Different views fo calendar
    private CalendarPickerView calendar;
    private CardView calendarContainer;

    private ArrayList<Lesson.DrawerItem> drawerItems;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    //Drawer items
    protected String[] titles;
    protected TypedArray drawables;

    private ActionBarDrawerToggle toggle;

    protected static DBHelper dbh;
    protected static int dbConnections;

    protected static SharedPreferences pref;
    protected static SharedPreferences.Editor editor;

    protected ScheduleDownloader scheduleDownloader;
    protected Handler handler;

    //Conteiner for fragment
    protected RelativeLayout container;

    protected Date globalDate = new Date();

    //BAckstack items
    protected String visibleFragmentTag;
    protected Stack<String> backStack;

    //Toast items
    private static TextView toastText;
    private static Toast toast;

    //Listeners fo function button
    private View.OnClickListener calendarButtonClick, addButtonClick;

    //We need it in actionMode callbacks
    private boolean isActionModeActive = false;
    private ActionMode actionMode;
    private String[] itemsToDelete;

    //Animations
    private Animation buttonOpen;
    private Animation buttonClose;
    private Animation calendarOpen;
    private Animation calendarClose;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        interfaces = new HashMap<String, Interfaces.Download>();

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            dbh.getWritableDatabase().rawQuery("PRAGMA automatic_index=off;", null);
            dbh.close();
        }
        dbConnections = 0;

        //Get drawer's items from resources
        titles = getResources().getStringArray(R.array.drawer_strings);
        drawables = getResources().obtainTypedArray(R.array.drawer_drawables);

        drawerItems = new ArrayList<Lesson.DrawerItem>();

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
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        {
            @Override
            public void onDrawerSlide(View view, float v)
            {
                super.onDrawerSlide(view, v);
            }

            @Override
            public void onDrawerOpened(View view)
            {
                super.onDrawerOpened(view);

                if(isFunctionButtonVisible) functionButton.startAnimation(buttonClose);
                if(calendarContainer.getVisibility() == View.VISIBLE) calendarContainer.startAnimation(calendarClose);
            }

            @Override
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);

                if(isFunctionButtonVisible) functionButton.startAnimation(buttonOpen);
            }

            @Override
            public void onDrawerStateChanged(int i)
            {
                super.onDrawerStateChanged(i);
            }
        };
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

        backStack = new Stack<String>();
        visibleFragmentTag = titles[0];
        getSupportActionBar().setTitle(titles[0]);

        //Toast init
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
            (ViewGroup) findViewById(R.id.toast_layout_root));

        toastText = (TextView) layout.findViewById(R.id.text);

        toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);

        //Init listeners
        calendarButtonClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onCalendarClick(view);
            }
        };
        addButtonClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onAddClick(view);
            }
        };

        //Don't forget finding views
        createProgressView();

        //Calendar button
        createFunctionButton();

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

                if(msg.what == END_OF_DOWNLOAD)
                {
                    //Show fragment
                    progressDrawable.stop();
                    progress.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);

                    Log.w("Shika", "ListDownloader finish " + new Date(Calendar.getInstance().getTimeInMillis()));
                }
            }
        };

        //Init animations
        animationsInit();
    }

    private void animationsInit()
    {
        buttonOpen = AnimationUtils.loadAnimation(this, R.anim.function_button_open);
        buttonOpen.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                functionButton.setVisibility(View.VISIBLE);
                isFunctionButtonVisible = true;
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        buttonClose = AnimationUtils.loadAnimation(this, R.anim.function_button_close);
        buttonClose.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                functionButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        calendarOpen = AnimationUtils.loadAnimation(this, R.anim.calendar_open);
        calendarOpen.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                calendarContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        calendarClose = AnimationUtils.loadAnimation(this, R.anim.calendar_close);
        calendarClose.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                calendarContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
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

    private void createFunctionButton()
    {
        Resources resources = getResources();

        final int circleDiameter = (int) resources.getDimension(R.dimen.function_button_diameter);

        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final int padding = (int) resources.getDimension(R.dimen.function_button_padding);
        mCircleWidth = circleDiameter;
        mCircleHeight = circleDiameter;
        final float xPosition = resources.getDimension(R.dimen.function_button_vertical_margin);
        final float yPosition = resources.getDimension(R.dimen.function_button_horizontal_margin);

        functionButton = new CircleImageView(this, getResources().getColor(R.color.light_blue), mCircleHeight / 2);

        RelativeLayout.LayoutParams lParams =
            new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lParams.setMargins(0, 0, (int) (xPosition), (int) (yPosition));

        functionButton.setLayoutParams(lParams);
        functionButton.setPadding(padding, padding, padding, padding);

        functionButton.setFocusable(true);
        functionButton.setClickable(true);

        functionButton.setImageDrawable(resources.getDrawable(R.drawable.ic_calendar));
        ((ViewGroup)(findViewById(R.id.activity_container))).addView(functionButton);

        functionButton.setOnClickListener(calendarButtonClick);
        isFunctionButtonVisible = true;
    }

    public static void showToast(String text)
    {
        toastText.setText(text);
        toast.show();
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
                    calendarContainer.startAnimation(calendarClose);
                    functionButton.startAnimation(buttonOpen);
                }

                globalDate = date;
            }

            @Override
            public void onDateUnselected(Date date)
            {

            }
        });

        calendarContainer = (CardView) findViewById(R.id.calendar_container);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if(calendarContainer.getVisibility() == View.VISIBLE)
        {
            calendarContainer.startAnimation(calendarClose);
            functionButton.startAnimation(buttonOpen);
        }
        else
        if(visibleFragmentTag.equals(titles[4]) && !isActionModeActive)
        {
            EditFragment fragment = ((EditFragment)getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            if(fragment.wasInEditMode)
            {
                ((EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag)).backPressed();
                functionButton.startAnimation(buttonOpen);
            }
            else
                super.onBackPressed();
        }
        else
        if(backStack.size() > 0)
        {
            String prevFragment = backStack.pop();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            ft.attach(getSupportFragmentManager().findFragmentByTag(prevFragment));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            visibleFragmentTag = prevFragment;

            if(visibleFragmentTag.equals(titles[4]))
            {
                functionButton.startAnimation(buttonOpen);
            }
        }
        else
            super.onBackPressed();

        if(!visibleFragmentTag.startsWith("Edit")) getSupportActionBar().setTitle(visibleFragmentTag.replace("Edit", ""));
        else getSupportActionBar().setTitle(visibleFragmentTag);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(scheduleDownloader != null)
            scheduleDownloader.cancel(true);

        dbh.close();
    }

    @Override
    public void listItemSelected(String type, String item)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;

        if(visibleFragmentTag.endsWith("ViewGroup") || visibleFragmentTag.endsWith("Edit") ||
            visibleFragmentTag.endsWith("Chooser"))
        {
            ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        }
        else
        {
            ft.detach(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        }

        backStack.push(visibleFragmentTag);

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

        getSupportActionBar().setTitle(item);
    }

    @Override
    public void listItemInEditSelected(String type, String item)
    {
        String tag = type + "Chooser";

        if(type.equals(titles[3]))
        {
            DialogAddFragment newFragment = DialogAddFragment.newInstance(item);
            newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;

        ft.detach(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        backStack.push(visibleFragmentTag);

        fragment = fm.findFragmentByTag(tag);
        if (fragment == null)
        {
            Thread thread = new Thread(new ListEditDownloader(type, item));
            thread.start();

            fragment = ListFragment.newEditInstance(tag, item);
            interfaces.put(tag, (Interfaces.Download) fragment);
            ft.add(R.id.main_container, fragment, tag);
            Log.w("Shika", "New Fragment made");
        }

        ft.attach(fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        visibleFragmentTag = tag;

        getSupportActionBar().setTitle(item);
    }

    @Override
    public void editTypeSelected(String tag)
    {
        tag += "Edit";

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;

        ft.detach(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        backStack.push(visibleFragmentTag);

        fragment = fm.findFragmentByTag(tag);
        if (fragment == null)
        {
            Thread thread = new Thread(new ListDownloader(tag.replace("Edit", "")));
            thread.start();

            fragment = ListFragment.newInstance(tag.replace("Edit", ""), true);
            interfaces.put(tag, (Interfaces.Download) fragment);
            ft.add(R.id.main_container, fragment, tag);
            Log.w("Shika", "New Fragment made");
        }

        ft.attach(fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        visibleFragmentTag = tag;

        getSupportActionBar().setTitle(tag.replace("Edit", ""));
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
    {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.edit_actionmode, menu);

        if(visibleFragmentTag.equals(titles[4]))
        {
            EditFragment fragment = (EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag);
            fragment.showCheckboxes(true);
        }

        isActionModeActive = true;

        functionButton.startAnimation(buttonClose);
        isFunctionButtonVisible = false;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
    {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
    {
        this.actionMode = actionMode;
        switch (menuItem.getItemId())
        {
            case R.id.delete:
                deleteItems();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode)
    {
        if(visibleFragmentTag.equals(titles[4]))
        {
            ((EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag)).showCheckboxes(false);
        }

        isActionModeActive = false;
        functionButton.startAnimation(buttonOpen);
    }

    public void deleteItems()
    {
        itemsToDelete = ((EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag)).getChecked();

        DialogFragment fragment = DialogCallbackFragment.newInstance(getResources().getString(R.string.dialog_delete_title),
            getResources().getString(R.string.dialog_delete_text));
        fragment.show(getSupportFragmentManager(), "DialogDelete");
    }

    @Override
    public void dialogDone(dialogs dialogs)
    {
        switch (dialogs)
        {
            case DIALOG_ADD:
                getSupportLoaderManager().getLoader(LOADER_EDIT).forceLoad();
                return;

            case DIALOG_REMOVE:

                actionMode.finish();
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SQLiteDatabase db = dbh.getWritableDatabase();
                        dbConnections++;

                        ContentValues cv = new ContentValues();
                        cv.put("isEnrolled", 0);

                        for (String item : itemsToDelete)
                        {
                            Log.w("Shika", item);
                            String where = "courseId = '" + item + "' or (name = '" + item + "' and courseId = '')";
                            db.update("Courses", cv, where, null);
                        }

                        closeDatabase(db);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                ((EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag)).update();
                            }
                        });
                    }
                });

                thread.start();
                break;
        }
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

            if(visibleFragmentTag.endsWith("ViewGroup") || visibleFragmentTag.endsWith("Edit") ||
                visibleFragmentTag.endsWith("Chooser"))
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
                else if(tag.equals(titles[titles.length - 1]))
                {
                    fragment = new EditFragment();
                }
                else
                {
                    Thread thread = new Thread(new ListDownloader(tag));
                    thread.start();

                    fragment = ListFragment.newInstance(tag, false);
                    interfaces.put(tag, (Interfaces.Download) fragment);
                }
                ft.add(R.id.main_container, fragment, tag);
                Log.w("Shika", "New Fragment made");
            }

            if(tag.endsWith("Chooser") || tag.contains("Edit"))
            {
                functionButton.setOnClickListener(addButtonClick);
                functionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_new));
                int p = (int) getResources().getDimension(R.dimen.function_button_padding);
                functionButton.setPadding(p, p, p, p);
            }
            else
            {
                functionButton.setOnClickListener(calendarButtonClick);
                functionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar));
                int p = (int) getResources().getDimension(R.dimen.function_button_padding);
                functionButton.setPadding(p, p, p, p);
            }

            ft.attach(fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            visibleFragmentTag = tag;

            //Clear back stack, as we started new category
            backStack.clear();
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
        for(int i = 0; i < 5; i++)
        {
            dateFormat = date.get(Calendar.YEAR) - 2000 + "" +
                (date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
                (date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
            lessons[i] = new Lesson(null, null, course, teacher, null, dateFormat, group, 0);
            date.add(Calendar.DATE, 1);
        }

        scheduleDownloader.execute(lessons);
    }



    /*AsyncTasks classes to download group's and lesson's rows*/
    public class ListDownloader implements Runnable
    {
        boolean isDatabaseEmpty = false;
        String param;
        Date lastUpdate;

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
                int downloaded = 0;
                String lastDownloaded = "";

                if(lastUpdate.getTime() > 0)
                    query.whereGreaterThan("createdAt", lastUpdate);
                else
                {
                    downloaded = pref.getInt(param + "downloaded", 0);
                    lastDownloaded = pref.getString(param + "last", "");
                }

                int amount = query.count();

                while (amount > downloaded)
                {
                    query.setLimit(50);

                    query.whereGreaterThan("name", lastDownloaded);

                    query.whereDoesNotExist("last");
                    query.addAscendingOrder("name");

                    parseObjects = (ArrayList<ParseObject>) query.find();
                    SQLiteDatabase db = dbh.getWritableDatabase();
                    dbConnections++;
                    ContentValues cv = new ContentValues();

                    for (ParseObject i : parseObjects)
                    {
                        String name = i.getString("name");
                        cv.put("name", name);

                        if(param.equals("Courses"))
                        {
                            cv.put("courseId", i.getString("courseId"));
                            cv.put("groups", i.getString("group"));
                            cv.put("teacher", i.getString("teacher"));

                            Cursor x = db.rawQuery("select count(*) from Courses where courseId = '" +
                                i.getString("courseId") + "' and name = '" + i.getString("name") + "'", null);
                            if(x.moveToFirst())
                                if(x.getInt(0) > 0)
                                {
                                    continue;
                                }
                        }

                        db.insert(param, null, cv);

                        downloaded++;
                        lastDownloaded = i.getString("name");
                    }

                    editor.putInt(param + "downloaded", downloaded);
                    editor.putString(param + "last", lastDownloaded);
                    editor.commit();

                    if(parseObjects.size() == 0)
                        break;

                    closeDatabase(db);
                    try
                    {
                        handler.sendEmptyMessage(downloaded);
                    }
                    catch (Exception e){e.printStackTrace();}
                }
            } catch (Exception e)
            {
                if(handler != null) showToast("Network error occured. Please check your internet connection");
                e.printStackTrace();
            }

            SQLiteDatabase db = dbh.getReadableDatabase();
            dbConnections++;
            
            Cursor c = db.query(param, null, null, null, null, null, null);
            isDatabaseEmpty = !c.moveToNext();

            closeDatabase(db);

            //Remove progress button
            try
            {
                handler.sendEmptyMessage(END_OF_DOWNLOAD);
            }
            catch (Exception e){e.printStackTrace();}

            long time;
            if (isDatabaseEmpty) time = 0;
            else time = Calendar.getInstance().getTimeInMillis();

            editor.putLong(param+"lastUpdate", time);
            editor.commit();
        }
    }

    public class ListEditDownloader implements Runnable
    {

        private String type;
        private String name;
        private Date lastUpdate;

        private boolean isNecessary;

        public ListEditDownloader(String type, String name)
        {
            Log.w("Shika", "ListEditDownloader exec");
            this.type = type;
            this.name = name;
            isNecessary = true;

            progressDrawable.start();
            progress.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
        }

        @Override
        public void run()
        {
            if (!isNecessary)
                return;

            ArrayList<ParseObject> parseObjects;
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Courses");

            try
            {
                int amount = query.count();
                query.setLimit(amount);
                query.addAscendingOrder("name");

                if(type.equals("Groups"))
                    query.whereEqualTo("group", name);

                if(type.equals("Teachers"))
                    query.whereEqualTo("teacher", name);

                parseObjects = (ArrayList<ParseObject>) query.find();
                Log.w("Shika", parseObjects.size() + " type:" + type);

                insertValues(parseObjects);
            }
            catch (Exception e){if(handler != null) showToast("Network error occured. Please check your internet connection");}

            try
            {
                handler.sendEmptyMessage(END_OF_DOWNLOAD);
            }
            catch (Exception e){e.printStackTrace();}
        }

        public void insertValues(ArrayList<ParseObject> parseObjects)
        {
            SQLiteDatabase db = dbh.getWritableDatabase();
            dbConnections++;
            
            ContentValues cv = new ContentValues();

            for (ParseObject i : parseObjects)
            {
                Cursor x = db.rawQuery("select count(*) from Courses where courseId = '" +
                    i.getString("courseId") + "' and name = '" + i.getString("name") + "'", null);
                if(x.moveToFirst())
                    if(x.getInt(0) > 0)
                    {
                        continue;
                    }

                cv.put("courseId", i.getString("courseId"));
                cv.put("groups", i.getString("group"));
                cv.put("teacher", i.getString("teacher"));
                cv.put("name", i.getString("name"));
                db.insert("Courses", null, cv);
            }

            handler.sendEmptyMessage(parseObjects.size());

            closeDatabase(db);
        }
    }

    public class ScheduleDownloader extends AsyncTask <Lesson, Void, String>
    {
        boolean isMySchedule;
        ArrayList<ParseObject> schedule;
        int counter;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.w("Shika", "ScheduleDownloader exec");

            isMySchedule = visibleFragmentTag.equals(titles[0]);

            counter = 0;
        }

        @Override
        protected String doInBackground(Lesson... lessons)
        {
            if(isMySchedule)
            {
                Log.w("Shika", "We have own schedule here " + visibleFragmentTag);
                return downloadSchedule(lessons);
            }

            for(Lesson lesson : lessons)
            {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Lessons");
                query.addAscendingOrder("name");

                if(lesson == null){ Log.w("Shika", "lesson == null"); continue;}

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

                parseQuery(query);
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

            if(string.equals("no courses"))
            {
                showToast("There are no courses in your schedule list. Please add them in edit schedule section.");
            }


            //Let's tell fragment that we have done something
            for(Interfaces.Download iFace : interfaces.values())
            {
                Fragment temp = (Fragment) iFace;
                if(temp.isVisible())
                    iFace.onDownloadEnd(string);
            }
        }

        protected String parseQuery(ParseQuery <ParseObject> query)
        {
            try
            {
                schedule = (ArrayList<ParseObject>) query.find();
                SQLiteDatabase db = dbh.getWritableDatabase();
                dbConnections++;
                
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
                closeDatabase(db);
            } catch (Exception e)
            {
                if(handler != null) showToast("Network error occured. Please check your internet connection");
            }

            return null;
        }

        protected String downloadSchedule(Lesson... lessons)
        {
            SQLiteDatabase db = dbh.getReadableDatabase();
            dbConnections++;
            
            Cursor c = db.rawQuery("select * from Courses where isEnrolled = 1", null);

            if(!c.moveToNext())
                return "no courses";

            int courseId = c.getColumnIndex("courseId");
            int name = c.getColumnIndex("name");
            int teacher = c.getColumnIndex("teacher");
            int group = c.getColumnIndex("groups");

            ParseQuery <ParseObject> query = new ParseQuery<ParseObject>("Lessons");
            for(Lesson lesson : lessons)
            {
                Log.w("Shika", "date: " + lesson.date);
                c.moveToFirst();
                do
                {
                    db = dbh.getReadableDatabase();
                    dbConnections++;
                    
                    Cursor x = db.rawQuery("select count(*) from Schedule where lesson = '" + c.getString(name) +
                        "' and courseId = '" + c.getString(courseId) + "' and date = '" + lesson.date + "'", null);
                    if(x.moveToFirst())
                        if(x.getInt(0) > 0)
                        {
                            counter++;
                            continue;
                        }

                    query.whereEqualTo("name", c.getString(name));
                    query.whereEqualTo("courseId", c.getString(courseId));
                    query.whereEqualTo("date", lesson.date);
                    query.whereEqualTo("teacher", c.getString(teacher));
                    query.whereEqualTo("group", c.getString(group));
                    query.addAscendingOrder("name");

                    parseQuery(query);

                }
                while (c.moveToNext());
            }

            if(counter == 0)
            {
                return "nothing";
            }

            return "success";
        }
    }

    public void onCalendarClick(View view)
    {
        if(calendarContainer.getVisibility() == View.GONE)
        {
            calendarContainer.startAnimation(calendarOpen);
            functionButton.startAnimation(buttonClose);
        }
    }

    public void onAddClick(View view)
    {
        EditFragment fragment = (EditFragment) getSupportFragmentManager().findFragmentByTag(titles[4]);
        fragment.addClick(view);

        functionButton.startAnimation(buttonClose);
        isFunctionButtonVisible = false;
    }

    public void setGlobalDate(Date date)
    {
        globalDate = date;

        calendar.selectDate(date);
    }

    public void closeDatabase(SQLiteDatabase db)
    {
        if(dbConnections > 0)
            return;
        closeDatabase(db);
    }
}