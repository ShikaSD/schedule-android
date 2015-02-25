package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.LocalLoaderInterface;

public class EditLocalLoader extends LocalLoader
{
	public EditLocalLoader(String id, Context ctx, LocalLoaderInterface callback)
	{
		super(id, ctx, callback, LoaderCode.EDIT);
	}

	@Override
	protected void load()
	{
		cursor = dbh.rawQuery("select courseId, name, isEnrolled from Courses where isEnrolled = 1 order by name", null);
	}
}
