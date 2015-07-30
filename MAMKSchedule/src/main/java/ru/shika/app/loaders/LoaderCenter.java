package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.ControllerInterface;
import ru.shika.app.interfaces.LoaderCenterInterface;
import ru.shika.app.interfaces.LocalLoaderInterface;
import ru.shika.app.interfaces.NetworkLoaderInterface;

import java.util.ArrayList;
import java.util.HashMap;

public class LoaderCenter implements NetworkLoaderInterface, LocalLoaderInterface, LoaderCenterInterface {
    /*If nothing has been downloaded*/
    final public static int FOUND_NOTHING = 0;
    /*One piece of downloaded contains ONE_PART items*/
    final public static int ONE_PART = 50;
    /*Means that download successfully ended*/
    final public static int SUCCESS = -1;
    /*Error code*/
    final public static int ERROR = -2;
    /*Chooser loader code*/
    final public static int LOADER_CHECK = -3;
    /*For network thread id*/
    final public static String NETWORK = "Net";

    private ArrayList<String> activeItems;
    private HashMap<String, Object> storage;
    private LoaderFabric fabric;

    private ControllerInterface controllerInterface;

    public LoaderCenter(Context ctx, ControllerInterface callback) {
        activeItems = new ArrayList<String>();
        storage = new HashMap<String, Object>();
        fabric = new LoaderFabric(ctx);

        controllerInterface = callback;
    }

    public void load(String id) {
        //Start local loader
        startLocalLoader(id, -1);

        //and network to update data
        NetworkLoader nl = fabric.createNetworkLoader(NETWORK + id, this);
        activeItems.add(NETWORK + id);
        if (nl != null) new Thread(nl).start();
    }

    public void load(String id, boolean isChecksUpdate) {
        if (!isChecksUpdate) {
            load(id);
            return;
        }

        startLocalLoader(id, LOADER_CHECK);

        NetworkLoader nl = fabric.createEmptyLoader(id, this); //If we have checked something, we don't have to download anything
        activeItems.add(NETWORK + id);
        if (nl != null) new Thread(nl).start();
    }

    @Override
    public void showError(String msg) {
        controllerInterface.showError(msg);
    }

    @Override
    public void updateIsRunning(String id, int amount) {
        //Log.d("Shika", "Update with id: " + id);
        if (id.startsWith(NETWORK)) id = id.replace(NETWORK, "");

        if (amount == FOUND_NOTHING) {
            //Log.d("Shika", "Nothing found with id: " + id);
            return;
        }

        //We have downloaded something
        startLocalLoader(id, amount);
    }

    public void startLocalLoader(String id, int amount) {
        LocalLoader ll = fabric.createLocalLoader(id, this, amount);
        activeItems.add(id);
        new Thread(ll).start();
    }

    @Override
    public void downloadEnd(String id, int code) {
        //Log.d("Shika", "Download end with id: " + id);
        //Delete thread from active ids
        if (activeItems.contains(id)) {
            int size = activeItems.size();
            for (int i = 0; i < size; i++) {
                if (activeItems.get(i).equals(id)) {
                    activeItems.remove(i);
                    break;
                }
            }
        }

        if (id.startsWith(NETWORK)) id = id.replace(NETWORK, "");

        if (controllerInterface.getView(id) != null) //It can be removed then
            controllerInterface.getView(id).downloadEnd();
    }

    @Override
    public void receiveData(String id, LoaderCode code, Object o) {
        //Log.d("Shika", "Loading from database ended with id: " + id);
        if (id.startsWith(NETWORK)) {
            showError("Internal error: too many threads");
            return;
        }

        if (activeItems.contains(id)) {
            int size = activeItems.size();
            for (int i = 0; i < size; i++) {
                if (activeItems.get(i).equals(id)) {
                    activeItems.remove(i);
                    break;
                }
            }
        }

        if (code == LoaderCode.CHECK) ((ArrayList<Object>) o).add("Check"); //To let view know that we have here checkFragment

        //Place object to the storage
        storage.put(id, o);

        //notify View and controller
        controllerInterface.loadEnded(id);

        if (controllerInterface.getView(id) != null) //It can be removed here
            controllerInterface.getView(id).updateIsRunning();
    }

    @Override
    public Object getData(String id) {
        return storage.get(id);
    }

    public void ready(String id) {
        storage.remove(id);
    }
}
