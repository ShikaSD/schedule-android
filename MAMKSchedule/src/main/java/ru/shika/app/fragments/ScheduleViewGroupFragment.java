package ru.shika.app.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import ru.shika.Application;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.android.SlidingTabLayout;
import ru.shika.app.Lesson;
import ru.shika.app.adapters.ScheduleViewGroupAdapter;
import ru.shika.app.interfaces.ControllerInterface;
import ru.shika.app.interfaces.DateInterface;
import ru.shika.app.interfaces.LoaderCenterInterface;
import ru.shika.app.interfaces.ViewInterface;
import ru.shika.app.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ScheduleViewGroupFragment extends Fragment implements ViewInterface, DateInterface
{
	ControllerInterface controller;
	LoaderCenterInterface loader;

	private ArrayList<ArrayList<Lesson>> lessonsArr;

	private ScheduleViewGroupAdapter pagerAdapter;
	private ViewPager viewPager;
	private String[] days;
	private SlidingTabLayout tabLayout;

	private String group;
	private String teacher;
	private String course;
	private Calendar date;

	private int dayOfWeek;

	private CircleImageView progress;
	private MaterialProgressDrawable progressDrawable;

	private Animation appear, disappear;

	private int id;

	public static Fragment newInstance(String group, String teacher, String course, Date date)
	{
		Fragment fragment = new ScheduleViewGroupFragment();
		Bundle args = new Bundle();
		args.putString("group", group);
		args.putString("teacher", teacher);
		args.putString("course", course);
		args.putLong("date", date.getTime());

		fragment.setArguments(args);
		return fragment;
	}

	//Interface init
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		controller = Application.getController();
		controller.setDateInterface(this);
		loader = Application.getController().getLoader();

		group = teacher = course = null;

		lessonsArr = new ArrayList<ArrayList<Lesson>>();
		for (int i = 0; i < 7; i++)
			lessonsArr.add(new ArrayList<Lesson>());

		group = getArguments().getString("group");
		teacher = getArguments().getString("teacher");
		course = getArguments().getString("course");

		date = Calendar.getInstance();
		date.setTime(controller.getDate().getTime());
		date.setFirstDayOfWeek(Calendar.MONDAY);
		Log.d("Shika", "Date in fragment: " + date.getTime());
		dayOfWeek = getWeekDay(date);

		animationInit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_schedule_viewgroup, container, false);
		createProgressView((ViewGroup) rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		days = getResources().getStringArray(R.array.days);

		viewPager = (ViewPager) view.findViewById(R.id.pager);
		tabLayout = (SlidingTabLayout) view.findViewById(R.id.tabs);

		id = controller.register(this);
	}

	private void createProgressView(ViewGroup rootView)
	{
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		int mCircleWidth = (int) (40 * metrics.density);
		int mCircleHeight = (int) (40 * metrics.density);

		progress = new CircleImageView(getActivity(), getResources().getColor(R.color.white), 40 / 2);
		progressDrawable = new MaterialProgressDrawable(getActivity(), progress);

		RelativeLayout.LayoutParams lParams =
			new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
		lParams.bottomMargin = (int) getResources().getDimension(R.dimen.function_button_vertical_margin);
		lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		progress.setLayoutParams(lParams);

		progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
		progress.setImageDrawable(progressDrawable);

		rootView.addView(progress);
		progress.setVisibility(View.GONE);
	}

	private void animationInit()
	{
		appear = AnimationUtils.loadAnimation(getActivity(), R.anim.progress_drawable_appear);
		appear.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				progress.setVisibility(View.VISIBLE);
				progressDrawable.start();
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

		disappear = AnimationUtils.loadAnimation(getActivity(), R.anim.progress_drawable_disappear);
		disappear.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				progressDrawable.stop();
				progress.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		//Init viewpager with adapter
		pagerAdapter = new ScheduleViewGroupAdapter(lessonsArr, days);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(dayOfWeek);

		//Init tabs
		tabLayout.setViewPager(viewPager);
		tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.orange_accent));
		tabLayout.setBackgroundColor(getResources().getColor(R.color.light_blue));
		tabLayout.setDividerColors(getResources().getColor(R.color.light_blue));

		tabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				setDayOfWeek(position);
			}
		});

		load();

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	private void load()
	{
		controller.load(this, group, teacher, course);
	}

	private void update(Object o)
	{
		lessonsArr.clear();

		lessonsArr.addAll((ArrayList<ArrayList<Lesson>>) o);
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				pagerAdapter.notifyDataSetChanged();
			}
		});
	}


	@Override
	public void notifyDateChanged()
	{
		Log.d("Shika", date.getTime() + " : " + controller.getDate().getTime());
		date = (Calendar) controller.getDate().clone();
		date.setFirstDayOfWeek(Calendar.MONDAY);
		dayOfWeek = getWeekDay(date);

		viewPager.setCurrentItem(dayOfWeek);
	}

	@Override
	public void downloadEnd()
	{
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				dismissProgress();
			}
		});
	}

	@Override
	public void updateIsRunning()
	{
		if(getActivity() != null)
			getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					showProgress();
				}
			});

		update(loader.getData(id));
		loader.ready(id);
	}

	@Override
	public boolean visible()
	{
		return isVisible();
	}

	@Override
	public void showProgress()
	{
		if (progress != null && progress.getVisibility() != View.VISIBLE)
			progress.startAnimation(appear);
	}

	@Override
	public void dismissProgress()
	{
		if (progress != null && progress.getVisibility() == View.VISIBLE)
			progress.startAnimation(disappear);
	}


	public void setDayOfWeek(int day)
	{
		date.set(Calendar.DAY_OF_WEEK, getWeekDay(day));
		dayOfWeek = day;

		controller.dateChanged(date.getTime());
		load();
	}

	private int getWeekDay(Calendar calendar)
	{
		switch (calendar.get(Calendar.DAY_OF_WEEK))
		{
			case Calendar.MONDAY:
				return 0;
			case Calendar.TUESDAY:
				return 1;
			case Calendar.WEDNESDAY:
				return 2;
			case Calendar.THURSDAY:
				return 3;
			case Calendar.FRIDAY:
				return 4;
			case Calendar.SATURDAY:
				return 5;
			case Calendar.SUNDAY:
				return 6;

			default:
				return 2; //Random number
		}
	}

	private int getWeekDay(int i)
	{
		switch(i)
		{
			case 0:
				return Calendar.MONDAY;
			case 1:
				return Calendar.TUESDAY;
			case 2:
				return Calendar.WEDNESDAY;
			case 3:
				return Calendar.THURSDAY;
			case 4:
				return Calendar.FRIDAY;
			case 5:
				return Calendar.SATURDAY;
			case 6:
				return Calendar.SUNDAY;

			default:
				return Calendar.WEDNESDAY;//Random
		}
	}

	public static void CursorLog(Cursor cursor)
	{
		if (cursor.moveToFirst())
		{
			do
			{
				for (int i = 0; i < cursor.getColumnCount(); i++)
				{
					Log.w("Shika", cursor.getColumnName(i) + ": " + cursor.getString(i));
				}
			}
			while (cursor.moveToNext());
		} else
			Log.w("Shika", "Cursor is empty");
	}
}
