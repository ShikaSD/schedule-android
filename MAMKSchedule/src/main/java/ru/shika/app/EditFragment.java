package ru.shika.app;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;

public class EditFragment extends Fragment
{
	private Interfaces.groupFragmentCallback callback;

	//To sort them by course id
	private SparseArray <String> keys; //Ids
	private ArrayList <ArrayList <String>> names; //Full name

	private Button add;
	private TextView empty;

	private ListView list;
	private SimpleAdapter editAdapter;
	private ListFragmentAdapter listAdapter;
	private TextView header;

	private String[] strings;
	private String[] titles;

	private ActionMode actionMode;

	private DBHelper dbh;

	public boolean wasInEditMode;

	private Animation editOpen, editClose;


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

		keys = new SparseArray<String>();
		names = new ArrayList<ArrayList <String>>();

		titles = getResources().getStringArray(R.array.drawer_strings);
		strings = getResources().getStringArray(R.array.edit_strings);

		dbh = getDBH();

		editAdapter = new SimpleAdapter(getActivity(), strings);
		listAdapter = new ListFragmentAdapter(getActivity(), keys, names, true);

		wasInEditMode = false;

		animationInit();
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

		update();

		if(wasInEditMode)
		{
			list.setAdapter(editAdapter);
		}

		if(list.getAdapter() == null)
			list.setAdapter(listAdapter);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if(list.getAdapter() instanceof ListFragmentAdapter)
				{
					if(listAdapter.getCheckedAmount() == 0)
						actionMode = ((MainActivity) getActivity()).startSupportActionMode((MainActivity) getActivity());

					listAdapter.toggle(i);

					if(listAdapter.getCheckedAmount() == 0)
						actionMode.finish();

					return;
				}

				callback.editTypeSelected(titles[i + 1]);
			}
		});
	}

	//For working with activity dbh
	private DBHelper getDBH()
	{
		return ((MainActivity) getActivity()).getDBHelper();
	}

	private void animationInit()
	{
		editOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.calendar_open);
		editOpen.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				switchToEdit();
				header.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				header.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});
		editClose = AnimationUtils.loadAnimation(getActivity(), R.anim.calendar_close);
		editClose.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				header.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				switchToNormal();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});
	}

	public void backPressed()
	{
		if(wasInEditMode)
			list.startAnimation(editClose);

		wasInEditMode = false;
	}


	public void onAddClick(View v)
	{
		((MainActivity) getActivity()).onAddClick(v);
	}

	public void addClick(View v)
	{
		list.startAnimation(editOpen);

		wasInEditMode = true;
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

				int index = -1;

				for(int i = 0; i < keys.size(); i++)
					if(keys.valueAt(i).equals(courseId))
					{
						index = keys.keyAt(i);
						break;
					}

				if (index < 0)
				{
					names.add(new ArrayList<String>());
					keys.put(names.size() - 1, courseId);
					index = names.size() - 1;
				}

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
	}

	public void showCheckboxes(boolean show)
	{
		if(!show)
			listAdapter.unCheck();
		listAdapter.showCheckboxes(show);
	}

	public String[] getChecked()
	{
		return listAdapter.getChecked();
	}

	public void update()
	{
		((MainActivity) getActivity()).showProgressView();
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				names.clear();
				keys.clear();

				Cursor c = dbh.rawQuery("select * from Courses where isEnrolled = 1", null);
				Log.d("Shika", c.getCount() + " in Edit fragment");
				cursorParse(c);

				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{

						if(!wasInEditMode)
						{
							if (keys.size() > 0)
							{
								header.setText("Your courses are:");
								header.setVisibility(View.VISIBLE);
								add.setVisibility(View.GONE);
								empty.setVisibility(View.GONE);
							} else
							{
								list.setVisibility(View.GONE);
								header.setVisibility(View.GONE);
								add.setVisibility(View.VISIBLE);
								empty.setVisibility(View.VISIBLE);
							}
						}
						else
						{
							header.setVisibility(View.VISIBLE);
							add.setVisibility(View.GONE);
							empty.setVisibility(View.GONE);
						}

						listAdapter.notifyDataSetChanged();

						((MainActivity) getActivity()).dismissProgressView();
					}
				});
			}
		});
		thread.start();
	}

	private void switchToEdit()
	{
		list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		list.setVisibility(View.VISIBLE);
		list.setAdapter(editAdapter);
		list.setPadding(list.getPaddingLeft() + (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
			list.getListPaddingTop(), list.getPaddingRight(), list.getPaddingBottom());

		header.setText(R.string.edit_header);

		empty.setVisibility(View.GONE);
		add.setVisibility(View.GONE);
	}

	private void switchToNormal()
	{
		list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		list.setAdapter(listAdapter);
		list.setPadding(list.getPaddingLeft() - (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
			list.getListPaddingTop(), list.getPaddingRight(), list.getPaddingBottom());
		if(keys.size() > 0)
		{
			header.setText(R.string.edit_list_header);
			header.setVisibility(View.VISIBLE);
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
}
