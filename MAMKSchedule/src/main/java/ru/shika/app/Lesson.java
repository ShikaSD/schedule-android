package ru.shika.app;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Lesson {
    public String start, end, room, name, teacher, date, group;
    public int day;
    public Date calendar;

    public Lesson(String start, String end, String room, String name, String teacher, String date, String group, int day) {
        this.start = start;
        this.end = end;
        this.room = room;
        this.name = name;
        this.teacher = teacher;
        this.date = date;
        this.group = group;
        this.day = day;
    }

    public Lesson(String group, String teacher, String name, @Nullable Calendar date) {
        this.group = group;
        this.teacher = teacher;
        this.name = name;

        if (date != null) calendar = new Date(date.getTimeInMillis());
    }

    public static String convertDateToString(Calendar date) {
        //Formatting date from Calendar to YYMMDD
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");

        return formatter.format(date.getTime());
    }

    public static class DrawerItem {
        public String string;
        public Drawable drawable;

        public DrawerItem(String string, Drawable drawable) {
            this.string = string;
            this.drawable = drawable;
        }
    }
}
