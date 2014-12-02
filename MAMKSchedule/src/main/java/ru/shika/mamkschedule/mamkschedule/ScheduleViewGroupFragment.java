package ru.shika.mamkschedule.mamkschedule;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import ru.shika.android.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ScheduleViewGroupFragment extends Fragment implements Interfaces.Download, LoaderManager.LoaderCallbacks<Cursor>
{
	Interfaces.needDownload downloader;

	HashMap <String, Interfaces.updateFragment> updateFragments = new HashMap<String, Interfaces.updateFragment>();
	ArrayList< ArrayList<Lesson>>lessonsArr = new ArrayList<ArrayList<Lesson>>();

	WeekPagerAdapter pagerAdapter;
	ViewPager viewPager;
	String[] days;
	SlidingTabLayout tabLayout;

	static String group = null;
	static String teacher = null;
	static String course = null;

	boolean isOwnSchedule = false;
	Calendar date = Calendar.getInstance();

	DBHelper db;

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

		Log.w("Shika", "ViewGroup onCreate");

		db = new DBHelper(getActivity());

		group = getArguments().getString("group");
		teacher = getArguments().getString("teacher");
		course = getArguments().getString("course");
		isOwnSchedule = (group == null && teacher == null && course == null);
		date.setTimeInMillis(getArguments().getLong("date"));

		if(getActivity().getSupportLoaderManager().getLoader(0) == null)
			getActivity().getSupportLoaderManager().initLoader(0, getArguments(), this);
		else
			getActivity().getSupportLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_schedule_viewgroup, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		days = getResources().getStringArray(R.array.days);

		viewPager = (ViewPager) view.findViewById(R.id.pager);
		tabLayout = (SlidingTabLayout) view.findViewById(R.id.tabs);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		//Init viewpager with adapter
		pagerAdapter = new WeekPagerAdapter(getChildFragmentManager());
		viewPager.setAdapter(pagerAdapter);

		//Init tabs
		tabLayout.setViewPager(viewPager);
		tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.light_blue));

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		db.close();
	}

	public class WeekPagerAdapter extends FragmentPagerAdapter
	{
		public WeekPagerAdapter(FragmentManager fm)
		{
			super(fm);

			//getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
		}

		@Override
		public Fragment getItem(int position)
		{
			//Let's get new item and connect/update it
			Fragment fragment = ScheduleFragment.newInstance(position);
			updateFragments.put("" + (position), (Interfaces.updateFragment) fragment);
			if(!lessonsArr.isEmpty())
			{
				((Interfaces.updateFragment)fragment).update(lessonsArr);
			}
			return fragment;
		}

		@Override
		public int getCount() {

			return days.length;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return days[position];
		}

		@Override
		public Object instantiateItem (ViewGroup container, int position)
		{
			//I don't actually know why we have to update it here... but it works

			if(!lessonsArr.isEmpty())
			{
				if(updateFragments.containsKey("" + position))
					updateFragments.get(position+"").update(lessonsArr);
			}

			return super.instantiateItem(container, position);
		}
	}


	//Interface method called when ScheduleDownloader ended its job
	@Override
	public void onDownloadEnd(String result)
	{
		if(result.equals("success"))
		{
			Log.w("Shika", "Found something");
			getActivity().getSupportLoaderManager().getLoader(0).forceLoad();
		}
		else
		if(result.equals("nothing"))
		{
			Log.w("Shika", "Found nothing");
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
		//This variable changes somehow O_O, so reinit it
		isOwnSchedule = (group == null && teacher == null && course == null);

		//Log.w("Shika", "Fragment: "+group+"+"+ teacher +"+"+ course+"+"+isOwnSchedule);
		this.date.setTime(date);
		getActivity().getSupportLoaderManager().restartLoader(0, getArguments(), this);

	}

	public void showError()
	{
		Toast.makeText(getActivity(), "Error occured. Please check your internet connection", Toast.LENGTH_SHORT).show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		Log.w("Shika", "onCreate Loader");
		return new ScheduleLoader(getActivity(), db, group,
			teacher, course, date, isOwnSchedule);
	}

	//Loader methods, here we update arrays
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		//Log.w("Shika", "Finish: "+group+"+"+ teacher +"+"+ course+"+"+isOwnSchedule);

		lessonsArr.clear();
		HashMap <String, Lesson> lessons = new HashMap<String, Lesson>();
		if(cursor.moveToFirst())
		{
			int start = cursor.getColumnIndex("start");
			int end = cursor.getColumnIndex("end");
			int room = cursor.getColumnIndex("room");
			int lesson = cursor.getColumnIndex("lesson");
			int teacher = cursor.getColumnIndex("teacher");
			int date = cursor.getColumnIndex("date");
			int lessonId = cursor.getColumnIndex("lessonId");

			do
			{
				if(lessons.containsKey(cursor.getString(lessonId)))
				{
					lessons.get(cursor.getString(lessonId)).teacher += ", " + cursor.getString(teacher);
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
					cursor.getString(room), cursor.getString(lesson), cursor.getString(teacher), null, null, day));
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
				lessonsArr.add(result);
			}

			for(Interfaces.updateFragment iFace : updateFragments.values())
			{
				iFace.update(lessonsArr);
			}
		}
		else
		{
			Log.w("Shika", "Found nothing in database, need download");

			//To avoid date changes
			Calendar dateArg = Calendar.getInstance();
			dateArg.setTime(date.getTime());

			if (group != null)
			{
				downloader.needDownload(group, null, null, dateArg);
			} else if (teacher != null)
			{
				downloader.needDownload(null, teacher, null, dateArg);
			}
			else if(course != null)
			{
				downloader.needDownload(null, null, course, dateArg);
			}
		}

		db.close();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}

	public static class ScheduleLoader extends CursorLoader
	{
		DBHelper db;
		String group;
		String teacher;
		String course;
		Calendar date;
		boolean isOwnSchedule = false;

		public ScheduleLoader(Context context, DBHelper db, String group, String teacher, String course,
							  Calendar date, boolean isOwnSchedule)
		{
			super(context);
			this.db = db;
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
			SQLiteDatabase sqdb = db.getReadableDatabase();
			
			date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			String dates[] = new String[5];
			for(int i = 0; i < dates.length; i++)
			{
				dates[i] = date.get(Calendar.YEAR) - 2000 + "" +
					(date.get(Calendar.MONTH) + 1 > 9 ? date.get(Calendar.MONTH) + 1 : "0" + (1 + date.get(Calendar.MONTH)))+
					(date.get(Calendar.DAY_OF_MONTH) > 9 ? date.get(Calendar.DAY_OF_MONTH) : "0" + date.get(Calendar.DAY_OF_MONTH));
				Log.w("Shika", "Dates are: " + dates[i] + " Group is: " + group);
				date.add(Calendar.DATE, 1);
			}

			if(isOwnSchedule)
				cursor = sqdb.query("schedule", null, "isEnrolled = 1 and (date like ? or date like ? or date like " +
						"? or date like ? or date like ?)", dates, null, null ,"start");
			else
			if(group != null)
			{
				cursor = sqdb.query("schedule", null, "groups like '" + group + "%'and (date like ? or date like ? or" +
					" date like ? or date like ? or date like ?)", dates, null, null, "start");
			}
			else
			if(teacher != null)
			{
				teacher += "%";
				cursor = sqdb.query("schedule", null, "teacher like '" + teacher + "%'and (date like ? or date like ?" +
					" or date like ? or date like ? or date like ?)", dates, null, null, "start");
			}
			else
			if(course != null)
			{
				course += "%";
				cursor = sqdb.query("schedule", null, "lesson like '" + course + "%'and (date like ? or date like ? " +
					"or date like ? or date like ? or date like ?)", dates, null, null, "start");
			}

			Log.w("Shika", cursor.getCount() + " found in database");

			return cursor;
		}
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
