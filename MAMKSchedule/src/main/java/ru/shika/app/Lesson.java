package ru.shika.app;

import android.graphics.drawable.Drawable;

public class Lesson
{
	String start, end, room, name, teacher, date, group;
	int day;

	public Lesson(String start, String end, String room, String name, String teacher, String date, String group,
				  int day)
	{
		this.start = start;
		this.end = end;
		this.room = room;
		this.name = name;
		this.teacher = teacher;
		this.date = date;
		this.group = group;
		this.day = day;
	}

	public static class DrawerItem
	{
		String string;
		Drawable drawable;

		public DrawerItem(String string, Drawable drawable)
		{
			this.string = string;
			this.drawable = drawable;
		}
	}
}
