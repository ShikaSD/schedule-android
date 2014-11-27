package ru.shika.mamkschedule.mamkschedule;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class GroupFragment extends Fragment implements Interfaces.Download
{
	Interfaces.groupFragmentCallback callback;

	String[] groupNames = new String[160];

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		callback = (Interfaces.groupFragmentCallback) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		for(int i = 0; i < groupNames.length; i++)
			groupNames[i] = "";

		DBHelper dbHelper = new DBHelper(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.query("groups", null, null, null, null, null, null);

		if(c.moveToFirst())
		{
			int name = c.getColumnIndex("name");
			int i = 0;
			do
			{
				groupNames[i++] = c.getString(name);
			}
			while (c.moveToNext());
		}
		else
		{
			Toast.makeText(getActivity(), "No groups found", Toast.LENGTH_SHORT).show();
		}

		c.close();
		dbHelper.close();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

		ListView list = (ListView) rootView.findViewById(R.id.groupsList);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, groupNames);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				callback.groupSelected(groupNames[i]);
			}
		});

		return rootView;
	}

	@Override
	public void onDownloadEnd(String result)
	{

	}

	@Override
	public void updateEnded()
	{

	}
}
