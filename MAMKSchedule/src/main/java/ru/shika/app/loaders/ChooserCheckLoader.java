package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.LocalLoaderInterface;

public class ChooserCheckLoader extends LocalLoader
{
	public ChooserCheckLoader(String id, Context ctx, LocalLoaderInterface callback)
	{
		super(id, ctx, callback, LoaderCode.CHECK);
	}

	@Override
	protected void load()
	{
		cursor = dbh.rawQuery("select courseId, name, isEnrolled from Courses where isEnrolled = 1", null);
	}
}
