package ru.shika.mamkschedule.mamkschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends Fragment
{
	RecyclerView list;
	RecyclerView.Adapter listAdapter;
	RecyclerView.LayoutManager listLayoutManager;

	public static ScheduleFragment newInstance(String date) {
		ScheduleFragment myFragment = new ScheduleFragment();

		Bundle args = new Bundle();
		args.putString("date", date);
		myFragment.setArguments(args);

		return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

		list = (RecyclerView) rootView.findViewById(R.id.recycler);


		return rootView;
	}
}
