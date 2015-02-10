package ru.shika.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import ru.shika.app.DBHelper;
import ru.shika.app.interfaces.LocalLoaderInterface;

import java.util.ArrayList;

abstract class LocalLoader implements Runnable
{
	protected DBHelper dbh;
	protected LocalLoaderInterface callback;

	protected LoaderCode code;
	protected String id;

	protected Cursor cursor;

	public LocalLoader(String id, Context ctx, LocalLoaderInterface callback, LoaderCode code)
	{
		Log.d("Shika", "Start loading from database with id: "+ id);
		dbh = DBHelper.getInstance(ctx);

		this.id = id;
		this.code = code;
		this.callback = callback;
		this.code = code;
	}

	protected abstract void load();

	protected void cursorParse(Cursor c)
	{
		ArrayList <ArrayList<String>> names = new ArrayList<ArrayList<String>>();
		SparseArray <String> keys = new SparseArray<String>();
		SparseBooleanArray checks = new SparseBooleanArray();

		int name = c.getColumnIndex("name");
		int id = c.getColumnIndex("courseId");
		int isEnrolled = c.getColumnIndex("isEnrolled");
		if(c.moveToFirst())
		{
			//Filling arrays
			do
			{
				boolean flag = false;
				String courseId;

				if(id == -1 || c.getString(id).equals(""))
					courseId = c.getString(name);
				else
					courseId = c.getString(id);

				if(courseId.equals(""))
					continue;

				int index = -1;

				for(int i = 0; i < keys.size(); i++)
					if(keys.valueAt(i).equals(courseId))
					{
						index = keys.keyAt(i);
						break;
					}

				if (index < 0)
				{
					names.add(new ArrayList<String>());
					keys.put(names.size() - 1, courseId);
					index = names.size() - 1;
				}

				for(String i : names.get(index))
				{
					if(i.replaceAll("[ ,.:;\\\\/-]+", "").toLowerCase().equals(c.getString(name).replaceAll("[ ,.:;\\\\/-]+", "").toLowerCase())
						|| (names.get(index).size() > 1 && c.getString(name).equals(courseId)))
					{
						flag = true;
						break;
					}
				}

				if(isEnrolled != -1 && c.getInt(isEnrolled) == 1)
				{
					checks.put(index, true);
				}

				if(flag)
					continue;

				names.get(index).add(c.getString(name));

			}
			while (c.moveToNext());
		}

		ArrayList <Object> objects = new ArrayList<Object>();
		objects.add(names);
		objects.add(keys);

		if(checks.size() > 0)
			objects.add(checks);

		callback.receiveData(this.id, code, objects);

		c.close();
	}

	@Override
	public void run()
	{
		load();

		cursorParse(cursor);
	}

	public LoaderCode getCode()
	{
		return code;
	}


}
