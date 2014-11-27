package ru.shika.mamkschedule.mamkschedule;

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
		sqLiteDatabase.execSQL("create table schedule (lesson text, " +
			"start text, end text, room text, teacher text, groups text, date text, isEnrolled integer, lessonId text)");
		sqLiteDatabase.execSQL("create table groups (id integer primary key autoincrement, name text)");
		sqLiteDatabase.execSQL("create table teachers (id integer primary key autoincrement, name text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
	{

	}
}
