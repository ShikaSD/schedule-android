package ru.shika.app.interfaces;

import android.app.Activity;

import java.util.Calendar;
import java.util.Date;

public interface ControllerInterface
{
	void setActivity(ActivityInterface activity);

	void showFragment(String name, String arg1);
	void listItemSelected(String type, String item);
	void listItemInEditSelected(String type, String item);
	void editTypeSelected(String type);

	void backPressed();

	void addClick();

	int register(ViewInterface i);
	void unregister(ViewInterface i);
	ViewInterface getInterface(int id);
	void setDateInterface(DateInterface i);

	Calendar getDate();
	void dateChanged(Date date);

	void load(ViewInterface i, String arg1, String arg2, String arg3);
	void updateIsRunning(int id);

	void showError(String msg);
}
