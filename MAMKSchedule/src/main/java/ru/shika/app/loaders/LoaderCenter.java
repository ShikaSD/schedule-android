package ru.shika.app.loaders;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.SparseArray;
import ru.shika.app.interfaces.ControllerInterface;
import ru.shika.app.interfaces.LoaderCenterInterface;
import ru.shika.app.interfaces.LocalLoaderInterface;
import ru.shika.app.interfaces.NetworkLoaderInterface;
import ru.shika.app.R;

import java.util.ArrayList;

public class LoaderCenter implements NetworkLoaderInterface, LocalLoaderInterface, LoaderCenterInterface
{
	/*If nothing has been downloaded*/
	final public static int FOUND_NOTHING = 0;
	/*One piece of downloaded contains ONE_PART items*/
	final public static int ONE_PART = 50;
	/*Means that download successfully ended*/
	final public static int END_OF_DOWNLOAD = -1;
	/*Error code*/
	final public static int ERROR = -2;
	/*If no courses found that value returns*/
	final public static int NO_COURSES = -3;
	/*For network thread id*/
	final public static int NETWORK = 1000000;

	private ArrayList<Integer> activeItems;
	private SparseArray<Object> storage;
	private LoaderFabric fabric;
	private Resources resources;

	private ControllerInterface controllerInterface;

	public LoaderCenter(Context ctx, ControllerInterface callback)
	{
		activeItems = new ArrayList<Integer>();
		storage = new SparseArray<Object>();
		fabric = new LoaderFabric(ctx);

		controllerInterface = callback;
		resources = ctx.getResources();
	}

	public void load(int id)
	{
		//Start local loader
		LocalLoader ll = fabric.createLocalLoader(this, id, -1);
		activeItems.add(id);
		new Thread(ll).start();

		//and network to update data
		NetworkLoader nl = fabric.createNetworkLoader(this, NETWORK + id);
		activeItems.add(NETWORK + id);
		if(nl != null) new Thread(nl).start();
	}

	@Override
	public void showError(String msg)
	{
		controllerInterface.showError(msg);
	}

	@Override
	public void updateIsRunning(int amount, int id)
	{
		if(id >= NETWORK)
			id -= NETWORK;

		if(amount == FOUND_NOTHING)
			return;

		//We have downloaded something
		LocalLoader ll = fabric.createLocalLoader(this, id, amount);
		activeItems.add(id);
		new Thread(ll).start();
	}

	@Override
	public void downloadEnd(int code, int id)
	{
		if(code == NO_COURSES)
			showError(resources.getString(R.string.no_courses));
		//Delete thread from active ids
		if(activeItems.contains(id))
		{
			int size = activeItems.size();
			for(int i = 0; i < size; i++)
			{
				if(activeItems.get(i) == id)
				{
					activeItems.remove(i);
					break;
				}
			}
		}

		if(id >= NETWORK)
			id -= NETWORK;

		controllerInterface.getInterface(id).downloadEnd();
	}

	@Override
	public void receiveData(int id, LoaderCode code, Object o1)
	{
		//Log.d("Shika", "update ended with id = " + id);
		if(id > NETWORK)
		{
			showError("Internal error: too many threads");
			return;
		}

		if(activeItems.contains(id))
		{
			int size = activeItems.size();
			for(int i = 0; i < size; i++)
			{
				if(activeItems.get(i) == id)
				{
					activeItems.remove(i);
					break;
				}
			}
		}

		//Place object to the storage
		storage.append(id, o1);

		//Log.d("Shika", "Sending signal to fragment");
		//notify View
		controllerInterface.updateIsRunning(id);
		controllerInterface.getInterface(id).updateIsRunning();

		//Update link to the object to let it be collected by garbage collector
		o1 = null;
	}

	@Override
	public Object getData(int id)
	{
		return storage.get(id);
	}

	public void ready(int id)
	{
		storage.remove(id);
	}
}
