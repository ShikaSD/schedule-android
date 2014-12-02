package ru.shika.mamkschedule.mamkschedule;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import ru.shika.android.ProgressView;

import java.util.ArrayList;
import java.util.Date;

public class ListFragment extends Fragment implements Interfaces.Download
{
	Interfaces.groupFragmentCallback callback;

	ArrayList <String> names = new ArrayList<String>();
	ListFragmentAdapter adapter;

	ProgressView progressView;

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

		Cursor c = db.query(fragmentType, null, null, null, null, null, null);

		if(c.moveToFirst())
		{
			int name = c.getColumnIndex("name");
			do
			{
				names.add(c.getString(name));
			}
			while (c.moveToNext());
		}
		else
		{
			Toast.makeText(getActivity(), "No "+fragmentType.toLowerCase()+" found", Toast.LENGTH_SHORT).show();
		}

		c.close();
		dbHelper.close();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_list, container, false);


		ListView list = (ListView) rootView.findViewById(R.id.groupsList);

		adapter = new ListFragmentAdapter(getActivity(), names);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				callback.listSelected(fragmentType, names.get(i));
			}
		});

		progressView = (ProgressView) rootView.findViewById(R.id.fragment_list_progress);

		int sdk = Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
			progressView.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button));
		else
			progressView.setBackground(getResources().getDrawable(R.drawable.round_button));

		ViewCompat.setElevation(progressView, 8);
		ViewCompat.setTranslationZ(progressView, 8);
		progressView.invalidate();

		return rootView;
	}

	@Override
	public void onDownloadEnd(String result)
	{}

	@Override
	public void updateInProgress(int amount)
	{
		Log.w("Shika", "update ListFragment " + amount);

		if(amount == -1)
		{
			progressView.setVisibility(View.GONE);
			return;
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("select * from "+ fragmentType+" limit "+names.size()+", 5000", null);

		if(c.moveToFirst())
		{
			int name = c.getColumnIndex("name");
			do
			{
				names.add(c.getString(name));
			}
			while (c.moveToNext());
			adapter.notifyDataSetChanged();
		}
		else
		{
			//Toast.makeText(getActivity(), "No "+fragmentType.toLowerCase()+" found", Toast.LENGTH_SHORT).show();
		}

		c.close();
		dbHelper.close();
	}

	@Override
	public void onDateChanged(Date date)
	{}
}
