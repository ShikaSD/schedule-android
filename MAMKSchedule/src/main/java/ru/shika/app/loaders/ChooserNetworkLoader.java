package ru.shika.app.loaders;

import android.content.Context;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.app.R;
import ru.shika.app.interfaces.NetworkLoaderInterface;

import java.util.ArrayList;

public class ChooserNetworkLoader extends NetworkLoader
{
	private String type, name;

	public ChooserNetworkLoader(String id, Context context, NetworkLoaderInterface callback, String type, String name)
	{
		super(id, context, callback, LoaderCode.CHOOSER);

		this.type = type.replace("Chooser", "");
		this.name = name;
	}

	@Override
	protected void prepareDownload()
	{
		if(!isNetworkConnection())
			error(getString(R.string.error_network_not_connected));

		query = ParseQuery.getQuery("Course");
	}

	@Override
	protected void download()
	{
		try
		{
			//To download all the data
			int amount = query.count();
			query.setLimit(amount);

			query.addAscendingOrder("name");

			if(type.equals("Groups"))
				query.whereEqualTo("group", name);

			if(type.equals("Teachers"))
				query.whereEqualTo("teacher", name);

			result = (ArrayList<ParseObject>) query.find();

			insertValues("Courses");
		}
		catch (Exception e)
		{
			error(getString(R.string.error_network_not_connected));
			e.printStackTrace();
		}
	}

	@Override
	protected void postDownload()
	{
		callback.downloadEnd(id, LoaderCenter.SUCCESS);
	}
}
