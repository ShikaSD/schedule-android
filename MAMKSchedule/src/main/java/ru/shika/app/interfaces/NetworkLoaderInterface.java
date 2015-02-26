package ru.shika.app.interfaces;

public interface NetworkLoaderInterface
{
	void showError(String msg);

	void updateIsRunning(String id, int amount);
	void downloadEnd(String id, int code);
}