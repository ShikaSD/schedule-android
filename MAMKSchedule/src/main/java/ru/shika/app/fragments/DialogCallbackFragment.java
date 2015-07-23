package ru.shika.app.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ru.shika.Application;
import ru.shika.app.Controller;
import ru.shika.app.DBHelper;
import ru.shika.app.R;
import ru.shika.app.interfaces.DialogCallback;

public class DialogCallbackFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener {
    private DialogCallback callback;

    private DBHelper dbh;

    private String title, text;
    private String[] items;

    public static DialogCallbackFragment newInstance(String title, String text, String[] items) {
        DialogCallbackFragment fragment = new DialogCallbackFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("text", text);
        args.putStringArray("items", items);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Dialog);

        title = getArguments().getString("title");
        text = getArguments().getString("text");
        items = getArguments().getStringArray("items");

        dbh = DBHelper.getInstance(getActivity());
        callback = Application.getController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog, container, false);

        Button yes = (Button) rootView.findViewById(R.id.dialog_yes);
        Button no = (Button) rootView.findViewById(R.id.dialog_no);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        ((TextView) rootView.findViewById(R.id.dialog_header)).setText(title);
        ((TextView) rootView.findViewById(R.id.dialog_text)).setText(text);

        return rootView;
    }

    private void update(final ContentValues cv) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String item : items)
                    dbh.update("Courses", cv, "name = ? or courseId = ?", new String[]{item, item});

                callback.dialogDone(Controller.Dialog.DIALOG_REMOVE);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_yes:

                ContentValues cv = new ContentValues();
                cv.put("isEnrolled", 0);

                update(cv);

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
