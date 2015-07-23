package ru.shika.app.interfaces;

import java.util.Calendar;

public interface ControllerInterface {
    void setActivity(ActivityInterface activity);

    void activityDestroyed();

    void showFragment(String name, String arg1);

    void listItemSelected(String type, String item);

    void listEditItemSelected(String type, String item);

    void listChooserTypeSelected(String type);

    void backPressed();

    void addClick();

    String register(ViewInterface i);

    void unregister(String key);

    ViewInterface getView(String id);

    void setDateInterface(DateInterface i);

    Calendar getDate();

    void dateChanged(long time);

    void load(String key, String arg1, String arg2, String arg3);

    void localLoad(String key, String arg1, String arg2, String arg3);

    void loadEnded(String id);

    void showError(String msg);
}
