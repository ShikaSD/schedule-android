package ru.shika.app.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.app.R;
import ru.shika.app.interfaces.NetworkLoaderInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ListNetworkLoader extends NetworkLoader
{
	private String type;

	//Some items can be already downloaded, some not
	private String lastDownloaded;

	//Last updating date
	private Date lastUpdate;

	public ListNetworkLoader(String id, Context ctx, NetworkLoaderInterface callbackInterface, String param)
	{
		super(id, ctx, callbackInterface, LoaderCode.LIST);
		type = param;

		lastDownloaded = "";

		lastUpdate = new Date(preferences.getLong(type + "lastUpdate", 0));
	}

	@Override
	protected void prepareDownload()
	{
		if(!isNetworkConnection())
		{
			error(getString(R.string.error_network_not_connected));
			return;
		}

		query = ParseQuery.getQuery(type);

		//Some objects must not be included
		query.whereDoesNotExist("last");
		query.addAscendingOrder("name");

		//Download parts
		query.setLimit(LoaderCenter.ONE_PART);

		if(lastUpdate.getTime() > 0)
			query.whereGreaterThan("createdAt", lastUpdate);
		else
		{
			//If we have something downloaded before
			lastDownloaded = preferences.getString(type + "last", "");
			query.whereGreaterThan("name", lastDownloaded);
		}
	}

	@Override
	protected void download()
	{
		try
		{
			int amount = 1; //It has to be more that zero, so init it with 1

			while (amount > 0)
			{
				//Update values
				query.whereGreaterThan("name", lastDownloaded);

				result = (ArrayList<ParseObject>) query.find();
				amount = result.size();

				lastDownloaded = insertValues(type);

				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(type + "last", lastDownloaded);
				editor.apply();
			}
		}
		catch (Exception e)
		{
			error(getString(R.string.error_network_not_connected));
		}
	}

	@Override
	protected void postDownload()
	{
		//In case we have saved nothing, let's load it next time
		Cursor c = dbh.rawQuery("select count(*) from " + type, null);
		c.moveToFirst();
		boolean isDatabaseEmpty = (c.getInt(0) <= 0);

		c.close();

		//Send signal about ending of download
		callback.downloadEnd(id, LoaderCenter.SUCCESS);

		long time;
		if (isDatabaseEmpty) time = 0;
		else time = Calendar.getInstance().getTimeInMillis();

		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(type+"lastUpdate", time);
		editor.apply();
	}
}
