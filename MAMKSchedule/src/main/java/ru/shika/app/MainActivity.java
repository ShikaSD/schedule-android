package ru.shika.app;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.parse.ParseCrashReporting;
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

    public enum Dialogs
    {DIALOG_ADD, DIALOG_REMOVE}

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

    //Container for fragment
    protected RelativeLayout container;

    protected Date globalDate = new Date();

    //Backstack items
    protected String visibleFragmentTag;
    protected String visibleFragmentTagParam;
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
        ParseCrashReporting.enable(this);
        Parse.initialize(this, "eR4X3CWg0H0dQiykPaWPymOLuceIj7XlCWu3SLLi", "tZ8L3pIHV1nXUmXj5GASyM2JdbwKFHUDYDuqhKR7");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Database init
        dbh = new DBHelper(this);
        dbConnections = 0;

        //Get drawer's items from resources
        titles = getResources().getStringArray(R.array.drawer_strings);
        drawables = getResources().obtainTypedArray(R.array.drawer_drawables);

        drawerItems = new ArrayList<Lesson.DrawerItem>();

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

        backStack = new Stack<String>();
        //Fragment init
        if(savedInstanceState == null)
        {
            visibleFragmentTag = "";
            showFragment(titles[0], titles[0]);
            backStack.clear();
        }
        else
        {
            visibleFragmentTag = savedInstanceState.getString("Fragment");
            visibleFragmentTagParam = savedInstanceState.getString("FragmentParam");
            showFragment(visibleFragmentTag, visibleFragmentTagParam);
        }

        //Toast init
        /*LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
            (ViewGroup) findViewById(R.id.toast_layout_root));

        toastText = (TextView) layout.findViewById(R.id.text);

        toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);*/

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

                if(msg.obj != null && interfaces.containsKey(msg.obj))
                {
                    interfaces.get(msg.obj).updateInProgress(msg.what);
                }
                else
                    for (Interfaces.Download iFace : interfaces.values())
                        iFace.updateInProgress(msg.what);

                if(progress.getVisibility() == View.VISIBLE)
                    Log.d("Shika", "Visible");
                else
                    Log.d("Shika", "invissible");
              // dismissProgressView();

                if(msg.what == END_OF_DOWNLOAD)
                {
                    //Show fragment
                   // dismissProgressView();

                    Log.d("Shika", "ListDownloader finish " + new Date(Calendar.getInstance().getTimeInMillis()));
                }
            }
        };

        //Init animations
        animationsInit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putString("Fragment", visibleFragmentTag);
        outState.putString("FragmentParam", visibleFragmentTagParam);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Resources resources = getResources();

        //TODO:change configuration with 820dp changes
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            calendarContainer.getLayoutParams().width = (int) resources.getDimension(R.dimen.calendar_width);
        else
            calendarContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null)
        {
            visibleFragmentTag = savedInstanceState.getString("Fragment");
            visibleFragmentTagParam = savedInstanceState.getString("FragmentParam");
            showFragment(visibleFragmentTag, visibleFragmentTagParam);
        }
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

        FrameLayout.LayoutParams lParams =
            new FrameLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.gravity = Gravity.CENTER;
        progress.setLayoutParams(lParams);

        progressDrawable.updateSizes(MaterialProgressDrawable.LARGE);
        progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
        progress.setImageDrawable(progressDrawable);

        ((ViewGroup) findViewById(R.id.main_container_parent)).addView(progress);
        progress.setVisibility(View.GONE);
    }

    private void createFunctionButton()
    {
        Resources resources = getResources();

        final int circleDiameter = (int) resources.getDimension(R.dimen.function_button_diameter);

        final int padding = (int) resources.getDimension(R.dimen.function_button_padding);
        mCircleWidth = circleDiameter;
        mCircleHeight = circleDiameter;
        final float xPosition = resources.getDimension(R.dimen.function_button_vertical_margin);
        final float yPosition = resources.getDimension(R.dimen.function_button_horizontal_margin);

        functionButton = new CircleImageView(this, getResources().getColor(R.color.orange_accent), mCircleHeight / 2);

        RelativeLayout.LayoutParams lParams =
            new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lParams.setMargins(0, 0, (int) (xPosition), (int) (yPosition));

        functionButton.setLayoutParams(lParams);
        functionButton.setPadding(padding, padding, padding, padding);

        functionButton.setFocusable(true);
        functionButton.setClickable(true);
        functionButton.setAdjustViewBounds(true);

        functionButton.setImageDrawable(resources.getDrawable(R.drawable.ic_calendar));
        ((ViewGroup)(findViewById(R.id.activity_container))).addView(functionButton);

        functionButton.setOnClickListener(calendarButtonClick);
        isFunctionButtonVisible = true;

        Log.w("Shika", getResources().getDisplayMetrics().xdpi + "");
    }

    public void showToast(String text)
    {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
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

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            calendarContainer.getLayoutParams().width = (int) getResources().getDimension(R.dimen.calendar_width);
        else
            calendarContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
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
            return;
        }

        if(visibleFragmentTag.equals(titles[4]) && !isActionModeActive)
        {
            EditFragment fragment = ((EditFragment)getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            if(fragment.wasInEditMode)
            {
                ((EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag)).backPressed();
                functionButton.startAnimation(buttonOpen);
                return;
            }
        }

        if(!backStack.empty())
        {
            dismissProgressView();

            String prevFragment = backStack.pop();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            ft.attach(getSupportFragmentManager().findFragmentByTag(prevFragment));
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();

            visibleFragmentTag = prevFragment;
            visibleFragmentTagParam = visibleFragmentTag.replace("Edit", "").replace("ViewGroup", "").replace("Chooser", "");

            if(visibleFragmentTag.equals(titles[4]))
            {
                functionButton.startAnimation(buttonOpen);
            }

            if(!visibleFragmentTag.startsWith("Edit")) getSupportActionBar().setTitle(visibleFragmentTag.replace("Edit", ""));
            else getSupportActionBar().setTitle(visibleFragmentTag);
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

        dbh.close();
    }

    public void showFragment(String tag, String param)
    {
        if(isActionModeActive)
            actionMode.finish();

        if(visibleFragmentTag.equals(tag))
            return;

        String title = param;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;

        if(visibleFragmentTag.endsWith("ViewGroup") || visibleFragmentTag.endsWith("Chooser"))
        {
            ft.remove(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
            ft.setTransition(FragmentTransaction.TRANSIT_EXIT_MASK);
        }
        else
        {
            if(!visibleFragmentTag.equals(""))
                ft.detach(getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));

            ft.setTransition(FragmentTransaction.TRANSIT_EXIT_MASK);
        }

        backStack.push(visibleFragmentTag);

        fragment = fm.findFragmentByTag(tag);
        if (fragment == null)
        {
            //If it's a ViewGroup in Groups/Teachers/Courses
            if(tag.endsWith("ViewGroup"))
            {
                String type = tag.replace("ViewGroup", "");
                if(type.equals(titles[1]))
                    fragment = ScheduleViewGroupFragment.newInstance(false, param, null, null, globalDate);
                else
                if(type.equals(titles[2]))
                    fragment = ScheduleViewGroupFragment.newInstance(false, null, param, null, globalDate);
                else
                if(type.equals(titles[3]))
                    fragment = ScheduleViewGroupFragment.newInstance(false, null, null, param, globalDate);
            }
            else
            //If it is a Chooser list
            if(tag.endsWith("Chooser"))
            {
                String type = tag.replace("Chooser", "");

                Thread thread = new Thread(new ListEditDownloader(type, param));
                thread.start();

                fragment = ListFragment.newEditInstance(tag, param);
            }
            else
            //If it is a list of Groups/Teachers/Courses in Edit Fragment
            if(tag.endsWith("Edit"))
            {
                String type = tag.replace("Edit", "");

                //Show progress bar
                showProgressView();

                Thread thread = new Thread(new ListDownloader(type));
                thread.start();

                fragment = ListFragment.newInstance(type, true);

                title = type;
            }
            else
            //Or main categories
            if(tag.equals(titles[0]))
            {
                fragment = ScheduleViewGroupFragment.newInstance(true, null, null, null, globalDate);
            }
            else
            if(tag.equals(titles[titles.length - 1]))
            {
                fragment = new EditFragment();
            }
            else
            {
                Thread thread = new Thread(new ListDownloader(tag));
                thread.start();

                //Show progress bar
                showProgressView();

                fragment = ListFragment.newInstance(tag, false);
            }
            if(!tag.startsWith("Edit"))
                interfaces.put(tag, (Interfaces.Download) fragment);

            ft.add(R.id.main_container, fragment, tag);
            Log.d("Shika", "New Fragment made");
        }

        ft.attach(fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.commit();

        visibleFragmentTag = tag;
        visibleFragmentTagParam = param;

        getSupportActionBar().setTitle(title);
    }

    @Override
    public void listItemSelected(String type, String item)
    {
        showFragment(type + "ViewGroup", item);
    }

    @Override
    public void listItemInEditSelected(String type, String item)
    {
        if(type.equals(titles[3]))
        {
            DialogAddFragment newFragment = DialogAddFragment.newInstance(item, getSupportActionBar().getTitle().toString());
            newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            return;
        }

        String tag = type + "Chooser";
        showFragment(tag, item);
    }

    @Override
    public void editTypeSelected(String tag)
    {
        showFragment(tag+"Edit", tag);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
    {
        this.actionMode = actionMode;

        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.edit_actionmode, menu);

        if(visibleFragmentTag.equals(titles[4]))
        {
            EditFragment fragment = (EditFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag);
            fragment.showCheckboxes(true);
        }

        actionMode.setTitle("Delete items");

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
    public void dialogDone(Dialogs dialogs)
    {
        int loader;
        if(visibleFragmentTag.equals("Courses"))
            loader = LOADER_LIST;
        else
            loader = LOADER_EDIT;

        switch (dialogs)
        {
            case DIALOG_ADD:
                ListFragment fragment = ((ListFragment) getSupportFragmentManager().findFragmentByTag(visibleFragmentTag));
                fragment.from = 0;
                if (getSupportLoaderManager().getLoader(loader) == null)
                    getSupportLoaderManager().initLoader(loader, null, fragment);
                else
                    getSupportLoaderManager().getLoader(loader).forceLoad();

                showToast("Course added to your schedule");
                return;

            case DIALOG_REMOVE:

                actionMode.finish();
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SQLiteDatabase db = dbh.getWritableDatabase();
                        addDBConnection();

                        ContentValues cv = new ContentValues();
                        cv.put("isEnrolled", 0);

                        for (String item : itemsToDelete)
                        {
                            String where = "courseId = ? or (name = ? and courseId = '')";
                            db.update("Courses", cv, where, new String[]{item, item});
                        }

                        closeDatabase();

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

            showFragment(tag, tag);

            functionButton.setVisibility(View.VISIBLE);
            if(tag.endsWith("Chooser") || tag.contains("Edit"))
            {
                functionButton.setOnClickListener(addButtonClick);
                functionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_new));
                int p = (int) getResources().getDimension(R.dimen.function_button_add_padding);
                functionButton.setPadding(p, p, p, p);

                if(getSupportFragmentManager().findFragmentByTag(titles[4]) != null)
                    ((EditFragment) getSupportFragmentManager().findFragmentByTag(titles[4])).wasInEditMode = false;
            }
            else
            {
                functionButton.setOnClickListener(calendarButtonClick);
                functionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar));
                int p = (int) getResources().getDimension(R.dimen.function_button_padding);
                functionButton.setPadding(p, p, p, p);
            }
            functionButton.setVisibility(View.INVISIBLE);
            isFunctionButtonVisible = true;

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

        if(scheduleDownloader != null)
            scheduleDownloader.cancel(true);

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
        private boolean isDatabaseEmpty;
        private String param;
        private Date lastUpdate;
        private Message msg;

        public ListDownloader(String param)
        {
            this.param = param;
            isDatabaseEmpty = false;

            Log.d("Shika", "ListDownloader exec");
        }

        @Override
        public void run()
        {
            Log.d("Shika", "Param is: " + param);

            //looking for last updates
            lastUpdate = new Date(pref.getLong(param+"lastUpdate", 0));
            Log.d("Shika", "lastUpdate at " + lastUpdate.toString());

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
                    ContentValues cv = new ContentValues();

                    for (ParseObject i : parseObjects)
                    {
                        SQLiteDatabase db = dbh.getWritableDatabase();
                        addDBConnection();

                        if(param.equals("Courses"))
                        {
                            Cursor x = db.rawQuery("select count(*) from Courses where courseId = ? and name = ? and " +
                                    "groups = ? and teacher = ?",
                                new String[]{i.getString("courseId"), i.getString("name"), i.getString("group"), i.getString("teacher")});
                            if(x.moveToFirst())
                                if(x.getInt(0) > 0)
                                {
                                    closeDatabase();
                                    continue;
                                }

                            cv.put("courseId", i.getString("courseId"));
                            cv.put("groups", i.getString("group"));
                            cv.put("teacher", i.getString("teacher"));
                        }

                        String name = i.getString("name");
                        cv.put("name", name);

                        db.insert(param, null, cv);

                        downloaded++;
                        lastDownloaded = i.getString("name");
                        closeDatabase();
                    }

                    editor.putInt(param + "downloaded", downloaded);
                    editor.putString(param + "last", lastDownloaded);
                    editor.commit();

                    if(parseObjects.size() == 0)
                        break;

                    try
                    {
                        msg = handler.obtainMessage(downloaded, param);
                        handler.sendMessage(msg);
                    }
                    catch (Exception e){e.printStackTrace();}
                }
            } catch (Exception e)
            {
                if(handler != null)
                {
                    Toast.makeText(MainActivity.this, "Network error occured. Please check your internet connection", Toast.LENGTH_SHORT).show();
                    msg = handler.obtainMessage(END_OF_DOWNLOAD, param);
                    handler.sendMessage(msg);
                }

                return;
            }

            SQLiteDatabase db = dbh.getReadableDatabase();
            addDBConnection();
            
            Cursor c = db.query(param, null, null, null, null, null, null);
            isDatabaseEmpty = !c.moveToNext();

            closeDatabase();

            //Remove progress button
            try
            {
                msg = handler.obtainMessage(END_OF_DOWNLOAD, param);
                handler.sendMessage(msg);
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

        private boolean isNecessary;

        public ListEditDownloader(String type, String name)
        {
            Log.d("Shika", "ListEditDownloader exec");
            this.type = type;
            this.name = name;
            isNecessary = true;

            showProgressView();
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

                insertValues(parseObjects);
            }
            catch (Exception e){if(getParent() != null) Toast.makeText(MainActivity.this, "Network error occured. " +
                "Please check your internet connection", Toast.LENGTH_SHORT).show();}

            try
            {
                handler.sendEmptyMessage(END_OF_DOWNLOAD);
            }
            catch (Exception e){e.printStackTrace();}
        }

        public void insertValues(ArrayList<ParseObject> parseObjects)
        {
            SQLiteDatabase db = dbh.getWritableDatabase();
            addDBConnection();
            
            ContentValues cv = new ContentValues();

            for (ParseObject i : parseObjects)
            {
                Cursor x = db.rawQuery("select count(*) from Courses where courseId = ? and name = ? and " +
                        "groups = ? and teacher = ?",
                    new String[]{i.getString("courseId"), i.getString("name"), i.getString("group"), i.getString("teacher")});
                if(x.moveToFirst())
                    if(x.getInt(0) > 0)
                    {
                        closeDatabase();
                        continue;
                    }

                cv.put("courseId", i.getString("courseId"));
                cv.put("groups", i.getString("group"));
                cv.put("teacher", i.getString("teacher"));
                cv.put("name", i.getString("name"));
                db.insert("Courses", null, cv);
            }

            handler.sendEmptyMessage(parseObjects.size());

            closeDatabase();
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
            Log.d("Shika", "ScheduleDownloader exec");

            isMySchedule = visibleFragmentTag.equals(titles[0]);

            counter = 0;
        }

        @Override
        protected String doInBackground(Lesson... lessons)
        {
            if(isMySchedule)
            {
                //Log.w("Shika", "We have own schedule here " + visibleFragmentTag);
                return downloadSchedule(lessons);
            }

            for(Lesson lesson : lessons)
            {
                //TODO: lessonsid with update... Parse base update
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Lessons");
                query.addAscendingOrder("name");

                if(lesson == null){ Log.d("Shika", "lesson == null"); continue;}

                if(lesson.group != null)
                    query.whereStartsWith("group", lesson.group);
                else
                if(lesson.teacher != null)
                    query.whereStartsWith("teacher", lesson.teacher);
                else
                if(lesson.name != null)
                    query.whereStartsWith("name", lesson.name);

                query.whereStartsWith("date", lesson.date);

                //Log.w("Shika", "Downloading in ScheduleDownloader from " + lesson.date);

                parseQuery(query);
            }

            //Log.w("Shika", "Schedule counter: " + counter+"");

            if(counter == 0)
            {
                return "nothing";
            }

            return "success";
        }

        protected String parseQuery(ParseQuery <ParseObject> query)
        {
            try
            {
                schedule = (ArrayList<ParseObject>) query.find();
                SQLiteDatabase db = dbh.getWritableDatabase();
                addDBConnection();
                
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
                closeDatabase();
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        protected String downloadSchedule(Lesson... lessons)
        {
            SQLiteDatabase db = dbh.getReadableDatabase();
            addDBConnection();
            
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
                //Log.w("Shika", "date: " + lesson.date);
                c.moveToFirst();
                do
                {
                    db = dbh.getReadableDatabase();
                    addDBConnection();

                    Cursor x = db.rawQuery("select count(*) from Schedule where lesson = ? and courseId = ? and date = ?",
                        new String[]{c.getString(name), c.getString(courseId), lesson.date});
                    if(x.moveToFirst())
                        if(x.getInt(0) > 0)
                        {
                            closeDatabase();
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
                    closeDatabase();
                }
                while (c.moveToNext());
            }

            closeDatabase();

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
            Log.d("Shika", "ScheduleDownloader finished");

            if(string.equals("no courses"))
            {
                //TODO: replace this with snackbar
                showToast("There are no courses in your schedule list. Please add them in edit schedule section.");
            }

            closeDatabase();

            //Let's tell fragment that we have done something
            for(Interfaces.Download iFace : interfaces.values())
            {
                Fragment temp = (Fragment) iFace;
                if(temp.isVisible())
                    iFace.onDownloadEnd(string);
            }
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

    public void closeDatabase()
    {
        Log.d("Shika", (dbConnections) + " out");
        if(dbConnections > 0)
        {
            dbConnections--;
            return;
        }
        dbh.close();
    }

    public DBHelper getDBHelper()
    {
        return dbh;
    }

    public void addDBConnection()
    {
        dbConnections++;
        Log.d("Shika", (dbConnections) + " in");
    }

    public void showProgressView()
    {
        container.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progressDrawable.start();


    }

    @Override
    public void dismissProgressView()
    {
        progressDrawable.stop();
        progress.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }
}