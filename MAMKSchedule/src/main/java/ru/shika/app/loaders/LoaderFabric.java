package ru.shika.app.loaders;

import android.content.Context;
import android.util.Log;
import ru.shika.app.Controller;
import ru.shika.app.Lesson;
import ru.shika.app.interfaces.LocalLoaderInterface;
import ru.shika.app.interfaces.NetworkLoaderInterface;

import java.util.Calendar;
import java.util.Date;

public class LoaderFabric
{
	private Context ctx;

	public LoaderFabric(Context ctx)
	{
		this.ctx = ctx;
	}

	public NetworkLoader createNetworkLoader(NetworkLoaderInterface i, int id)
	{
		/**argument can have different meanings, so it is arg
		 *@arg1 = param/type/group
		 *@arg2 = name/teacher
		 *@arg3 = name in schedule objects*/

		Lesson temp = Controller.items.get(id - LoaderCenter.NETWORK);

		String arg1 = temp.group;
		String arg2 = temp.teacher;
		String arg3 = temp.name;
		Calendar date = Calendar.getInstance();

		if(temp.calendar != null)
		{
			date.setTime(temp.calendar);
			date.setFirstDayOfWeek(Calendar.MONDAY);
			/*So it is Schedule*/
			boolean isPersonalSchedule = (arg1 == null && arg2 == null && arg3 == null);

			date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

			Lesson[] lessons = new Lesson[7];
			String dateFormat;

			//Adding items
			for(int it = 0; it < lessons.length; it++)
			{
				dateFormat = Lesson.convertDateToString(date);

				lessons[it] = new Lesson(null, null, null, arg3, arg2, dateFormat, arg1, 0);
				date.add(Calendar.DATE, 1);
			}
			return new ScheduleNetworkLoader(id, ctx, i, isPersonalSchedule, lessons);
		}

		if(arg2 == null)
		{
			//It is List
			return new ListNetworkLoader(id, ctx, i, arg1);
		}

		if(arg1.contains("Edit")) //It is Edit loader, we don't need anything here
			return null;

		//Only one variant, it is Chooser
		return new ChooserNetworkLoader(id, ctx, i, arg1, arg2);
	}

	public LocalLoader createLocalLoader(LocalLoaderInterface i, int id, int downloaded)
	{
		/**argument can have different meanings, so it is arg
		 *@arg1 = param/type/group
		 *@arg2 = name/teacher
		 *@arg3 = name in schedule objects*/
		Lesson temp = Controller.items.get(id);
		String arg1 = temp.group;
		String arg2 = temp.teacher;
		String arg3 = temp.name;
		Date date = temp.calendar;

		if(date != null)
		{
			//This is Schedule
			boolean isPersonalSchedule = (arg1 == null && arg2 == null && arg3 == null);
			return new ScheduleLocalLoader(id, ctx, i, arg1, arg2, arg3, date, isPersonalSchedule);
		}

		if(arg1.contains("Edit")) //It is Edit loader
			return new EditLocalLoader(id, ctx, i);

		if(arg2 == null)
		{
			//This is List
			return new ListLocalLoader(id, ctx, i, arg1, downloaded);
		}

		//This is Chooser
		return new ChooserLocalLoader(id, ctx, i, arg1, arg2);
	}
}
