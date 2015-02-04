package ru.shika.app;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import ru.shika.app.fragments.*;
import ru.shika.app.interfaces.*;
import ru.shika.app.loaders.LoaderCenter;

import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

public class Controller implements ControllerInterface, DialogCallback, ActionMode.Callback
{
	public enum Dialog {
		DIALOG_ADD, DIALOG_REMOVE
	}

	//For loader fabric
	public static SparseArray <Lesson> items;

	//Basic things and some interfaces
	private LoaderCenter loaderCenter;
	private SparseArray <ViewInterface> viewInterfaces;
	private DateInterface dateInterface;
	private EditInterface editInterface;
	private ActivityInterface activity;
	private Context ctx;

	private int id;
	private int visibleId;

	//Chosen date
	private Calendar globalDate;

	public static boolean isActivityRunning;

	private boolean isActivityProgressRunning;

	//For fragment changes
	private String visibleFragmentTag;
	private Stack<String> backStack;

	private String[] tags;

	//Action mode
	private ActionMode actionMode;
	private boolean isActionModeActive;

	public Controller(Context ctx)
	{
		this.ctx = ctx;

		items = new SparseArray<Lesson>();

		loaderCenter = new LoaderCenter(ctx, this);
		viewInterfaces = new SparseArray<ViewInterface>();
		dateInterface = null;
		editInterface = null;

		globalDate = Calendar.getInstance();
		globalDate.setFirstDayOfWeek(Calendar.MONDAY);

		//Init fragment stack
		visibleFragmentTag = "";
		backStack = new Stack<String>();
		tags = ctx.getResources().getStringArray(R.array.drawer_strings);

		//Init action mode
		actionMode = null;
		isActionModeActive = false;

		isActivityProgressRunning = false;

		isActivityRunning = true;

		id = 0;
	}

	@Override
	public void setActivity(ActivityInterface activity)
	{
		this.activity = activity;
	}

	@Override
	public void showFragment(String name, String arg1)
	{
		/**argument can have different meanings, so it is arg
		 *@arg1 = param/type/group
		 *@arg2 = name/teacher
		 *@arg3 = name in schedule objects*/

		Log.d("Shika", "showFragment with " + name + " : " + arg1);

		if(isActivityProgressRunning)
			dismissActivityProgress();

		if(isActionModeActive)
			actionMode.finish();
		if(visibleFragmentTag.equals(name))
			return;

		String title = arg1;

		FragmentManager fm = activity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment;

		if(visibleFragmentTag.endsWith("ViewGroup") || visibleFragmentTag.endsWith("Chooser"))
		{
			ft.remove(fm.findFragmentByTag(visibleFragmentTag));
		}
		else
		{
			if(!visibleFragmentTag.equals(""))
				ft.detach(fm.findFragmentByTag(visibleFragmentTag));
		}

		backStack.push(visibleFragmentTag);

		fragment = fm.findFragmentByTag(name);

		//To avoid date change
		Date date = new Date(globalDate.getTimeInMillis());
		if (fragment == null)
		{
			//If it's a ViewGroup in Groups/Teachers/Courses
			if(name.endsWith("ViewGroup"))
			{
				String type = name.replace("ViewGroup", "");

				if(type.equals("Courses"))
					fragment = ScheduleViewGroupFragment.newInstance(null, null, arg1, date);
				else
				if(type.equals("Groups"))
					fragment = ScheduleViewGroupFragment.newInstance(arg1, null, null, date);
				else
					fragment = ScheduleViewGroupFragment.newInstance(null, arg1, null, date);
			}
			else
			//If it is a Chooser list
			if(name.endsWith("Chooser"))
			{
				fragment = ListFragment.newEditInstance(name, arg1);
			}
			else
			//If it is a list of Groups/Teachers/Courses in Edit Fragment
			if(name.endsWith("Edit"))
			{
				String type = name.replace("Edit", "");

				fragment = ListFragment.newInstance(type, true);

				title = type;
			}
			else
			{
				//Or main categories
				//We have changed category, clear back stack
				backStack.clear();
				EditFragment.wasInEditMode = false;

				if (name.equals(tags[0]))
				{
					fragment = ScheduleViewGroupFragment.newInstance(null, null, null, date);
				}
				else if (name.equals(tags[tags.length - 1]))
				{
					fragment = new EditFragment();
					editInterface = (EditFragment) fragment;
				}
				else
				{
					fragment = ListFragment.newInstance(name, false);
				}
			}

			ft.add(R.id.main_container, fragment, name);
			Log.d("Shika", "New Fragment made");
		}

		ft.attach(fragment);
		ft.commit();

		visibleFragmentTag = name;

		activity.setTitle(title);
	}

