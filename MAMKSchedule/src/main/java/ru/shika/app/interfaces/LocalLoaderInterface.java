package ru.shika.app.interfaces;

import ru.shika.app.loaders.LoaderCode;

public interface LocalLoaderInterface
{
	void receiveData(String id, LoaderCode code, Object o1);
	void showError(String msg);
}
