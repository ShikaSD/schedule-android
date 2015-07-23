package ru.shika.app.loaders;

import android.content.Context;
import android.util.Log;
import ru.shika.app.Controller;
import ru.shika.app.Lesson;
import ru.shika.app.interfaces.LocalLoaderInterface;
import ru.shika.app.interfaces.NetworkLoaderInterface;

import java.util.Calendar;
import java.util.Date;

public class LoaderFabric {
    private Context ctx;

    public LoaderFabric(Context ctx) {
        this.ctx = ctx;
    }

    public NetworkLoader createNetworkLoader(String id, NetworkLoaderInterface i) {
        /**argument can have different meanings, so it is arg
         *@arg1 = param/type/group
         *@arg2 = name/teacher
         *@arg3 = name in schedule objects*/

        Lesson temp = Controller.items.get(id.replace(LoaderCenter.NETWORK, ""));

        String arg1 = temp.group;
        String arg2 = temp.teacher;
        String arg3 = temp.name;
        Calendar date = Calendar.getInstance();

        Log.d("Shika", "Fabric: Making network loader with values " + id + ", " + arg1 + ", " + arg2 + ", " + arg3);

        if (temp.calendar != null) {
            date.setTimeInMillis(temp.calendar.getTime());
            /*So it is Schedule*/
            boolean isPersonalSchedule = (arg1 == null && arg2 == null && arg3 == null);

            date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            Lesson[] lessons = new Lesson[2];
            String dateFormat;

            lessons[0] = new Lesson(null, null, null, arg3, arg2, Lesson.convertDateToString(date), arg1, 0);
            date.add(Calendar.DATE, 6);
            lessons[1] = new Lesson(null, null, null, arg3, arg2, Lesson.convertDateToString(date), arg1, 0);

            return new ScheduleNetworkLoader(id, ctx, i, isPersonalSchedule, lessons);
        }

        if (arg1.startsWith("Edit")) //It is Edit loader
            return new EmptyNetworkLoader(id, ctx, i); //It doesn't need any network iloaders

        if (arg2 == null) {
            //It is List
            return new ListNetworkLoader(id, ctx, i, arg1);
        }

        if (arg1.contains("Edit")) //It is Edit loader, we don't need anything here
            return null;

        //Only one variant, it is Chooser
        return new ChooserNetworkLoader(id, ctx, i, arg1, arg2);
    }

    public LocalLoader createLocalLoader(String id, LocalLoaderInterface i, int downloaded) {
        /**argument can have different meanings, so it is arg
         *@arg1 = param/type/group
         *@arg2 = name/teacher
         *@arg3 = name in schedule objects*/
        Log.d("Shika", "Fabric: Making local loader with id " + id);

        Lesson temp = Controller.items.get(id);
        String arg1 = temp.group;
        String arg2 = temp.teacher;
        String arg3 = temp.name;
        Date date = temp.calendar;

        if (downloaded == LoaderCenter.LOADER_CHECK) return new ChooserCheckLoader(id, ctx, i);

        if (date != null) {
            //This is Schedule
            boolean isPersonalSchedule = (arg1 == null && arg2 == null && arg3 == null);
            return new ScheduleLocalLoader(id, ctx, i, arg1, arg2, arg3, date, isPersonalSchedule);
        }

        if (arg1.startsWith("Edit")) //It is Edit loader
            return new EditLocalLoader(id, ctx, i);

        if (arg2 == null) {
            //This is List
            return new ListLocalLoader(id, ctx, i, arg1, downloaded);
        }

        //This is Chooser
        return new ChooserLocalLoader(id, ctx, i, arg1, arg2);
    }

    public NetworkLoader createEmptyLoader(String id, NetworkLoaderInterface i) {
        return new EmptyNetworkLoader(id, ctx, i);
    }
}
