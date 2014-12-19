package ru.shika.mamkschedule.mamkschedule;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class EditFragment extends Fragment
{
	ListView list;
	DrawerListAdapter adapter;

	protected String[] titles;
	protected TypedArray drawables;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//adapter = new DrawerListAdapter(getActivity(), )
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		list = (ListView) view.findViewById(R.id.fragment_schedule_list);
	}
}
