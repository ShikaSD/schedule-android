package ru.shika.app.loaders;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.app.DBHelper;
import ru.shika.app.interfaces.NetworkLoaderInterface;
import ru.shika.app.R;

import java.util.ArrayList;

/**Downloading loader
 * downloads data from the server and add it to the local database*/
abstract class NetworkLoader implements Runnable
{
	private Context ctx;
	protected DBHelper dbh;

	protected SharedPreferences preferences;
	protected NetworkLoaderInterface callback;

	protected ArrayList <ParseObject> result;
	protected ParseQuery <ParseObject> query;

	private boolean isErrorHappened;

	protected int id;
	public LoaderCode code;

	public NetworkLoader(int id, Context context, NetworkLoaderInterface callback, LoaderCode code)
	{
		ctx = context;
		this.callback = callback;
		this.id = id;
		this.code = code;

		dbh = DBHelper.getInstance(ctx);
		preferences = ctx.getSharedPreferences(ctx.getString(R.string.app_name), Context.MODE_PRIVATE);
		isErrorHappened = false;
	}

	protected boolean isNetworkConnection()
	{
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	@Override
	public void run()
	{
		//On every part of the system check the errors
		if(!isErrorHappened)
			prepareDownload();

		if(!isErrorHappened)
			download();

		if(!isErrorHappened)
			postDownload();
	}

	protected abstract void prepareDownload();

	protected abstract void download();

	protected abstract void postDownload();

	/**Inserts objects from Parse Objects to database
	* Returns last inserted item*/
	protected String insertValues(String type)
	{
		//We need to return last downloaded item here
		String lastDownloaded = "";
		ContentValues cv = new ContentValues();

		int downloaded = 0;

		for (ParseObject i : result)
		{
			lastDownloaded = i.getString("name");

			if (type.equals("Courses"))
			{
				//Check for equality (in another tables we don't have to do that part)
				Cursor x = dbh.rawQuery("select count(*) from Courses where courseId = ? and name = ? and groups = ? and teacher = ?",
					new String[]{i.getString("courseId"), i.getString("name"), i.getString("group"), i.getString("teacher")});
				if (x.moveToFirst())
					if (x.getInt(0) > 0)
					{
						x.close(); //It is equal to what we have, so we skip that
						continue;
					}
				x.close();

				cv.put("courseId", i.getString("courseId"));
				cv.put("groups", i.getString("group"));
				cv.put("teacher", i.getString("teacher"));
			}

			cv.put("name", i.getString("name"));

			dbh.insert(type, null, cv);

			downloaded++;
		}

		//Send signal that we have downloaded something
		callback.updateIsRunning(downloaded, id);

		return lastDownloaded;
	}

	protected void error(String msg)
	{
		isErrorHappened = true;

		callback.showError(msg);

		callback.downloadEnd(LoaderCenter.ERROR, id);
	}

	protected String getString(int id)
	{
		return ctx.getString(id);
	}

	public LoaderCode getCode()
	{
		return code;
	}
}
