package ru.shika.mamkschedule.mamkschedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
	public DBHelper(Context ctx)
	{
		super(ctx, "myDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase)
	{
		sqLiteDatabase.execSQL("create table schedule (id integer primary key autoincrement, day integer, name text, start text, end text, room text, teacher text);");
		sqLiteDatabase.execSQL("create table enrol (id integer primary key autoincrement, name text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
	{

	}
}
