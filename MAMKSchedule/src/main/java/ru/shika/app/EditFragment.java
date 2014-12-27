package ru.shika.app;

import android.app.Activity;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditFragment extends Fragment
{
	Interfaces.groupFragmentCallback callback;

	//To sort them by course id
	Map<String, Integer> keys = new HashMap<String, Integer>(); //Ids
	ArrayList <ArrayList <String>> names = new ArrayList<ArrayList <String>>(); //Full name

	Button add;
	TextView empty;

	ListView list;
	DrawerListAdapter editAdapter;
	ListFragmentAdapter listAdapter;
	TextView header;

	String[] strings;
	String[] titles;
	TypedArray drawables;
	ArrayList <Lesson.DrawerItem> drawerItems;

	ActionMode actionMode;

	DBHelper dbh;

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

		titles = getResources().getStringArray(R.array.drawer_strings);

		strings = getResources().getStringArray(R.array.edit_strings);
		drawables = getResources().obtainTypedArray(R.array.drawer_drawables);

		drawerItems = new ArrayList<Lesson.DrawerItem>();

		for(int i = 0; i < strings.length; i++)
			drawerItems.add(new Lesson.DrawerItem(strings[i], drawables.getDrawable(i)));
		drawables.recycle();

		dbh = new DBHelper(getActivity());

		editAdapter = new DrawerListAdapter(getActivity(), drawerItems);
		listAdapter = new ListFragmentAdapter(getActivity(), keys, names);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		SQLiteDatabase db = dbh.getReadableDatabase();
		Cursor c = db.rawQuery("select * from Courses where isEnrolled = 1", null);
		cursorParse(c);

		listAdapter.notifyDataSetChanged();

		empty = (TextView) view.findViewById(R.id.fragment_edit_empty);
		empty.setText(getResources().getText(R.string.edit_empty));
		empty.setGravity(Gravity.CENTER);

		add = (Button) view.findViewById(R.id.fragment_edit_add);
		add.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onAddClick(view);
			}
		});

		list = (ListView) view.findViewById(R.id.fragment_edit_list);
		header = (TextView) view.findViewById(R.id.fragment_edit_list_header);
		if(list.getAdapter() == null)
		{
			list.setAdapter(listAdapter);
			list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		}

		if(keys.size() > 0)
		{
			header.setText("Your courses are:");
			header.setVisibility(View.VISIBLE);
			add.setVisibility(View.GONE);
			empty.setVisibility(View.GONE);
		}
		else
		{
			list.setVisibility(View.GONE);
		}

		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if(!(list.getAdapter() instanceof DrawerListAdapter))
				{
					if(actionMode != null)
						return;

					actionMode = ((MainActivity) getActivity()).startSupportActionMode((MainActivity) getActivity());
					view.setSelected(true);
					return;
				}

				switch (i)
				{
					case 0:
						callback.editTypeSelected(titles[1]);
						break;
					case 1:
						callback.editTypeSelected(titles[2]);
						break;
					case 2:
						callback.editTypeSelected(titles[3]);
						break;
				}
			}
		});
	}

	public void backPressed()
	{
		list.setAdapter(listAdapter);
		list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

		if(keys.size() > 0)
		{
			header.setText("Your courses are:");
			add.setVisibility(View.GONE);
			empty.setVisibility(View.GONE);
		}
		else
		{
			header.setVisibility(View.GONE);
			list.setVisibility(View.GONE);
			add.setVisibility(View.VISIBLE);
			empty.setVisibility(View.VISIBLE);
		}
	}


	public void onAddClick(View v)
	{
		((MainActivity) getActivity()).onAddClick(v);
	}

	public void addClick(View v)
	{
		empty.setVisibility(View.GONE);
		add.setVisibility(View.GONE);

		header.setVisibility(View.VISIBLE);
		list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		list.setVisibility(View.VISIBLE);
		list.setAdapter(editAdapter);
	}

	private void cursorParse(Cursor c)
	{
		if(c.moveToFirst())
		{
			int name = c.getColumnIndex("name");
			int id = c.getColumnIndex("courseId");
			do
			{
				String courseId;

				if(id == -1 || c.getString(id).equals(""))
					courseId = c.getString(name);
				else
					courseId = c.getString(id);

				if (!keys.containsKey(courseId))
				{
					names.add(new ArrayList<String>());
					keys.put(courseId, names.size() - 1);
				}

				int index = keys.get(courseId);

				if (!names.get(index).contains(c.getString(name)))
					names.get(index).add(c.getString(name));

			}
			while (c.moveToNext());
		}
		else
		{
			//Toast.makeText(getActivity(), "No "+fragmentType.toLowerCase()+" found", Toast.LENGTH_SHORT).show();
		}
		c.close();
		dbh.close();
	}
}
