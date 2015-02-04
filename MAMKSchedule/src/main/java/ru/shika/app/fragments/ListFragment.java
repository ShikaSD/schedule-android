package ru.shika.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ru.shika.Application;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.app.R;
import ru.shika.app.RecyclerItemClickListener;
import ru.shika.app.adapters.ListFragmentAdapter;
import ru.shika.app.interfaces.ControllerInterface;
import ru.shika.app.interfaces.LoaderCenterInterface;
import ru.shika.app.interfaces.ViewInterface;

import java.util.ArrayList;

public class ListFragment extends Fragment implements ViewInterface
{
	/**Fragment is used three times: in drawer menu and two types in edit
	 * isEditFragment = false, fragmentType = true : drawer
	 * isEditFragment = true : edit (typeName = null || != null)
	 */

	private ControllerInterface controller;
	private LoaderCenterInterface loader;

	//To sort them by course id
	private SparseArray <String> keys; //Ids
	private ArrayList <ArrayList <String>> names; //Full name

	private ListFragmentAdapter adapter;
	private RecyclerView list;
	private TextView empty;

	private CircleImageView progressView;
	private MaterialProgressDrawable progressDrawable;

	private String fragmentType;
	private String typeName;
	private boolean isEditFragment;

	private TranslateAnimation appear, disappear;

	private int id;

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
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		controller = Application.getController();
		loader = Application.getController().getLoader();

		animationInit();

		keys = new SparseArray<String>();
		names = new ArrayList<ArrayList<String>>();

		fragmentType = getArguments().getString("type");
		typeName = null;
		isEditFragment = getArguments().getBoolean("edit", false);

		if(getArguments().getString("name") != null)
		{
			isEditFragment = true;
			typeName = getArguments().getString("name");
		}

		if(fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser") )
		{
			Log.d("Shika", "Checkboxes showed");
			adapter = new ListFragmentAdapter(keys, names, true);
			adapter.showCheckboxes(true);
		}
		else
			adapter = new ListFragmentAdapter(keys, names, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		list = (RecyclerView) rootView.findViewById(R.id.groupsList);
		empty = (TextView) rootView.findViewById(R.id.empty);

		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener()
		{
			@Override
			public void onItemClick(View view, int i)
			{
				if (isEditFragment && (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser")) &&
					adapter.isChecked(i))
					return;

				String item;
				if (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser"))
				{
					item = keys.get(i);
				} else
					item = names.get(i).get(0);

				Log.d("Shika", i + ": " + item);

				if (isEditFragment && typeName == null)
				{
					controller.listItemInEditSelected(fragmentType, item);
				}
				else if (typeName != null)
				{
					controller.listItemInEditSelected("Courses", item);
				} else
					controller.listItemSelected(fragmentType, item);
			}
		}));

		progressInit();

		((ViewGroup) rootView).addView(progressView);
		progressView.setVisibility(View.GONE);

		load();

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		id = controller.register(this);
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
	public void onDestroy()
	{
		super.onDestroy();

		controller.unregister(this);
	}

	private void load()
	{
		Log.d("Shika", "In fragment " + fragmentType + " : " + typeName);
		controller.load(this, fragmentType, typeName, null);
	}

	private void update(Object o)
	{
		ArrayList <Object> items = (ArrayList<Object>) o;
		ArrayList <ArrayList <String>> tempNames = (ArrayList<ArrayList<String>>) items.get(0);
		SparseArray <String> tempKeys = (SparseArray <String>) items.get(1);
		final SparseBooleanArray checks = (items.size() > 2) ? (SparseBooleanArray) items.get(2) : null;

		int tempSize = tempKeys.size();
		int size = keys.size();

		for(int i = 0; i < tempSize; i++)
		{
			boolean isAdded = false;

			for(int j = 0; j < size; j++)
			{
				if(tempKeys.get(i).equals(keys.get(j)))
				{
					int index = keys.keyAt(j);
					int ind = tempKeys.keyAt(i);

					if(!names.get(index).equals(tempNames.get(ind)))
						names.get(index).addAll(tempNames.get(ind));

					isAdded = true;
					break;
				}
			}

			if(!isAdded)
			{
				keys.append(tempKeys.keyAt(i), tempKeys.valueAt(i));
				names.add(tempNames.get(tempKeys.keyAt(i)));
			}
		}

		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				adapter.swapData();

				if(checks != null)
					adapter.check(checks);

				if(keys.size() == 0)
				{
					//Show that list is empty
					empty.setVisibility(View.VISIBLE);
					list.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public void downloadEnd()
	{
		dismissProgress();
	}

	@Override
	public void updateIsRunning()
	{
		showProgress();

		update(loader.getData(id));
		loader.ready(id);
	}

	@Override
	public boolean visible()
	{
		return isVisible();
	}

	@Override
	public void showProgress()
	{
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if(progressView.getVisibility() != View.VISIBLE)
					progressView.startAnimation(appear);
			}
		});
	}

	@Override
	public void dismissProgress()
	{
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if(progressView.getVisibility() == View.VISIBLE)
					progressView.startAnimation(disappear);
			}
		});
	}
}
