package ru.shika;

import com.parse.Parse;
import ru.shika.app.Controller;
import ru.shika.app.DBHelper;

public class Application extends android.app.Application {
    private static Application instance;
    private static Controller controller;

    public static Application getInstance() {
        if (instance == null) instance = new Application();

        return instance;
    }

    public static Controller getController() {
        if (controller == null) controller = new Controller(instance);

        return controller;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "eR4X3CWg0H0dQiykPaWPymOLuceIj7XlCWu3SLLi", "tZ8L3pIHV1nXUmXj5GASyM2JdbwKFHUDYDuqhKR7");

        instance = this;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        DBHelper.getInstance(this).close();
    }
}
