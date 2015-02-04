package ru.shika.app.loaders;

import android.content.Context;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import ru.shika.app.interfaces.NetworkLoaderInterface;
import ru.shika.app.R;

import java.util.ArrayList;

public class ChooserNetworkLoader extends NetworkLoader
{
	private String type, name;

	public ChooserNetworkLoader(int id, Context context, NetworkLoaderInterface callback, String type, String name)
	{
		super(id, context, callback, LoaderCode.CHOOSER);

		this.type = type;
		this.name = name;
	}

	@Override
	protected void prepareDownload()
	{
		if(!isNetworkConnection())
			error(getString(R.string.error_network_not_connected));

		query = ParseQuery.getQuery("Courses");
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
		}
	}

	@Override
	protected void postDownload()
	{
		callback.downloadEnd(LoaderCenter.END_OF_DOWNLOAD, id);
	}
}
