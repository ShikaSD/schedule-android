package ru.shika.app;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
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

public class ListFragment extends Fragment implements Interfaces.Download, LoaderManager.LoaderCallbacks<Cursor>
{
	/**Fragment is used three times: in drawer menu and two types in edit
	 * isEditFragment = false, fragmentType = true : drawer
	 * isEditFragment = true : edit (typeName = null || != null)
	 */
	private Interfaces.groupFragmentCallback callback;

	//To sort them by course id
	private SparseArray <String> keys; //Ids
	private ArrayList <ArrayList <String>> names; //Full name
	private SparseArray <Boolean> checks;

	private ListFragmentAdapter adapter;
	private ListView list;
	private TextView empty;

	private CircleImageView progressView;
	private MaterialProgressDrawable progressDrawable;

	private boolean isFinished;

	private String fragmentType;
	private String typeName;
	private boolean isEditFragment;

	private DBHelper dbh;

	public int from = 0;

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

		keys = new SparseArray<String>();
		names = new ArrayList<ArrayList<String>>();
		checks = new SparseArray<Boolean>();

		fragmentType = getArguments().getString("type");
		isEditFragment = getArguments().getBoolean("edit", false);
		isFinished = false;

		if(getArguments().getString("name") != null)
		{
			isEditFragment = true;
			typeName = getArguments().getString("name");
		}

		dbh = getDBH();

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


		if(fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser") )
		{
			adapter = new ListFragmentAdapter(getActivity(), keys, names, true);
			adapter.showCheckboxes(true);
		}
		else
			adapter = new ListFragmentAdapter(getActivity(), keys, names, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		list = (ListView) rootView.findViewById(R.id.groupsList);
		empty = (TextView) rootView.findViewById(R.id.empty);

		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if(isEditFragment && (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser")) &&
					adapter.isChecked(i))
					return;

				String item;
				if(fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser") )
				{
					item = keys.get(i);
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

		/*if(typeName != null)
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_EDIT, null, this);
		else
			getActivity().getSupportLoaderManager().restartLoader(MainActivity.LOADER_LIST, null, this);*/

		return rootView;
	}

	//For working with activity dbh
	private DBHelper getDBH()
	{
		return ((MainActivity) getActivity()).getDBHelper();
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

		progressView = new CircleImageView(getActivity(), getResources().getColor(R.color.white), 40/2);
		progressDrawable = new MaterialProgressDrawable(getActivity(), progressView);

		RelativeLayout.LayoutParams lParams =
			new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
		lParams.bottomMargin = (int) getResources().getDimension(R.dimen.function_button_vertical_margin);
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
		Log.d("Shika", "updateInProgress in " + fragmentType);
		if(keys != null)
			from = keys.size() - 1;
		if(from < 0 || keys == null) from = 0;

		isFinished = true;

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

			if(isVisible())
			{
				Log.d("Shika", "restart loader");
				if (typeName != null)
					getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_EDIT).forceLoad();
				else
				{
					ListLoader.setFrom(from);
					getActivity().getSupportLoaderManager().getLoader(MainActivity.LOADER_LIST).forceLoad();
				}
			}
		}
	}

	@Override
	public void onDateChanged(Date date)
	{}

	private void cursorParse(Cursor c)
	{
		if(c.getCount() == 0 && fragmentType.equals("Courses"))
		{
			c = dbh.rawQuery("select * from Courses where isEnrolled = 1", null);
			Log.w("Shika", "Shit works");
		}

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

				if(isEnrolled != -1 && c.getInt(isEnrolled) == 1)
				{
					checks.put(index, true);
				}

			}
			while (c.moveToNext());
		}
		else
		{
			//Toast.makeText(getActivity(), "No "+fragmentType.toLowerCase()+" found", Toast.LENGTH_SHORT).show();
		}

		c.close();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
	{
		if(dbh == null)
			dbh = getDBH();

		if(i == MainActivity.LOADER_EDIT)
			return new ListLoader(getActivity(), dbh, typeName, -1);

		return new ListLoader(getActivity(), dbh, fragmentType, from);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{

				try
				{
					cursorParse(cursor);
				}
				catch (Exception e){if(e.getMessage() != null) Log.e("Shika", e.getMessage());}
				finally
				{
					if (MainActivity.isActivityRunning)
						getActivity().runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{

								adapter.notifyDataSetChanged();

								if (adapter.isCheckingList)
									adapter.check(checks);

								if (isFinished || keys.size() > 0)
									callback.dismissProgressView();

								if (keys.size() == 0)
								{
									list.setVisibility(View.GONE);
									empty.setVisibility(View.VISIBLE);
								} else
								{
									list.setVisibility(View.VISIBLE);
								}
							}
						});
				}
			}
		}).start();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{

	}

	public static class ListLoader extends CursorLoader
	{
		DBHelper dbh;
		String type;
		public static int from;

		public ListLoader(Context context, DBHelper dbh, String type, int from)
		{
			super(context);
			this.dbh = dbh;
			this.type = type;
			this.from = from;
		}

		public static void setFrom(int from)
		{
			ListLoader.from = from;
		}

		@Override
		public Cursor loadInBackground()
		{
			Cursor c;

			if(from >= 0)
				c = dbh.rawQuery("select * from "+ type +" order by name limit "+from+", 10000", null);
			else
				c = dbh.rawQuery("select * from Courses where groups like '%"+type+"%' or teacher like '%"+type+"%' " +
					"order by name", null);

			return c;
		}
	}
}
