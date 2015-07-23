package ru.shika.app.fragments;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ru.shika.Application;
import ru.shika.app.Controller;
import ru.shika.app.DBHelper;
import ru.shika.app.R;
import ru.shika.app.interfaces.DialogCallback;

public class DialogAddFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener {
    /**
     * Dialogs are mini-activities here
     * so I decided not to delete another parts and database queries from here
     * It will be too hard and that structure is easier.
     */
    private DialogCallback callback;

    private DBHelper dbh;

    private String name, item;

    public static DialogAddFragment newInstance(String name, String item) {
        DialogAddFragment fragment = new DialogAddFragment();

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("item", item);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogAddFragment.STYLE_NO_FRAME, R.style.AppTheme_Dialog);
        dbh = getDBH();

        name = getArguments().getString("name");
        item = getArguments().getString("item");

        callback = Application.getController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog, container, false);

        Button yes = (Button) rootView.findViewById(R.id.dialog_yes);
        Button no = (Button) rootView.findViewById(R.id.dialog_no);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        return rootView;
    }

    //For working with activity dbh
    private DBHelper getDBH() {
        return DBHelper.getInstance(getActivity());
    }

    private void addToSchedule(final String where, final String[] args, final ContentValues cv) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int res = dbh.update("Courses", cv, where, args);

                Log.d("Shika", where + " :rows changed - " + res);
                callback.dialogDone(Controller.Dialog.DIALOG_ADD);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_yes:
                ContentValues cv = new ContentValues();
                cv.put("isEnrolled", 1);

                String where = "(courseId = ? or (name = ? and courseId = ''))";
                String[] args;
                if (!item.equals("Courses")) {
                    where += " and (groups = ? or teacher = ?)";
                    args = new String[]{name, name, item, item};
                } else args = new String[]{name, name};

                addToSchedule(where, args, cv);

                dismiss();
                break;
            case R.id.dialog_no:
                dismiss();
                break;

            default:
                break;
        }
    }
}
