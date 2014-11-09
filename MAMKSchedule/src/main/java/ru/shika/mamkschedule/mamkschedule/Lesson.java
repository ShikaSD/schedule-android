package ru.shika.mamkschedule.mamkschedule;

import android.graphics.drawable.Drawable;

public class Lesson
{
	String start, end, room, name, teacher;

	public Lesson(String start, String end, String room, String name, String teacher)
	{
		this.start = start;
		this.end = end;
		this.room = room;
		this.name = name;
		this.teacher = teacher;
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
