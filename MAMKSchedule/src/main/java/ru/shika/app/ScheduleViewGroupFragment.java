package ru.shika.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.android.SlidingTabLayout;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ScheduleViewGroupFragment extends Fragment implements Interfaces.Download, LoaderManager.LoaderCallbacks<Cursor>
{
	protected Interfaces.needDownload downloader;
	protected ArrayList< ArrayList<Lesson>>lessonsArr;

	protected ScheduleViewGroupAdapter pagerAdapter;
	protected ViewPager viewPager;
	protected String[] days;
	protected SlidingTabLayout tabLayout;

	protected String group = null;
	protected String teacher = null;
	protected String course = null;

	protected boolean isOwnSchedule = false;
	protected boolean didUpdate = false;
	protected Calendar globalDate;

	protected int dayOfWeek;

	protected DBHelper dbh;

	protected CircleImageView progress;
	protected MaterialProgressDrawable progressDrawable;
	private int mCircleWidth, mCircleHeight;

	private Animation appear, disappear;

	public static Fragment newInstance(boolean isOwnSchedule, String group, String teacher, String course, Date date)
	{
		Fragment fragment = new ScheduleViewGroupFragment();
		Bundle args = new Bundle();
		args.putBoolean("schedule", isOwnSchedule);
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
		try
		{
			downloader = (Interfaces.needDownload) activity;
		}
		catch (Exception e){e.printStackTrace();}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		lessonsArr = new ArrayList<ArrayList<Lesson>>();
		for (int i = 0; i < 7; i++)
			lessonsArr.add(new ArrayList<Lesson>());

		dbh = getDBH();

		group = getArguments().getString("group");
		teacher = getArguments().getString("teacher");
		course = getArguments().getString("course");
		isOwnSchedule = (group == null && teacher == null && course == null);

		didUpdate = false;

		globalDate = Calendar.getInstance();
		globalDate.setTimeInMillis(getArguments().getLong("date"));
		globalDate.setFirstDayOfWeek(Calendar.MONDAY);
		dayOfWeek = getWeekDay(globalDate);

		animationInit();

		if(getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_SCHEDULE) == null)
			getActivity().getSupportLoaderManager().initLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);
		else
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView =  inflater.inflate(R.layout.fragment_schedule_viewgroup, container, false);
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

		getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);
	}

	private void createProgressView(ViewGroup rootView) {
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		int mCircleWidth = (int) (40 * metrics.density);
		int mCircleHeight = (int) (40 * metrics.density);

		progress = new CircleImageView(getActivity(), getResources().getColor(R.color.background), 40/2);
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

	//For working with activity dbh
	private DBHelper getDBH()
	{
		return ((MainActivity) getActivity()).getDBHelper();
	}

	private void addDBConnection()
	{
		((MainActivity) getActivity()).addDBConnection();
	}

	private void closeDatabase()
	{
		((MainActivity) getActivity()).closeDatabase();
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

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		dbh.close();
	}


	//Interface method called when ScheduleDownloader ended its job
	@Override
	public void onDownloadEnd(String result)
	{
		pagerAdapter.setTextToEmpty(getString(R.string.no_lessons));
		progress.startAnimation(disappear);

		if(result.equals("success"))
		{
			Log.d("Shika", "Found something");
			getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_SCHEDULE).forceLoad();
		}
		else
		if(result.equals("nothing") || result.equals("no courses"))
		{
			Log.d("Shika", "Found nothing");
			/*int size = lessonsArr.size();
			for(int i = 0; i < size; i++)
				lessonsArr.get(i).clear();

			pagerAdapter.notifyDataSetChanged(lessonsArr);*/
		}
		else
		{
			Log.w("Shika", "Error");
			showError();
		}
	}

	@Override
	public void updateInProgress(int amount) {}

	@Override
	public void onDateChanged(Date date)
	{
		//Log.w("Shika", "dateChanged in Fragment"+ this);
		//This variable changes somehow O_O, so reinit it
		isOwnSchedule = (group == null && teacher == null && course == null);

		//Let's compare week number
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		d.setFirstDayOfWeek(Calendar.MONDAY);

		if(d.get(Calendar.WEEK_OF_YEAR) != globalDate.get(Calendar.WEEK_OF_YEAR))
		{
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);
			for(ArrayList<Lesson> i : lessonsArr)
				i.clear();
		}

		globalDate = d;
		dayOfWeek = getWeekDay(globalDate);

		Log.d("Shika", date.toString() + " in Fragment");

		viewPager.setCurrentItem(dayOfWeek);
		didUpdate = false;
	}

	public void showError()
	{
		((MainActivity) getActivity()).showToast("Network error occured. Please check your internet connection");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		if(dbh == null)
			dbh = getDBH();

		//Log.w("Shika", "onCreate Loader");
		return new ScheduleLoader(getActivity(), dbh, group,
			teacher, course, globalDate, isOwnSchedule);
	}

	//Loader methods, here we update arrays
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		//Log.w("Shika", "Finish: "+group+"+"+ teacher +"+"+ course+"+"+isOwnSchedule);
		lessonsArr.clear();

		if(cursor.moveToFirst())
		{
			HashMap <String, Lesson> lessons = new HashMap<String, Lesson>();

			int start = cursor.getColumnIndex("start");
			int end = cursor.getColumnIndex("end");
			int room = cursor.getColumnIndex("room");
			int lesson = cursor.getColumnIndex("lesson");
			int teacher = cursor.getColumnIndex("teacher");
			int date = cursor.getColumnIndex("date");
			int lessonId = cursor.getColumnIndex("lessonId");
			int group = cursor.getColumnIndex("groups");

			do
			{
				String id = cursor.getString(lessonId);
				if(lessons.containsKey(id))
				{
					if(lessons.get(id).teacher == null)
					{
						Log.w("Shika", "Teacher null on Course name = " + lessons.get(id).name);
						continue;
					}
					if(lessons.get(id).group == null)
					{
						Log.w("Shika", "Group null on Course name = " + lessons.get(id).name);
						continue;
					}

					if(!lessons.get(id).teacher.equals(cursor.getString(teacher)))
						lessons.get(id).teacher += ", "+cursor.getString(teacher);
					if(!lessons.get(id).group.equals(cursor.getString(group)))
						lessons.get(id).group += ", "+cursor.getString(group);
					continue;
				}

				String dateFormat = cursor.getString(date);

				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.YEAR, Integer.parseInt("20"+dateFormat.substring(0,2)));
				calendar = setMonth(calendar, dateFormat);
				calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateFormat.substring(4)));
				calendar.setFirstDayOfWeek(Calendar.MONDAY);

				int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
				//Log.w("Shika", "Day : " + day + " " + cursor.getString(lesson));

				lessons.put(cursor.getString(lessonId), new Lesson(cursor.getString(start), cursor.getString(end),
					cursor.getString(room), cursor.getString(lesson), cursor.getString(teacher), cursor.getString(group),
					null, day));
			}
			while (cursor.moveToNext());

			ArrayList <Lesson> result;

			for(int i = 1; i <= days.length; i++)
			{
				result = new ArrayList<Lesson>();
				for (Lesson temp : lessons.values())
				{
					if (temp.day == i)
						result.add(temp);
				}
				//Log.w("Shika", "result: " + result.size());
				lessonsArr.add(result);
			}

			pagerAdapter.notifyDataSetChanged(lessonsArr);
			viewPager.setCurrentItem(dayOfWeek);

			if(!didUpdate)
			{
				//To avoid date changes
				Calendar dateArg = Calendar.getInstance();
				dateArg.setTime(globalDate.getTime());

				progress.startAnimation(appear);

				pagerAdapter.setTextToEmpty(getString(R.string.updating));

				downloader.needDownload(this.group, this.teacher, course, dateArg);
				didUpdate = true;
			}
		} else
		{
			for(int i = 0; i < days.length; i++)
				lessonsArr.add(new ArrayList<Lesson>());

			pagerAdapter.notifyDataSetChanged(lessonsArr);

			progress.startAnimation(appear);

			Log.d("Shika", "Found nothing in database, need download");

			//To avoid date changes
			Calendar dateArg = Calendar.getInstance();
			dateArg.setTime(globalDate.getTime());

			didUpdate = true;

			pagerAdapter.setTextToEmpty(getString(R.string.updating));

			downloader.needDownload(group, teacher, course, dateArg);
		}

		closeDatabase();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}

	public static class ScheduleLoader extends CursorLoader
	{
		DBHelper dbh;
		String group;
		String teacher;
		String course;
		Calendar date;
		boolean isOwnSchedule = false;

		int argumentsArrayLength = 7;

		public ScheduleLoader(Context context, DBHelper dbh, String group, String teacher, String course,
							  Calendar date, boolean isOwnSchedule)
		{
			super(context);
			this.dbh = dbh;
			this.group = group;
			this.teacher = teacher;
			this.course = course;
			this.date = date;
			this.isOwnSchedule = isOwnSchedule;
			//Log.w("Shika", group+"+"+ teacher +"+"+ course+"+"+isOwnSchedule);
		}

		//Load from database
		@Override
		public Cursor loadInBackground()
		{
			Cursor cursor = null;
			SQLiteDatabase sqdb = dbh.getReadableDatabase();
			//As it is "static context"
			MainActivity.dbConnections++;

			String[] dates;
			
			date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

			if(course != null)
			{
				dates = new String[argumentsArrayLength + 1];
				dates[0] = course;
			}
			else
				dates = new String[argumentsArrayLength];
			for(int i = dates.length - argumentsArrayLength; i < dates.length; i++)
			{
				dates[i] = date.get(Calendar.YEAR) - 2000 + "" +
					(date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
					(date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
				//Log.w("Shika", "Dates are: " + dates[i] + " Group is: " + group);
				date.add(Calendar.DATE, 1);
			}

			if(isOwnSchedule)
			{
				String query = "select * from Courses inner join Schedule on(Courses.courseId = Schedule.courseId and" +
					" Courses.name = Schedule.lesson and Courses.teacher = Schedule.teacher and Courses.groups = " +
					"Schedule.groups) where isEnrolled = 1 and " +
					"(date like ? or date like ? or date like ? or date like ? or date like ? or date like ? or date " +
					"like ?)";
				cursor = sqdb.rawQuery(query, dates);
			}
			else
			if(group != null)
			{
				cursor = sqdb.query("Schedule", null, "groups like '" + group + "%' and (date like ? or date like ? " +
					"or date like ? or date like ? or date like ? or date like ? or date " +
					"like ?)", dates, null, null, "start");
			}
			else
			if(teacher != null)
			{
				cursor = sqdb.query("Schedule", null, "teacher like '" + teacher + "%' and (date like ? or date like " +
					"? or date like ? or date like ? or date like ?  or date like ? or date like ?)", dates, null, null, "start");
			}
			else
			if(course != null)
			{
				cursor = sqdb.query("Schedule", null, "(courseId like '" + course + "%' or lesson like ?) and (date " +
					"like ? or date like ? or date like ? or date like ? or date like ?  or date like ? or date like ?)", dates, null, null, "start");
			}

			Log.d("Shika", cursor.getCount() + " found in database");

			return cursor;
		}
	}

	public void setDayOfWeek(int day)
	{
		globalDate.set(Calendar.DAY_OF_WEEK, getWeekDayNumber(day));
		dayOfWeek = day;

		((MainActivity) getActivity()).setGlobalDate(globalDate.getTime());
	}

	//Fucking bullshit for calendar
	private Calendar setMonth(Calendar calendar, String dateString)
	{
		switch (Integer.parseInt(dateString.substring(2, 4)))
		{
			case 1:
				calendar.set(Calendar.MONTH, Calendar.JANUARY);
				break;
			case 2:
				calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
				break;
			case 3:
				calendar.set(Calendar.MONTH, Calendar.MARCH);
				break;
			case 4:
				calendar.set(Calendar.MONTH, Calendar.APRIL);
				break;
			case 5:
				calendar.set(Calendar.MONTH, Calendar.MAY);
				break;
			case 6:
				calendar.set(Calendar.MONTH, Calendar.JUNE);
				break;
			case 7:
				calendar.set(Calendar.MONTH, Calendar.JULY);
				break;
			case 8:
				calendar.set(Calendar.MONTH, Calendar.AUGUST);
				break;
			case 9:
				calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
				break;
			case 10:
				calendar.set(Calendar.MONTH, Calendar.OCTOBER);
				break;
			case 11:
				calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
				break;
			case 12:
				calendar.set(Calendar.MONTH, Calendar.DECEMBER);
				break;

			default:
				calendar.set(Calendar.MONTH, Calendar.JANUARY);
				break;
		}

		return calendar;
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
				return 7;

			default:
				return 0;
		}
	}

	private int getWeekDayNumber(int i)
	{
		switch (i)
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
				return Calendar.MONDAY;
		}
	}

	protected static void CursorLog(Cursor cursor)
	{
		if(cursor.moveToFirst())
		{
			do
			{
				for(int i = 0; i < cursor.getColumnCount(); i++)
				{
					Log.w("Shika", cursor.getColumnName(i) + ": " + cursor.getString(i));
				}
			}
			while (cursor.moveToNext());
		}
		else
			Log.w("Shika", "Cursor is empty");
	}
}
