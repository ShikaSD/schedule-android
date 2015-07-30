package ru.shika.app;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import ru.shika.app.fragments.*;
import ru.shika.app.interfaces.*;
import ru.shika.app.loaders.LoaderCenter;

import java.util.*;

public class Controller implements ControllerInterface, DialogCallback, ActionMode.Callback {
    public enum Dialog {
        DIALOG_ADD, DIALOG_REMOVE
    }

    //For loader fabric
    public static Map<String, Lesson> items;

    //Basic things and some interfaces
    private LoaderCenter loaderCenter;
    private Map<String, ViewInterface> viewInterfaces;
    private DateInterface dateInterface;
    private EditInterface editInterface;
    private ActivityInterface activity;
    private Context ctx;

    //Chosen date
    private Calendar globalDate;

    public static boolean isActivityRunning;

    private boolean isActivityProgressRunning;

    //For fragment changes
    private String visibleFragmentTag;
    private String visibleFragmentId;
    private Stack<String> backStack;

    private String[] tags;

    //Action mode
    private ActionMode actionMode;
    private boolean isActionModeActive;

    public Controller(Context ctx) {
        this.ctx = ctx;

        items = new Hashtable<String, Lesson>();

        loaderCenter = new LoaderCenter(ctx, this);
        viewInterfaces = new HashMap<String, ViewInterface>();
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
    }

    @Override
    public void setActivity(ActivityInterface activity) {
        this.activity = activity;
    }

    @Override
    public void activityDestroyed() {
        visibleFragmentTag = "";
        visibleFragmentId = "";
        backStack.clear();
        globalDate = Calendar.getInstance();

        viewInterfaces.clear();
        items.clear();

        isActivityRunning = false;
        isActivityProgressRunning = false;
    }

    @Override
    public void showFragment(String name, String arg1) {
        /**argument can have different meanings, so it is arg
         *@arg1 = param/type/group
         *@arg2 = name/teacher
         *@arg3 = name in schedule objects*/

        //Log.d("Shika", "Controller: showFragment with " + name + " : " + arg1 + " last visible was " + visibleFragmentTag);

        if (isActivityProgressRunning) dismissActivityProgress();

        if (isActionModeActive) actionMode.finish();
        if (visibleFragmentTag.equals(name)) return;

        String title = arg1;

        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;

        if (visibleFragmentTag.endsWith("ViewGroup") || visibleFragmentTag.endsWith("Chooser")) {
            ft.remove(fm.findFragmentByTag(visibleFragmentTag));
        } else {
            if (!visibleFragmentTag.equals("")) ft.detach(fm.findFragmentByTag(visibleFragmentTag));
        }

        backStack.push(visibleFragmentTag);

        fragment = fm.findFragmentByTag(name);

        for (String tag : tags)
            if (tag.equals(name)) {
                //We have changed category, clear back stack
                backStack.clear();
                break;
            }

        //To avoid date change
        Date date = new Date(globalDate.getTimeInMillis());
        if (fragment == null) {
            //If it's a ViewGroup in Groups/Teachers/Courses
            if (name.endsWith("ViewGroup")) {
                String type = name.replace("ViewGroup", "");

                if (type.equals("Courses")) fragment = ScheduleViewGroupFragment.newInstance(null, null, arg1, date);
                else if (type.equals("Groups")) fragment = ScheduleViewGroupFragment.newInstance(arg1, null, null, date);
                else fragment = ScheduleViewGroupFragment.newInstance(null, arg1, null, date);
            } else
                //If it is a Chooser list
                if (name.endsWith("Chooser")) {
                    fragment = ListFragment.newEditInstance(name, arg1);
                } else
                    //If it is a list of Groups/Teachers/Courses in Edit Fragment
                    if (name.endsWith("Edit")) {
                        String type = name.replace("Edit", "");

                        fragment = ListFragment.newInstance(type, true);

                        title = type;
                    } else {
                        //Or main categories
                        EditFragment.wasInEditMode = false;

                        if (name.equals(tags[0])) {
                            fragment = ScheduleViewGroupFragment.newInstance(null, null, null, date);
                        } else if (name.equals(tags[tags.length - 1])) {
                            fragment = new EditFragment();
                            editInterface = (EditFragment) fragment;

                            if (EditFragment.wasInEditMode) {
                                MainActivity.isFunctionButtonVisible = false;
                            }
                        } else {
                            fragment = ListFragment.newInstance(name, false);
                        }
                    }

            ft.add(R.id.main_container, fragment, name);
            //Log.d("Shika", "Controller: New Fragment made");
        }

        ft.attach(fragment);
        ft.commit();

        visibleFragmentTag = name;
        visibleFragmentId = name + arg1;

        activity.setTitle(title);
    }

    @Override
    public void listItemSelected(String type, String item) {
        showFragment(type + "ViewGroup", item);
    }

    @Override
    public void listEditItemSelected(String type, String item) {
        if (type.equals(tags[3])) {
            DialogAddFragment fragment = DialogAddFragment.newInstance(item, activity.getActionTitle());
            fragment.show(activity.getSupportFragmentManager().beginTransaction(), "dialog");
            return;
        }

        String tag = type + "Chooser";
        showFragment(tag, item);
    }

    @Override
    public void listChooserTypeSelected(String tag) {
        showFragment(tag + "Edit", tag);
    }

    public LoaderCenterInterface getLoader() {
        return loaderCenter;
    }

