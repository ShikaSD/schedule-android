package ru.shika.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
	public DBHelper(Context ctx)
	{
		super(ctx, "scheduleDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase)
	{
		sqLiteDatabase.execSQL("create table Schedule (lesson text, " +
			"start text, end text, room text, teacher text, groups text, date text, lessonId text, courseId text)");
		sqLiteDatabase.execSQL("create table Groups (id integer primary key autoincrement, name text)");
		sqLiteDatabase.execSQL("create table Teachers (id integer primary key autoincrement, name text)");
		sqLiteDatabase.execSQL("create table Courses (id integer primary key autoincrement, name text, courseId text," +
			"groups text, teacher text, isEnrolled integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
	{

	}
}