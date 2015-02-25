package ru.shika.app.loaders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.app.Lesson;
import ru.shika.app.R;
import ru.shika.app.interfaces.NetworkLoaderInterface;

import java.util.ArrayList;

//Specific kind of network loader with own parameters...
public class ScheduleNetworkLoader extends NetworkLoader
{
	private Lesson[] items;
	private boolean isPersonalSchedule;
	private int counter;

	public ScheduleNetworkLoader(String id, Context ctx, NetworkLoaderInterface callback, boolean isPersonalSchedule, Lesson... items)
	{
		super(id, ctx, callback, LoaderCode.SCHEDULE);

		this.items = items;
		this.isPersonalSchedule = isPersonalSchedule;

		counter = 0;
	}

	@Override
	protected void prepareDownload()
	{
		if (!isNetworkConnection())
			error(getString(R.string.error_network_not_connected));

		if (!isPersonalSchedule)
			query = prepareQuery();
		else
			//If we added some of the courses with the same code
			updateCourses();
	}

	@Override
	protected void download()
	{
		if (isPersonalSchedule)
		{
			/*Unfortunately, because of Parse limitations we have to do some different preparations
			and multiple requests*/
			downloadPersonalSchedule();
			return;
		}

		try
		{
			//We want to download all the items
			int amount = query.count();
			query.setLimit(amount);

			result = (ArrayList<ParseObject>) query.find();

			insertValues(null);
		} catch (Exception e)
		{
			error(getString(R.string.error_network_not_connected));
		}
	}

	@Override
	protected void postDownload()
	{
		callback.downloadEnd(id, LoaderCenter.SUCCESS);
	}

	private void downloadPersonalSchedule()
	{
		Cursor c = dbh.rawQuery("select * from Courses where isEnrolled = 1", null);

		int courseId = c.getColumnIndex("courseId");
		int name = c.getColumnIndex("name");
		int teacher = c.getColumnIndex("teacher");
		int group = c.getColumnIndex("groups");
		int start = c.getColumnIndex("startDate");
		int end = c.getColumnIndex("endDate");

		ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
		queries.clear();

		if(!c.moveToFirst())
		{
			//No courses found
			error(getString(R.string.no_courses));
			c.close();
			return;
		}

		do
		{
			Log.d("Shika", getClass().getName() + ": Name: " + c.getString(name) + ", courseId: " + c.getString(courseId) + ", date: " + items[0].date + ", " + items[1].date
				+ ", group: " + c.getString(group));

			if(c.getString(start).compareTo(items[1].date) > 0 || c.getString(end).compareTo(items[0].date) < 0)
				continue;

			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Lesson");

			query.whereEqualTo("name", c.getString(name));
			query.whereEqualTo("courseId", c.getString(courseId));
			query.whereGreaterThanOrEqualTo("dateString", items[0].date);
			query.whereLessThanOrEqualTo("dateString", items[1].date);
			query.whereEqualTo("teacher", c.getString(teacher));
			query.whereEqualTo("group", c.getString(group));

			queries.add(query);

			if (queries.size() >= 10)
			{
				this.query = ParseQuery.or(queries);

				try
				{
					result = (ArrayList<ParseObject>) this.query.find();
					insertValues(null);
				} catch (Exception e)
				{
					Log.e("Shika", e.getMessage());
					error(getString(R.string.error_network_not_connected));
				}
				queries.clear();
			}
		}
		while (c.moveToNext());

		query = ParseQuery.or(queries);

		try
		{
			result = (ArrayList<ParseObject>) query.find();
			insertValues(null);
		} catch (Exception e)
		{
			Log.d("Shika", e.getMessage());
			error(getString(R.string.error_network_not_connected));
		}

		c.close();

		callback.updateIsRunning(id, counter);
	}

