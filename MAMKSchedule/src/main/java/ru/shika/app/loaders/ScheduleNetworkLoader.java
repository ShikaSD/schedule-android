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

		if (!c.moveToNext())
		{
			callback.showError(getString(R.string.no_courses)); //No chosen courses found
			return;
		}
		//
		int courseId = c.getColumnIndex("courseId");
		int name = c.getColumnIndex("name");
		int teacher = c.getColumnIndex("teacher");
		int group = c.getColumnIndex("groups");

		ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
		queries.clear();
		c.moveToFirst();
		do
		{
			Log.d("Shika", "Name: " + c.getString(name) + ", courseId: " + c.getString(courseId) + ", date: " + items[0].date + ", " + items[1].date
				+ ", group: " + c.getString(group));

			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Lessons");

			query.whereEqualTo("name", c.getString(name));
			query.whereEqualTo("courseId", c.getString(courseId));
			query.whereGreaterThanOrEqualTo("date", items[0].date);
			query.whereLessThanOrEqualTo("date", items[1].date);
			query.whereEqualTo("teacher", c.getString(teacher));
			query.whereEqualTo("group", c.getString(group));

			queries.add(query);

			if (queries.size() >= 10)
			{
				query = ParseQuery.or(queries);

				try
				{
					result = (ArrayList<ParseObject>) query.find();
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

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Lessons");

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
			query.whereGreaterThanOrEqualTo("date", items[0].date);
			query.whereLessThanOrEqualTo("date", items[1].date);
			queries.add(query);

			query = ParseQuery.getQuery("Lessons");
			query.whereEqualTo("name", items[0].name);
		}

		query.whereGreaterThanOrEqualTo("date", items[0].date);
		query.whereLessThanOrEqualTo("date", items[1].date);

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
			cv.put("date", i.getString("date"));
			cv.put("lesson", i.getString("name"));
			cv.put("teacher", i.getString("teacher"));
			cv.put("room", i.getString("room"));
			cv.put("start", i.getString("start"));
			cv.put("end", i.getString("end"));
			cv.put("lessonId", i.getString("lessonId"));
			cv.put("courseId", i.getString("courseId"));

			counter++;

			//If the same lesson is in the database
			Cursor x = dbh.rawQuery("select * from Schedule where lessonId = ?", new String[]{i.getString
				("lessonId")});
			//check for equality
			if(x.moveToFirst())
			{
				dbh.update("Schedule", cv, "lessonId = ?", new String[]{i.getString("lessonId")});
				x.close();
				continue;
			}

			x.close();
			dbh.insert("schedule", null, cv);
		}

		callback.updateIsRunning(id, counter);

		return null;
	}
}
