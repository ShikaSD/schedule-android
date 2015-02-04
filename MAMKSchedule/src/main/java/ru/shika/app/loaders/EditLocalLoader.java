package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.LocalLoaderInterface;

public class EditLocalLoader extends LocalLoader
{
	public EditLocalLoader(int id, Context ctx, LocalLoaderInterface callback)
	{
		super(id, ctx, callback, LoaderCode.EDIT);
	}

	@Override
	protected void load()
	{
		cursor = dbh.rawQuery("select * from Courses where isEnrolled = 1", null);
	}
}