	@Override
	public void listItemSelected(String type, String item)
	{
		showFragment(type + "ViewGroup", item);
	}

	@Override
	public void listItemInEditSelected(String type, String item)
	{
		if(type.equals(tags[3]))
		{
			DialogAddFragment fragment = DialogAddFragment.newInstance(item, activity.getActionTitle());
			fragment.show(activity.getSupportFragmentManager().beginTransaction(), "dialog");
			return;
		}

		String tag = type + "Chooser";
		showFragment(tag, item);
	}

	@Override
	public void editTypeSelected(String tag)
	{
		showFragment(tag+"Edit", tag);
	}

	public LoaderCenterInterface getLoader()
	{
		return loaderCenter;
	}

	@Override
	public void backPressed()
	{
		if(visibleFragmentTag.equals(tags[tags.length - 1]) && !isActionModeActive)
		{
			if(EditFragment.wasInEditMode)
			{
				editInterface.backPressed();
				activity.showFunctionButton();
				return;
			}
		}

		if(!backStack.empty())
		{
			dismissActivityProgress();

			String prevFragment = backStack.pop();
			FragmentManager fm = activity.getSupportFragmentManager();

			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(fm.findFragmentByTag(visibleFragmentTag));
			ft.attach(fm.findFragmentByTag(prevFragment));
			ft.commit();

			visibleFragmentTag = prevFragment;

			if(visibleFragmentTag.equals(tags[4]))
			{
				activity.showFunctionButton();
			}

			if(!visibleFragmentTag.startsWith("Edit")) activity.setTitle(visibleFragmentTag.replace("Edit", ""));
			else activity.setTitle(visibleFragmentTag);
		}
		else
			activity.backPressed();
	}

	@Override
	public void addClick()
	{
		editInterface.addClick();

		activity.dismissFunctionButton();
		MainActivity.isFunctionButtonVisible = false;
	}

	//returns id
	@Override
	public int register(ViewInterface i)
	{
		if(indexOfValue(i) < 0)
		{
			while (viewInterfaces.indexOfKey(id) >= 0)
			{
				id++;
				if (id >= LoaderCenter.NETWORK) id = 0;
			}

			viewInterfaces.put(id, i);
		}

		int size = viewInterfaces.size();
		for(int it = 0; it < size; it++)
			if(viewInterfaces.valueAt(it).visible())
			{
				visibleId = viewInterfaces.keyAt(it);
				break;
			}

		Log.d("Shika", "Registered id = " + id + " visible = " + visibleId);
		return id;
	}

	@Override
	public void unregister(ViewInterface i)
	{
		if(indexOfValue(i) >= 0)
		{
			viewInterfaces.put(id, i);
		}

		int size = viewInterfaces.size();
		for(int it = 0; it < size; it++)
			if(viewInterfaces.valueAt(it).visible())
			{
				Log.d("Shika", "Visible is " + visibleId);
				visibleId = viewInterfaces.keyAt(it);
				break;
			}
	}

	public void notifyDateChanged()
	{
		if(dateInterface != null)
			dateInterface.notifyDateChanged();

		activity.notifyDateChanged();
	}

