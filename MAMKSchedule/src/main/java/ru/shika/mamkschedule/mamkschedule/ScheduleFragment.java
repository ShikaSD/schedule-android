package ru.shika.mamkschedule.mamkschedule;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment
{
	MainActivity.DBHelper dbh;
	SQLiteDatabase db;

	RecyclerView list;
	RecyclerView.Adapter listAdapter;
	RecyclerView.LayoutManager listLayoutManager;

	boolean isFirst = true;

	public static ScheduleFragment newInstance(int day)
	{
		ScheduleFragment myFragment = new ScheduleFragment();

		Bundle args = new Bundle();
		args.putInt("day", day);
		myFragment.setArguments(args);

		return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//InitDB
		dbh = new MainActivity.DBHelper(getParentFragment().getActivity());
		db = dbh.getWritableDatabase();

		ArrayList<Lesson> lessons = new ArrayList<Lesson>();

		Cursor cursor = db.query("schedule", null, "day = " + getArguments().getInt("day"), null, null, null, null);
		if(cursor != null)
		{
			if(cursor.moveToFirst())
			{
				do
				{
					String start, end, room, name, teacher;
					start = end = room = name = teacher = null;
					for(String cn : cursor.getColumnNames())
					{
						if(cn.equals("start"))
							start = cursor.getString(cursor.getColumnIndex(cn));
						if(cn.equals("end"))
							end = cursor.getString(cursor.getColumnIndex(cn));
						if(cn.equals("room"))
							room = cursor.getString(cursor.getColumnIndex(cn));
						if(cn.equals("name"))
							name = cursor.getString(cursor.getColumnIndex(cn));
						if(cn.equals("teacher"))
							teacher = cursor.getString(cursor.getColumnIndex(cn));
					}
					lessons.add(new Lesson(start, end, room, name, teacher));
				}
				while (cursor.moveToNext());
			}
		}
		db.close();
		listAdapter = new ScheduleListAdapter(lessons);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		list = (RecyclerView) rootView.findViewById(R.id.recycler);
		list.setHasFixedSize(true);

		listLayoutManager = new LinearLayoutManager(getParentFragment().getActivity());
		list.setLayoutManager(listLayoutManager);

		list.setAdapter(listAdapter);
		isFirst = false;

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}
}
