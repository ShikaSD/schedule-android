package ru.shika.app;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.squareup.timessquare.CalendarPickerView;
import ru.shika.Application;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.app.adapters.DrawerListAdapter;
import ru.shika.app.fragments.DialogInfoFragment;
import ru.shika.app.fragments.EditFragment;
import ru.shika.app.interfaces.ActivityInterface;
import ru.shika.app.interfaces.ControllerInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements ActivityInterface {
    private ControllerInterface controller;

    Toolbar toolbar;

    //ProgressBar in the center
    private CircleImageView progress;
    private int mCircleWidth;
    private int mCircleHeight;
    private MaterialProgressDrawable progressDrawable;

    //Function button
    private CircleImageView functionButton;
    public static boolean isFunctionButtonVisible;

    //Different views fo calendar
    private CalendarPickerView calendar;
    private CardView calendarContainer;

    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private ActionBarDrawerToggle toggle;

    //Container for fragment
    private RelativeLayout container;

    //Listeners fo function button
    private View.OnClickListener calendarButtonClick, addButtonClick;

    //Animations
    private Animation buttonOpen;
    private Animation buttonClose;

    private Animation calendarOpen;
    private Animation calendarClose;

    private Animation snackBarOpen;
    private Animation snackBarClose;

    //SnackBar
    private RelativeLayout snackBar;
    private TextView snackBarText;
    private TextView snackBarButton;

    //Drawer titles
    private String[] titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        controller = Application.getController();

        isFunctionButtonVisible = true;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create drawer
        drawerInit();

        //SnackBar Init
        snackBar = (RelativeLayout) findViewById(R.id.snackbar);
        snackBarText = (TextView) findViewById(R.id.snackbar_text);
        snackBarButton = (TextView) findViewById(R.id.snackbar_button);

        updateSnackBarSize();

        snackBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackBar.startAnimation(snackBarClose);
                showFunctionButton();
            }
        });
        snackBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((onDrawerItemClickListener) drawerList.getOnItemClickListener()).replaceFragment("Edit schedule");
            }
        });

        //Init listeners
        calendarButtonClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCalendarClick(view);
            }
        };
        addButtonClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.addClick();
            }
        };

        //Don't forget finding views
        createProgressView();

        //Calendar button
        createFunctionButton();

        //CalendarView init
        calendarInit();

        container = (RelativeLayout) findViewById(R.id.main_container);

        //Init animations
        animationsInit();

        controller.setActivity(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateSnackBarSize();
    }

    private void animationsInit() {
        buttonOpen = AnimationUtils.loadAnimation(this, R.anim.function_button_open);
        buttonOpen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                functionButton.setVisibility(View.VISIBLE);
                isFunctionButtonVisible = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        buttonClose = AnimationUtils.loadAnimation(this, R.anim.function_button_close);
        buttonClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                functionButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        calendarOpen = AnimationUtils.loadAnimation(this, R.anim.calendar_open);
        calendarOpen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                calendarContainer.setVisibility(View.VISIBLE);
                calendar.clearFocus();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        calendarClose = AnimationUtils.loadAnimation(this, R.anim.calendar_close);
        calendarClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                calendarContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        snackBarOpen = AnimationUtils.loadAnimation(this, R.anim.snackbar_appear);
        snackBarOpen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                snackBar.setVisibility(View.VISIBLE);
                snackBarButton.setVisibility(View.VISIBLE);
                dismissFunctionButton();
                isFunctionButtonVisible = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        snackBarClose = AnimationUtils.loadAnimation(this, R.anim.snackbar_disappear);
        snackBarClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                snackBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void drawerInit() {
        //Drawer items
        ArrayList<Lesson.DrawerItem> drawerItems;
        TypedArray drawables;

        //Get drawer's items from resources
        titles = getResources().getStringArray(R.array.drawer_strings);
        drawables = getResources().obtainTypedArray(R.array.drawer_drawables);

        drawerItems = new ArrayList<Lesson.DrawerItem>();

        //Init drawer list
        for (int i = 0; i < titles.length; i++) {
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
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View view, float v) {
                super.onDrawerSlide(view, v);
            }

            @Override
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);

                if (isFunctionButtonVisible) functionButton.startAnimation(buttonClose);
                if (calendarContainer.getVisibility() == View.VISIBLE) calendarContainer.startAnimation(calendarClose);
            }

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                if (isFunctionButtonVisible) functionButton.startAnimation(buttonOpen);
            }

            @Override
            public void onDrawerStateChanged(int i) {
                super.onDrawerStateChanged(i);
            }
        };
        drawerLayout.setDrawerListener(toggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void createProgressView() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (56 * metrics.density);
        mCircleHeight = (int) (56 * metrics.density);

        progress = new CircleImageView(this, getResources().getColor(R.color.white), 56 / 2);
        progressDrawable = new MaterialProgressDrawable(this, progress);

        FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lParams.gravity = Gravity.CENTER;
        progress.setLayoutParams(lParams);

        progressDrawable.updateSizes(MaterialProgressDrawable.LARGE);
        progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
        progress.setImageDrawable(progressDrawable);

        ((ViewGroup) findViewById(R.id.main_container_parent)).addView(progress);
        progress.setVisibility(View.GONE);
    }

    private void createFunctionButton() {
        Resources resources = getResources();
        final float density = resources.getDisplayMetrics().density;

        final int circleDiameter = (int) resources.getDimension(R.dimen.function_button_diameter);
        final int padding = (int) resources.getDimension(R.dimen.function_button_padding);
        mCircleWidth = circleDiameter;
        mCircleHeight = circleDiameter;
        final float xPosition = resources.getDimension(R.dimen.function_button_vertical_margin);
        final float yPosition = resources.getDimension(R.dimen.function_button_horizontal_margin);

        functionButton = new CircleImageView(this, getResources().getColor(R.color.orange_accent), (int) (circleDiameter / 2 / density));

        RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lParams.setMargins(0, 0, (int) (xPosition), (int) (yPosition));

        functionButton.setLayoutParams(lParams);
        functionButton.setPadding(padding, padding, padding, padding);
        functionButton.setAdjustViewBounds(true);
        functionButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        functionButton.setFocusable(true);
        functionButton.setClickable(true);

        functionButton.setImageDrawable(resources.getDrawable(R.drawable.ic_calendar));
        ((ViewGroup) (findViewById(R.id.activity_container))).addView(functionButton);

        functionButton.setOnClickListener(calendarButtonClick);
        isFunctionButtonVisible = true;
    }

    private void showFragment(String name, String arg1) {
        if (snackBar.getVisibility() == View.VISIBLE) snackBar.startAnimation(snackBarClose);

        controller.showFragment(name, arg1);
    }

    @Override
    public void showError(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (msg.equals(getString(R.string.no_courses))) {
                    showSnackBar(msg, getString(R.string.add));
                    return;
                }

                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showSnackBar(String text, String btnText) {
        snackBarText.setText(text);
        snackBarButton.setText(btnText);

        snackBar.startAnimation(snackBarOpen);
    }

    private void calendarInit() {
        Calendar until = Calendar.getInstance();
        until.add(Calendar.MONTH, 5);
        until.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar from = Calendar.getInstance();
        from.set(2014, Calendar.SEPTEMBER, 1);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Calendar today = Calendar.getInstance();

        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) today.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        calendar.init(from.getTime(), until.getTime()).withSelectedDate(today.getTime());

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                controller.dateChanged(date.getTime());

                calendar.selectDate(date, true);
                calendarContainer.startAnimation(calendarClose);
                functionButton.startAnimation(buttonOpen);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        calendarContainer = (CardView) findViewById(R.id.calendar_container);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) calendarContainer.getLayoutParams().width = (int) getResources().getDimension(R.dimen.calendar_width);
        else calendarContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Controller.isActivityRunning = true;
        dismissProgress(); //Sometimes it is running somehow O_O

        showFragment("My schedule", "My schedule");

        toggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.info:
                DialogFragment fragment = new DialogInfoFragment();
                fragment.show(getSupportFragmentManager(), "Info");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (calendarContainer.getVisibility() == View.VISIBLE) {
            calendarContainer.startAnimation(calendarClose);
            functionButton.startAnimation(buttonOpen);
            return;
        }

        controller.backPressed();
    }

    //If in controller we have to find back pressed
    @Override
    public void backPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.activityDestroyed();
    }

    private class onDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            replaceFragment(titles[position]);

            getSupportActionBar().setTitle(titles[position]);
            drawerLayout.closeDrawer(drawerList);
        }

        private void replaceFragment(String tag) {
            functionButton.setVisibility(View.INVISIBLE);
            if (tag.endsWith("Chooser") || tag.contains("Edit")) {
                functionButton.setOnClickListener(addButtonClick);
                functionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_new));
                int p = (int) getResources().getDimension(R.dimen.function_button_add_padding);
                functionButton.setPadding(p, p, p, p);
            } else {
                functionButton.setOnClickListener(calendarButtonClick);
                functionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar));
                int p = (int) getResources().getDimension(R.dimen.function_button_padding);
                functionButton.setPadding(p, p, p, p);
            }
            functionButton.setVisibility(View.INVISIBLE);
            isFunctionButtonVisible = true;
            EditFragment.wasInEditMode = false;

            showFragment(tag, tag);
        }
    }

    public void onCalendarClick(View view) {
        if (calendarContainer.getVisibility() != View.VISIBLE) {
            calendarContainer.startAnimation(calendarOpen);
            functionButton.startAnimation(buttonClose);
        }
    }

    @Override
    public void showProgress() {
        container.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progressDrawable.start();
    }

    @Override
    public void dismissProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDrawable.stop();
                progress.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showFunctionButton() {
        if (functionButton.getVisibility() != View.VISIBLE) functionButton.startAnimation(buttonOpen);
    }

    @Override
    public void dismissFunctionButton() {
        if (functionButton.getVisibility() == View.VISIBLE) functionButton.startAnimation(buttonClose);
    }

    @Override
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public String getActionTitle() {
        return getSupportActionBar().getTitle().toString();
    }

    @Override
    public void notifyDateChanged() {
        calendar.selectDate(controller.getDate().getTime());
    }

    private void updateSnackBarSize() {
        float dp = getResources().getDisplayMetrics().widthPixels;
        dp *= .7f;
        snackBarText.setMaxWidth((int) dp);

        /*dp = getResources().getDisplayMetrics().widthPixels;
        dp *= .25f;
        dp -= 50 * getResources().getDisplayMetrics().density;
		Log.d("Shika", dp + "");
        dp /= 2;
		snackBarButton.setVisibility(View.VISIBLE);
        snackBarButton.setPadding((int) (dp * getResources().getDisplayMetrics().density), snackBarButton.getPaddingTop(),
            (int) (dp * getResources().getDisplayMetrics().density), snackBarButton.getPaddingBottom());*/
    }
}