package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.LocalLoaderInterface;

public class ChooserLocalLoader extends LocalLoader
{
	private String name, type;

	public ChooserLocalLoader(int id, Context ctx,LocalLoaderInterface callback, String type, String name)
	{
		super(id, ctx, callback, LoaderCode.CHOOSER);

		this.name = name;
		this.type = type;
	}

	@Override
	protected void load()
	{
		cursor = dbh.rawQuery("select * from Courses where groups like '%"+ name +"%' or teacher like '%"+ name +"%' " + "order by name", null);
	}

}
