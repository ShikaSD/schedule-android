package ru.shika.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper
{
	private Context context;
	private String databaseName;

	public DBHelper(Context ctx)
	{
		super(ctx, "scheduleDB", null, 1);

		context = ctx;
		databaseName = "scheduleDB";
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

	@Override
	public synchronized SQLiteDatabase getReadableDatabase()
	{
		SQLiteDatabase db;
		try {
			db = super.getReadableDatabase();
		}
		catch (SQLiteException e) {
			Log.d("Shika", e.getMessage());
			File dbFile = context.getDatabasePath(databaseName);
			Log.d("Shika", "db path="+dbFile.getAbsolutePath());
			//db = SQLiteDatabase.openDatabase(/*DB_PATH + DB_NAME*/ dbFile.getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			db = SQLiteDatabase.openOrCreateDatabase(dbFile.getAbsolutePath(), null);
		}
		return db;
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase()
	{
		return super.getWritableDatabase();
	}

	@Override
	public synchronized void close()
	{
		super.close();
	}

	public synchronized long insert(String table, String columnHack, ContentValues contentValues)
	{
		return getWritableDatabase().insert(table, columnHack, contentValues);
	}

	public synchronized int update(String table, ContentValues contentValues, String where, String[] whereArgs)
	{
		return getWritableDatabase().update(table, contentValues, where, whereArgs);
	}

	public synchronized Cursor rawQuery(String query, String[] args)
	{
		return getReadableDatabase().rawQuery(query, args);
	}

	public synchronized Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String
		groupBy, String having, String orderBy)
	{
		return getReadableDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
}
