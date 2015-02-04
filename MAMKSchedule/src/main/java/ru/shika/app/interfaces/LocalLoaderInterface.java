package ru.shika.app.interfaces;

import ru.shika.app.loaders.LoaderCode;

public interface LocalLoaderInterface
{
	void receiveData(int id, LoaderCode code, Object o1);
	void showError(String msg);
}
