package ru.shika.mamkschedule.mamkschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment implements Interfaces.updateFragment
{

	RecyclerView list;
	ScheduleListAdapter listAdapter;
	RecyclerView.LayoutManager listLayoutManager;

	TextView empty;

	boolean isFirst = true;

	int globalDay = 0;

	public ArrayList<Lesson> lessons = new ArrayList<Lesson>();

	//Init Fragment
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

		listAdapter = new ScheduleListAdapter(lessons);
		globalDay = getArguments().getInt("day");
		Log.w("Shika", "globalDay in onCreate " + globalDay);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		empty = (TextView) rootView.findViewById(R.id.emptySchedule);
		list = (RecyclerView) rootView.findViewById(R.id.recycler);
		list.setHasFixedSize(true);

		listLayoutManager = new LinearLayoutManager(getParentFragment().getActivity());
		list.setLayoutManager(listLayoutManager);

		list.setAdapter(listAdapter);
		isFirst = false;

		if(lessons.isEmpty())
			empty.setVisibility(View.VISIBLE);
		else
			empty.setVisibility(View.GONE);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void update(ArrayList< ArrayList<Lesson>> list)
	{
		if(globalDay == 0)
			globalDay = getArguments().getInt("day");

		try
		{
			listAdapter.swapData(list.get(globalDay));
		}
		catch (Exception e){Log.e("Shika", "Error in update fragment");}

		if(lessons.isEmpty() && empty != null)
			empty.setVisibility(View.VISIBLE);
		else if(empty != null)
			empty.setVisibility(View.GONE);
	}

}
