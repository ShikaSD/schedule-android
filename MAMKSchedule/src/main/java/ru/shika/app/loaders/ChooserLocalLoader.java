package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.Lesson;
import ru.shika.app.interfaces.LocalLoaderInterface;

import java.util.Calendar;

public class ChooserLocalLoader extends LocalLoader
{
	private String name, type;

	public ChooserLocalLoader(String id, Context ctx,LocalLoaderInterface callback, String type, String name)
	{
		super(id, ctx, callback, LoaderCode.CHOOSER);

		this.name = name;
		this.type = type;
	}

	@Override
	protected void load()
	{
		String now = Lesson.convertDateToString(Calendar.getInstance());
		cursor = dbh.rawQuery("select courseId, name, isEnrolled from Courses where (groups like '%"+ name +"%' or teacher like '%"+ name +"%') " +
			"and  " + now + " >= startDate and " + now + " <= endDate order by name", null);
	}

}
