package ru.shika.app.interfaces;

import android.support.v4.app.FragmentManager;

public interface ActivityInterface
{
	void showError(String msg);

	void showProgress();
	void dismissProgress();

	void showFunctionButton();
	void dismissFunctionButton();

	void backPressed();

	FragmentManager getSupportFragmentManager();
	void setTitle(String title);
	String getActionTitle();

	void notifyDateChanged();
}
