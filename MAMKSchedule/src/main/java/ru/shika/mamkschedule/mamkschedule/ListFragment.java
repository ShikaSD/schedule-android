package ru.shika.mamkschedule.mamkschedule;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ListFragment extends Fragment implements Interfaces.Download
{
	Interfaces.groupFragmentCallback callback;

	//To sort them by course id
	Map<String, Integer> keys = new HashMap<String, Integer>(); //Ids
	ArrayList <ArrayList <String>> names = new ArrayList<ArrayList <String>>(); //Full name

	ListFragmentAdapter adapter;

	CircleImageView progressView;
	MaterialProgressDrawable progressDrawable;

	String fragmentType = "";

	DBHelper dbHelper;

	public static Fragment newInstance(String listType)
	{
		Fragment fragment = new ListFragment();

		Bundle args = new Bundle();
		args.putString("type", listType);
		fragment.setArguments(args);

		return fragment;
	}

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

		fragmentType = getArguments().getString("type");

		dbHelper = new DBHelper(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = db.rawQuery("select * from " + fragmentType + " order by name", null);
		cursorParse(c);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_list, container, false);


		ListView list = (ListView) rootView.findViewById(R.id.groupsList);

		adapter = new ListFragmentAdapter(getActivity(), keys, names);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if(fragmentType.equals("Courses"))
				{
					if(keys.containsValue(i))
						for(String key : keys.keySet())
							if (keys.get(key).equals(i))
							{
								callback.listSelected(fragmentType, key);
								return;
							}

				}
				callback.listSelected(fragmentType, names.get(i).get(0));
			}
		});

		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		int mCircleWidth = (int) (40 * metrics.density);
		int mCircleHeight = (int) (40 * metrics.density);

		progressView = new CircleImageView(getActivity(), getResources().getColor(R.color.background), 40/2);
		progressDrawable = new MaterialProgressDrawable(getActivity(), progressView);

		RelativeLayout.LayoutParams lParams =
			new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
		lParams.bottomMargin = 20;
		lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		progressView.setLayoutParams(lParams);

		progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
		progressView.setImageDrawable(progressDrawable);

		((ViewGroup) rootView).addView(progressView);
		progressView.setVisibility(View.GONE);

		return rootView;
	}

	@Override
	public void onDownloadEnd(String result)
	{}

	@Override
	public void updateInProgress(int amount)
	{
		Log.w("Shika", "update ListFragment " + amount);

		if(progressView.getVisibility() != View.VISIBLE)
		{
			progressView.setVisibility(View.VISIBLE);
			progressDrawable.start();
		}

		if(amount == -1)
		{
			progressDrawable.stop();
			progressView.setVisibility(View.GONE);
			return;
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("select * from "+ fragmentType+" order by name limit "+names.size()+", 5000", null);
		cursorParse(c);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onDateChanged(Date date)
	{}

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
		dbHelper.close();
	}
}