	@Override
	public ViewInterface getInterface(int id)
	{
		return viewInterfaces.get(id);
	}

	@Override
	public void setDateInterface(DateInterface i)
	{
		dateInterface = i;
	}

	@Override
	public Calendar getDate()
	{
		return globalDate;
	}

	@Override
	public void dateChanged(Date date)
	{
		Log.d("Shika", "date changed: current date was " + globalDate.getTime() + " new is " + date);
		globalDate.setTime(date);
		globalDate.setFirstDayOfWeek(Calendar.MONDAY);
		notifyDateChanged();
	}

	@Override
	public void load(ViewInterface i, String arg1, String arg2, String arg3)
	{
		/**argument can have different meanings, so it is arg
		 *@arg1 = param/type/group
		 *@arg2 = name/teacher
		 *@arg3 = name in schedule objects*/

		//search for id and load the interface
		int index = indexOfValue(i);
		if(index < 0)
		{
			register(i);
			index = indexOfValue(i);
		}

		int id = viewInterfaces.keyAt(index);

		Lesson temp;
		if(visibleFragmentTag.equals(tags[0]))//it is personal schedule
			temp = new Lesson(arg1, arg2, arg3, getDate());
		else
			temp = new Lesson(arg1, arg2, arg3, null);

		items.append(id, temp);

		loaderCenter.load(id);

		showActivityProgress();
	}

	@Override
	public void updateIsRunning(int id)
	{
		if(isActivityProgressRunning || visibleId == id)
			activity.dismissProgress();
	}

	@Override
	public void showError(String msg)
	{
		//Activity shows error
		activity.showError(msg);
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
	{
		this.actionMode = actionMode;

		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.edit_actionmode, menu);

		if(visibleFragmentTag.equals(tags[tags.length - 1]))
		{
			editInterface.showCheckboxes(true);
		}

		actionMode.setTitle("Delete items");

		isActionModeActive = true;

		activity.dismissFunctionButton();
		MainActivity.isFunctionButtonVisible = false;

		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
	{
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
	{
		this.actionMode = actionMode;
		switch (menuItem.getItemId())
		{
			case R.id.delete:
				deleteItems();
				return true;

			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode)
	{
		if(visibleFragmentTag.equals(tags[tags.length - 1]))
		{
			editInterface.showCheckboxes(false);
		}

		isActionModeActive = false;
		this.actionMode = null;
		activity.showFunctionButton();
	}

	public void deleteItems()
	{
		String[] itemsToDelete = editInterface.getChecked();

		//Show deletion fragment
		DialogFragment fragment = DialogCallbackFragment.newInstance(ctx.getString(R.string.dialog_delete_title),
																		ctx.getString(R.string.dialog_delete_text),
			itemsToDelete);
		fragment.show(activity.getSupportFragmentManager(), "DialogDelete");
	}

	@Override
	public void dialogDone(Dialog dialogs)
	{
		switch (dialogs)
		{
			case DIALOG_ADD:
				//use loader method update in progress to update the list
				loaderCenter.updateIsRunning(-1, visibleId);

				//show message
				showError("Course added to your schedule");
				return;

			case DIALOG_REMOVE:

				actionMode.finish();
				loaderCenter.updateIsRunning(-1, visibleId);
				break;
		}
	}

	private void showActivityProgress()
	{
		if(isActivityProgressRunning)
			return;

		isActivityProgressRunning = true;
		activity.showProgress();
	}

	private void dismissActivityProgress()
	{
		if(!isActivityProgressRunning)
			return;

		isActivityProgressRunning = false;
		activity.dismissProgress();
	}

	private int indexOfValue(Object o)
	{
		int size = viewInterfaces.size();
		//Check, whether the array contains value
		for(int it = 0; it < size; it++)
			if(viewInterfaces.valueAt(it).equals(o))
			{
				return it;
			}

		return -1;
	}
}
