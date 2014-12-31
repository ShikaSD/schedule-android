package ru.shika.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ListFragment extends Fragment implements Interfaces.Download, LoaderManager.LoaderCallbacks<Cursor>
{
	/**Fragment is used three times: in drawer menu and two types in edit
	 * isEditFragment = false, fragmentType = true : drawer
	 * isEditFragment = true : edit (typeName = null || != null)
	 */
	private Interfaces.groupFragmentCallback callback;

	//To sort them by course id
	private Map<String, Integer> keys; //Ids
	private ArrayList <ArrayList <String>> names; //Full name
	private SparseArray <Boolean> checks;

	private ListFragmentAdapter adapter;
	private ListView list;
	private TextView empty;

	private CircleImageView progressView;
	private MaterialProgressDrawable progressDrawable;

	private String fragmentType;
	private String typeName;
	private boolean isEditFragment;

	private DBHelper dbHelper;

	private int from = 0;

	private TranslateAnimation appear, disappear;

	public static Fragment newInstance(String listType, boolean isEditFragment)
	{
		Fragment fragment = new ListFragment();

		Bundle args = new Bundle();
		args.putString("type", listType);
		args.putBoolean("edit", isEditFragment);
		fragment.setArguments(args);

		return fragment;
	}

	public static Fragment newEditInstance(String listType, String name)
	{
		Fragment fragment = new ListFragment();

		Bundle args = new Bundle();
		args.putString("type", listType);
		args.putString("name", name);
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

		animationInit();

		keys = new HashMap<String, Integer>();
		names = new ArrayList<ArrayList<String>>();
		checks = new SparseArray<Boolean>();

		fragmentType = getArguments().getString("type");
		isEditFragment = getArguments().getBoolean("edit", false);

		if(getArguments().getString("name") != null)
		{
			isEditFragment = true;
			typeName = getArguments().getString("name");
		}

		dbHelper = new DBHelper(getActivity());

		if(typeName == null)
		{
			if (getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_LIST) == null)
				getActivity().getSupportLoaderManager().initLoader(MainActivity.LOADER_LIST, null, this);
			else
				getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_LIST, null, this);
		}
		else
		{
			if (getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_EDIT) == null)
				getActivity().getSupportLoaderManager().initLoader(MainActivity.LOADER_EDIT, null, this);
			else
				getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_EDIT, null, this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		list = (ListView) rootView.findViewById(R.id.groupsList);
		empty = (TextView) rootView.findViewById(R.id.empty);

		if(fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser") )
		{
			adapter = new ListFragmentAdapter(getActivity(), keys, names, true);
			adapter.showCheckboxes(true);
		}
		else
			adapter = new ListFragmentAdapter(getActivity(), keys, names, false);

		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if(isEditFragment && (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser")) &&
					adapter.isChecked(i))
					return;

				String item = "";
				if(fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser") )
				{
					if(keys.containsValue(i))
						for(String key : keys.keySet())
							if (keys.get(key).equals(i))
							{
								item = key;
								break;
							}

				}
				else
					item = names.get(i).get(0);

				if(isEditFragment && typeName == null)
				{
					callback.listItemInEditSelected(fragmentType, item);
				}
				else if(typeName != null)
				{
					callback.listItemInEditSelected("Courses", item);
				}
				else
					callback.listItemSelected(fragmentType, item);
			}
		});

		progressInit();

		((ViewGroup) rootView).addView(progressView);
		progressView.setVisibility(View.GONE);

		if(typeName != null)
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_EDIT, null, this);
		else
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_LIST, null, this);

		return rootView;
	}

	private void animationInit()
	{
		appear = (TranslateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.progress_drawable_appear);
		appear.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				progressView.setVisibility(View.VISIBLE);
				progressDrawable.start();
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{

			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});

		disappear = (TranslateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.progress_drawable_disappear);
		disappear.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				progressDrawable.stop();
				progressView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});
	}

	private void progressInit()
	{
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		int mCircleWidth = (int) (40 * metrics.density);
		int mCircleHeight = (int) (40 * metrics.density);

		progressView = new CircleImageView(getActivity(), getResources().getColor(R.color.background), 40/2);
		progressDrawable = new MaterialProgressDrawable(getActivity(), progressView);

		RelativeLayout.LayoutParams lParams =
			new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
		lParams.bottomMargin = 22;
		lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		progressView.setLayoutParams(lParams);

		progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
		progressView.setImageDrawable(progressDrawable);


	}

	@Override
	public void onDownloadEnd(String result)
	{}

	@Override
	public void updateInProgress(int amount)
	{
		if(keys != null)
			from = keys.size() - 1;
		if(from < 0 || keys == null) from = 0;

		if(getActivity() != null)
		{
			if (progressView.getVisibility() != View.VISIBLE)
			{
				progressView.startAnimation(appear);
				list.setVisibility(View.VISIBLE);
				empty.setVisibility(View.GONE);
			}

			if (amount == -1)
			{
				progressView.startAnimation(disappear);
				return;
			}

			if(typeName != null)
				getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_EDIT, null, this);
			else
				getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_LIST, null, this);
		}
	}

	@Override
	public void onDateChanged(Date date)
	{}

	private void cursorParse(Cursor c)
	{
		checks.clear();
		if(c.moveToFirst())
		{
			int name = c.getColumnIndex("name");
			int id = c.getColumnIndex("courseId");
			int isEnrolled = c.getColumnIndex("isEnrolled");
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

				if(isEnrolled != -1)
					if(c.getInt(isEnrolled) == 1)
						checks.put(index, true);

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

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		if(i == MainActivity.LOADER_EDIT)
			return new ListLoader(getActivity(), dbHelper, typeName, -1);

		return new ListLoader(getActivity(), dbHelper, fragmentType, from);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		cursorParse(cursor);

		adapter.notifyDataSetChanged();

		if(adapter.isCheckingList)
			adapter.check(checks);
		Log.w("Shika", fragmentType);

		if(keys.size() == 0)
		{
			list.setVisibility(View.GONE);
			empty.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}

	public static class ListLoader extends CursorLoader
	{
		DBHelper dbh;
		String type;
		int from;

		public ListLoader(Context context, DBHelper dbh, String type, int from)
		{
			super(context);
			this.dbh = dbh;
			this.type = type;
			this.from = from;
		}

		@Override
		public Cursor loadInBackground()
		{
			Cursor c;

			SQLiteDatabase db = dbh.getReadableDatabase();

			if(from >= 0)
				c = db.rawQuery("select * from "+ type +" order by name limit "+from+", 5000", null);
			else
				c = db.rawQuery("select * from Courses where groups like '%"+type+"%' or teacher like '%"+type+"%' " +
					"order by name", null);

			return c;
		}
	}
}
