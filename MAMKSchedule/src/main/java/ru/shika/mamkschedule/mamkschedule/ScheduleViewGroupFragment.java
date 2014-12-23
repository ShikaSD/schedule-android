package ru.shika.mamkschedule.mamkschedule;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.shika.android.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ScheduleViewGroupFragment extends Fragment implements Interfaces.Download, LoaderManager.LoaderCallbacks<Cursor>
{
	Interfaces.needDownload downloader;
	public ArrayList< ArrayList<Lesson>>lessonsArr;

	ScheduleViewGroupAdapter pagerAdapter;
	ViewPager viewPager;
	String[] days;
	SlidingTabLayout tabLayout;

	String group = null;
	String teacher = null;
	String course = null;

	boolean isOwnSchedule = false;
	Calendar globalDate = Calendar.getInstance();
	int dayOfWeek = getWeek(globalDate);

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

		lessonsArr = new ArrayList<ArrayList<Lesson>>();
		for (int i = 0; i < 5; i++)
			lessonsArr.add(new ArrayList<Lesson>());

		db = new DBHelper(getActivity());

		group = getArguments().getString("group");
		teacher = getArguments().getString("teacher");
		course = getArguments().getString("course");
		isOwnSchedule = (group == null && teacher == null && course == null);
		globalDate.setTimeInMillis(getArguments().getLong("date"));
		globalDate.setFirstDayOfWeek(Calendar.MONDAY);
		dayOfWeek = getWeek(globalDate);

		if(getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_SCHEDULE) == null)
			getActivity().getSupportLoaderManager().initLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);
		else
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);
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
		pagerAdapter = new ScheduleViewGroupAdapter(lessonsArr, days);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(dayOfWeek);

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


	//Interface method called when ScheduleDownloader ended its job
	@Override
	public void onDownloadEnd(String result)
	{
		if(result.equals("success"))
		{
			Log.w("Shika", "Found something");
			getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_SCHEDULE).forceLoad();
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
		Log.w("Shika", "dateChanged in Fragment"+ this);
		//This variable changes somehow O_O, so reinit it
		isOwnSchedule = (group == null && teacher == null && course == null);

		//Log.w("Shika", "Fragment: "+group+"+"+ teacher +"+"+ course+"+"+isOwnSchedule);
		globalDate.setTime(date);
		dayOfWeek = getWeek(globalDate);
		globalDate.setFirstDayOfWeek(Calendar.MONDAY);
		getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_SCHEDULE, getArguments(), this);

	}

	public void showError()
	{
		MainActivity.showToast("Error occured. Please check your internet connection");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		Log.w("Shika", "onCreate Loader");
		return new ScheduleLoader(getActivity(), db, group,
			teacher, course, globalDate, isOwnSchedule);
	}

	//Loader methods, here we update arrays
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		//Log.w("Shika", "Finish: "+group+"+"+ teacher +"+"+ course+"+"+isOwnSchedule);

		if(cursor.moveToFirst())
		{
			lessonsArr.clear();
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
		} else
		{
			Log.w("Shika", "Found nothing in database, need download");

			//To avoid date changes
			Calendar dateArg = Calendar.getInstance();
			dateArg.setTime(globalDate.getTime());

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
				//Log.w("Shika", "Dates are: " + dates[i] + " Group is: " + group);
				date.add(Calendar.DATE, 1);
			}

			if(isOwnSchedule)
			{
				String query = "select * from Courses inner join Schedule on(Courses.courseId = Schedule.courseId and" +
					" Courses.name = Schedule.lesson) where isEnrolled = 1 and (date like ? or date like ? or date " +
					"like ? or date like ? or date like ?)";
				cursor = sqdb.rawQuery(query, dates);
			}
			else
			if(group != null)
			{
				cursor = sqdb.query("Schedule", null, "groups like '" + group + "%'and (date like ? or date like ? or" +
					" date like ? or date like ? or date like ?)", dates, null, null, "start");
			}
			else
			if(teacher != null)
			{
				cursor = sqdb.query("Schedule", null, "teacher like '" + teacher + "%'and (date like ? or date like ?" +
					" or date like ? or date like ? or date like ?)", dates, null, null, "start");
			}
			else
			if(course != null)
			{
				cursor = sqdb.query("Schedule", null, "(courseId like '" + course + "%' or lesson like '" + course +
					"%')and (date like ? or date like ? or date like ? or date like ? or date like ?)", dates, null, null, "start");
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

	private int getWeek(Calendar calendar)
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
				return 4;
			case Calendar.SUNDAY:
				return 4;

			default:
				return 0;
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
