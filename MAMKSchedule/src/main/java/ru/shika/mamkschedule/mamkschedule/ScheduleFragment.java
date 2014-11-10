package ru.shika.mamkschedule.mamkschedule;

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
	RecyclerView list;
	RecyclerView.Adapter listAdapter;
	RecyclerView.LayoutManager listLayoutManager;

	String color;

	boolean isFirst = true;

	public static ScheduleFragment newInstance(String color)
	{
		ScheduleFragment myFragment = new ScheduleFragment();

		Bundle args = new Bundle();
		args.putString("color", color);
		myFragment.setArguments(args);

		return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ArrayList<Lesson> lessons = new ArrayList<Lesson>();
		lessons.add(new Lesson("09:00", "12:00", "Kas/E012", "T5614SN, PC Technology", "Matti Juutilainen"));
		lessons.add(new Lesson("10:00", "13:00", "Kas/MB310", "T42052A Electronics and Measurements", "Reijo Vuohelainen"));

		color = getArguments().getString("color");

		listAdapter = new ScheduleListAdapter(lessons);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		list = (RecyclerView) rootView.findViewById(R.id.recycler);
		list.setHasFixedSize(true);
		list.setBackgroundColor(getResources().getColor(R.color.background));

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

		if(color != null)
			list.setBackgroundColor(Color.parseColor(color));
	}
}
