package ru.shika.app.loaders;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import ru.shika.app.Lesson;
import ru.shika.app.interfaces.LocalLoaderInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ScheduleLocalLoader extends LocalLoader {
    private String group, course, teacher;
    private boolean isPersonalSchedule;
    private Calendar date;

    public ScheduleLocalLoader(String id, Context ctx, LocalLoaderInterface callback, String group, String teacher, String course, Date date, boolean isPersonalSchedule) {
        super(id, ctx, callback, LoaderCode.SCHEDULE);

        this.group = group;
        this.course = course;
        this.teacher = teacher;
        this.isPersonalSchedule = isPersonalSchedule;
        this.date = Calendar.getInstance();
        this.date.setTime(date);
    }

    @Override
    protected void load() {
        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        String startDate = Lesson.convertDateToString(date);
        date.add(Calendar.DATE, 6);
        String endDate = Lesson.convertDateToString(date);

        //To avoid date changes
        date.add(Calendar.DATE, -6);

        if (isPersonalSchedule) {
            Log.d("Shika", "Here is personal schedule");
            String query = "select * from Courses inner join Schedule on(Courses.courseId = Schedule.courseId and" +
                " Courses.name = Schedule.lesson and Courses.teacher = Schedule.teacher and Courses.groups = " +
                "Schedule.groups) where Courses.isEnrolled = 1 and " +
                "(Schedule.date >= ? and Schedule.date <= ?)";
            cursor = dbh.rawQuery(query, new String[]{startDate, endDate});
        } else if (group != null) {
            Log.d("Shika", "Here is group schedule");
            cursor = dbh.query("Schedule", null, "groups = '" + group + "' and (date >= ? and date <= ?)", new String[]{startDate, endDate}, null, null, "start");
        } else if (teacher != null) {
            Log.d("Shika", "Here is teacher schedule");
            cursor = dbh.query("Schedule", null, "teacher = '" + teacher + "' and (date >= ? and date <= ?)", new String[]{startDate, endDate}, null, null, "start");
        } else if (course != null) {
            Log.d("Shika", "Here is course schedule");
            cursor = dbh.query("Schedule", null, "(courseId like ? or lesson like ?) and " + "(date >= ? and date <= ?)", new String[]{course, course, startDate, endDate}, null, null, "start");
        }
    }

    @Override
    protected void cursorParse(Cursor c) {
        ArrayList<ArrayList<Lesson>> lessonsArr = new ArrayList<ArrayList<Lesson>>();

        for (int i = 0; i < 7; i++)
            lessonsArr.add(new ArrayList<Lesson>());

        try {
            if (cursor.moveToFirst()) {
                HashMap<String, Lesson> lessons = new HashMap<String, Lesson>();

                int start = cursor.getColumnIndex("start");
                int end = cursor.getColumnIndex("end");
                int room = cursor.getColumnIndex("room");
                int lesson = cursor.getColumnIndex("lesson");
                int teacher = cursor.getColumnIndex("teacher");
                int date = cursor.getColumnIndex("date");
                int group = cursor.getColumnIndex("groups");
                int courseId = cursor.getColumnIndex("courseId");

                Log.d("Shika", getClass().getName() + ": " + cursor.getString(lesson) + ", " + cursor.getString(teacher));

                do {
                    Log.d("Shika", getClass().getName() + ": name: " + cursor.getString(lesson) + ", group: " + cursor.getString(group));
                    String id = cursor.getString(courseId);

                    boolean equals = false;

                    //Filling similar lessons with different group or teacher
                    while (lessons.containsKey(id)) {
                        Lesson temp = lessons.get(id);

                        if (temp.date.equals(cursor.getString(date)) && temp.start.equals(cursor.getString(start)) && temp.end.equals(cursor.getString(end)) && temp.room.equals(cursor.getString(room)) && temp.name.equals(cursor.getString(lesson))) {
                            if (!temp.group.equals(cursor.getString(group))) temp.group += ", " + cursor.getString(group);

                            if (!temp.teacher.equals(cursor.getString(teacher))) temp.teacher += ", " + cursor.getString(teacher);

                            equals = true;
                            break;
                        }

                        //It is another lesson
                        id += "$";
                    }

                    if (equals) continue;

                    String dateFormat = cursor.getString(date);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, Integer.parseInt("20" + dateFormat.substring(0, 2)));
                    calendar = setMonth(calendar, dateFormat);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateFormat.substring(4)));
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);

                    int day = calendar.get(Calendar.DAY_OF_WEEK) - 2;

                    lessons.put(id, new Lesson(cursor.getString(start), cursor.getString(end), cursor.getString(room), cursor.getString(lesson), cursor.getString(teacher), dateFormat, cursor.getString(group), day));
                } while (cursor.moveToNext());

                for (int i = 0; i < 7; i++) {
                    for (Lesson temp : lessons.values()) {
                        if (temp.day == i) lessonsArr.get(i).add(temp);
                    }
                }

            } else {
                Log.d("Shika", "Found nothing in database");
            }
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e("Shika", e.getMessage());
        }

        callback.receiveData(id, code, lessonsArr);

        cursor.close();
    }

    //Fucking bullshit for calendar
    private Calendar setMonth(Calendar calendar, String dateString) {
        switch (Integer.parseInt(dateString.substring(2, 4))) {
            case 1:
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                break;
            case 2:
                calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
                break;
            case 3:
                calendar.set(Calendar.MONTH, Calendar.MARCH);
                break;
            case 4:
                calendar.set(Calendar.MONTH, Calendar.APRIL);
                break;
            case 5:
                calendar.set(Calendar.MONTH, Calendar.MAY);
                break;
            case 6:
                calendar.set(Calendar.MONTH, Calendar.JUNE);
                break;
            case 7:
                calendar.set(Calendar.MONTH, Calendar.JULY);
                break;
            case 8:
                calendar.set(Calendar.MONTH, Calendar.AUGUST);
                break;
            case 9:
                calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
                break;
            case 10:
                calendar.set(Calendar.MONTH, Calendar.OCTOBER);
                break;
            case 11:
                calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
                break;
            case 12:
                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                break;

            default:
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                break;
        }

        return calendar;
    }
}