    @Override
    public void backPressed() {
        if (visibleFragmentTag.equals(tags[tags.length - 1]) && !isActionModeActive) {
            if (EditFragment.wasInEditMode) {
                editInterface.backPressed();
                activity.showFunctionButton();
                return;
            }
        }

        if (!backStack.empty()) {
            dismissActivityProgress();

            String prevFragment = backStack.pop();
            FragmentManager fm = activity.getSupportFragmentManager();

            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fm.findFragmentByTag(visibleFragmentTag));
            ft.attach(fm.findFragmentByTag(prevFragment));
            ft.commit();

            visibleFragmentTag = prevFragment;

            if (visibleFragmentTag.equals(tags[tags.length - 1]) && !EditFragment.wasInEditMode) {
                activity.showFunctionButton();
            }

            if (!visibleFragmentTag.startsWith("Edit")) activity.setTitle(visibleFragmentTag.replace("Edit", ""));
            else activity.setTitle(visibleFragmentTag);
        } else activity.backPressed();
    }

    @Override
    public void addClick() {
        editInterface.addClick();

        activity.dismissFunctionButton();
        MainActivity.isFunctionButtonVisible = false;
    }

    //returns id
    @Override
    public String register(ViewInterface i) {
        if (!viewInterfaces.containsKey(visibleFragmentId)) viewInterfaces.put(visibleFragmentId, i);

        //Log.d("Shika", "Controller: registered id = " + visibleFragmentId);
        return visibleFragmentId;
    }

    @Override
    public void unregister(String key) {
        if (viewInterfaces.containsKey(key)) viewInterfaces.remove(key);
    }

    public void notifyDateChanged() {
        if (dateInterface != null) dateInterface.notifyDateChanged();

        activity.notifyDateChanged();
    }

    @Override
    public ViewInterface getView(String id) {
        return viewInterfaces.get(id);
    }

    @Override
    public void setDateInterface(DateInterface i) {
        dateInterface = i;
    }

    @Override
    public Calendar getDate() {
        return globalDate;
    }

    @Override
    public void dateChanged(long time) {
        //Log.d("Shika", "Controller: date changed: date was " + globalDate.getTime() + " new is " + new Date(time));
        globalDate.setTimeInMillis(time);
        globalDate.setFirstDayOfWeek(Calendar.MONDAY);
        notifyDateChanged();
    }

    @Override
    public void load(String key, String arg1, String arg2, String arg3) {
        /**argument can have different meanings, so it is arg
         *@arg1 = param/type/group
         *@arg2 = name/teacher
         *@arg3 = name in schedule objects*/

        //Log.d("Shika", "Controller: start loading with id: " + key + ", and args: " + arg1 + ", " + arg2 + ", " + arg3);

        Lesson temp;
        if (arg1.contains("ViewGroup"))//it is schedule
        {
            arg1 = arg1.replace("ViewGroup", "");
            if (arg1.equals("") || arg1.equals("null")) arg1 = null; //If it was empty, make it null

            temp = new Lesson(arg1, arg2, arg3, getDate());
        } else temp = new Lesson(arg1, arg2, arg3, null);

        items.put(key, temp);

        loaderCenter.load(key);

        showActivityProgress();
    }

    @Override
    public void localLoad(String key, String arg1, String arg2, String arg3) {
        Lesson temp;
        if (arg1.contains("ViewGroup"))//it is schedule
        {
            arg1 = arg1.replace("ViewGroup", "");
            if (arg1.equals("") && arg1.equals("null")) arg1 = null; //If it was empty, make it null

            temp = new Lesson(arg1, arg2, arg3, getDate());
        } else temp = new Lesson(arg1, arg2, arg3, null);

        items.put(key, temp);

        loaderCenter.startLocalLoader(key, -1);
    }

    @Override
    public void loadEnded(String id) {
        if (visibleFragmentId.equals(id)) activity.dismissProgress();
    }

    @Override
    public void showError(String msg) {
        //Activity shows error
        activity.showError(msg);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        this.actionMode = actionMode;

        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.edit_actionmode, menu);

        if (visibleFragmentTag.equals(tags[tags.length - 1])) {
            editInterface.showCheckboxes(true);
        }

        actionMode.setTitle("Delete items");

        isActionModeActive = true;

        activity.dismissFunctionButton();
        MainActivity.isFunctionButtonVisible = false;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        this.actionMode = actionMode;
        switch (menuItem.getItemId()) {
            case R.id.delete:
                deleteItems();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

        if (visibleFragmentTag.equals(tags[tags.length - 1])) {
            editInterface.showCheckboxes(false);
        }

        isActionModeActive = false;
        this.actionMode = null;
        activity.showFunctionButton();
    }

    private void deleteItems() {
        String[] itemsToDelete = editInterface.getChecked();

        //Show deletion fragment
        DialogFragment fragment = DialogCallbackFragment.newInstance(ctx.getString(R.string.dialog_delete_title), ctx.getString(R.string.dialog_delete_text), itemsToDelete);
        fragment.show(activity.getSupportFragmentManager(), "DialogDelete");
    }

    @Override
    public void dialogDone(Dialog dialogs) {
        switch (dialogs) {
            case DIALOG_ADD:
                //we just need to update the database
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loaderCenter.load(visibleFragmentId, true);

                        //show message
                        showError("Course added to your schedule");
                    }
                });

                return;

            case DIALOG_REMOVE:

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        actionMode.finish();
                        loaderCenter.load(visibleFragmentId, true);
                    }
                });
                break;
        }
    }

    private void showActivityProgress() {
        if (isActivityProgressRunning) return;

        isActivityProgressRunning = true;
        activity.showProgress();
    }

    private void dismissActivityProgress() {
        if (!isActivityProgressRunning) return;

        isActivityProgressRunning = false;
        activity.dismissProgress();
    }
}