	private ParseQuery<ParseObject> prepareQuery()
	{
		ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Lesson");

		if (items[0] == null)
		{
			Log.e("Shika", "item == null");
		}

		if (items[0].group != null)
			query.whereEqualTo("group", items[0].group);
		else if (items[0].teacher != null)
			query.whereEqualTo("teacher", items[0].teacher);
		else if (items[0].name != null)
		{
			//We have to match in this case by course id and name, as there can be both
			query.whereEqualTo("courseId", items[0].name);
			query.whereGreaterThanOrEqualTo("dateString", items[0].date);
			query.whereLessThanOrEqualTo("dateString", items[1].date);
			queries.add(query);

			query = ParseQuery.getQuery("Lesson");
			query.whereEqualTo("name", items[0].name);
		}

		query.whereGreaterThanOrEqualTo("dateString", items[0].date);
		query.whereLessThanOrEqualTo("dateString", items[1].date);

		queries.add(query);

		return ParseQuery.or(queries);
	}

	@Override
	protected String insertValues(String type)
	{
		ContentValues cv = new ContentValues();

		for (ParseObject i : result)
		{
			cv.put("groups", i.getString("group"));
			cv.put("date", i.getString("dateString"));
			cv.put("lesson", i.getString("name"));
			cv.put("teacher", i.getString("teacher"));
			cv.put("room", i.getString("room"));
			cv.put("start", i.getString("startTimeString"));
			cv.put("end", i.getString("endTimeString"));
			cv.put("courseId", i.getString("courseId"));

			counter++;

			//If the same lesson is in the database
			Cursor x = dbh.rawQuery("select * from Schedule where lesson = ? and teacher = ? and groups = ? and date = ? and start = ?",
				new String[]{ i.getString("name"), i.getString("teacher"), i.getString("group"), i.getString("dateString"), i.getString("startTimeString")});
			//check for equality
			if(x.moveToFirst())
			{
				dbh.update("Schedule", cv, "lesson = ? and teacher = ? and groups = ? and date = ? and start = ?",
					new String[]{ i.getString("name"), i.getString("teacher"), i.getString("group"), i.getString("dateString"), i.getString("startTimeString")});
				x.close();
				continue;
			}

			x.close();
			dbh.insert("schedule", null, cv);
		}

		callback.updateIsRunning(id, counter);

		return null;
	}

	private void updateCourses()
	{
		Cursor c = dbh.rawQuery("select * from Courses where isEnrolled = 1", null);

		if (!c.moveToFirst())
		{
			callback.showError(getString(R.string.no_courses)); //No chosen courses found
			c.close();
			return;
		}

		int courseId = c.getColumnIndex("courseId");
		ArrayList <ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
		do
		{
			query = ParseQuery.getQuery("Course");
			query.whereEqualTo("courseId", c.getString(courseId));
			queries.add(query);

			if(queries.size() >= 10)
			{
				query = ParseQuery.or(queries);
				insertCourses(c);
				queries.clear();
			}
		}
		while (c.moveToNext());

		query = ParseQuery.or(queries);
		insertCourses(c);

		c.close();
	}

	private void insertCourses(Cursor c)
	{
		int courseId = c.getColumnIndex("courseId");
		int name = c.getColumnIndex("name");

		try
		{
			result = (ArrayList <ParseObject>) query.find();

			for(ParseObject object : result)
			{
				c.moveToFirst();
				boolean f = false;

				do
				{
					//Compare objects with existing
					if(object.getString("courseId").equals(c.getString(courseId))
						|| (object.getString("courseId").equals("") && object.getString("name").equals(c.getString(name))))
					{
						f = true;
						break;
					}
				}
				while (c.moveToNext());

				if(f)
					continue;

				ContentValues cv = new ContentValues();
				cv.put("courseId", object.getString("courseId"));
				cv.put("name", object.getString("name"));
				cv.put("isEnrolled", 1);
				cv.put("startDate", object.getString("start"));
				cv.put("endDate", object.getString("end"));

				dbh.insert("Courses", null, cv);
			}
		}
		catch (Exception e)
		{
			Log.e("Shika", e.getMessage());
			error(getString(R.string.error_network_not_connected));
		}
	}
}
