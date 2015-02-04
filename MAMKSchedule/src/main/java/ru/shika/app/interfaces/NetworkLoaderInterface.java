package ru.shika.app.interfaces;

import ru.shika.app.loaders.LoaderCode;

public interface NetworkLoaderInterface
{
	void showError(String msg);

	void updateIsRunning(int amount, int id);
	void downloadEnd(int code, int id);
}