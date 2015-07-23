package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.NetworkLoaderInterface;

public class EmptyNetworkLoader extends NetworkLoader {
    public EmptyNetworkLoader(String id, Context ctx, NetworkLoaderInterface callback) {
        super(id, ctx, callback, LoaderCode.EMPTY);
    }

    @Override
    protected void prepareDownload() {

    }

    @Override
    protected void download() {
        callback.updateIsRunning(id, LoaderCenter.FOUND_NOTHING);
    }

    @Override
    protected void postDownload() {
        callback.downloadEnd(id, LoaderCenter.SUCCESS);
    }
}
