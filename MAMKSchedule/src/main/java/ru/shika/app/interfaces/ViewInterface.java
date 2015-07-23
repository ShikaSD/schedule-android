package ru.shika.app.interfaces;

public interface ViewInterface {
    void downloadEnd();

    void updateIsRunning();

    boolean visible();

    void showProgress();

    void dismissProgress();
}
