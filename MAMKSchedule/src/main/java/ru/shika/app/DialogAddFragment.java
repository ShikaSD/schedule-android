package ru.shika.app;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ru.shika.mamkschedule.mamkschedule.R;

public class DialogAddFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener
{
	Interfaces.dialogCallback callback;

	Button yes, no;
	DBHelper dbh;

	String name;

	public static DialogAddFragment newInstance(String name)
	{
		DialogAddFragment fragment = new DialogAddFragment();

		Bundle args = new Bundle();
		args.putString("name", name);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		callback = (Interfaces.dialogCallback) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setStyle(DialogAddFragment.STYLE_NO_FRAME, R.style.AppTheme_Dialog);
		dbh = new DBHelper(getActivity());

		name = getArguments().getString("name");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_dialog, container, false);

		yes = (Button) rootView.findViewById(R.id.dialog_yes);
		no = (Button) rootView.findViewById(R.id.dialog_no);

		yes.setOnClickListener(this);
		no.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.dialog_yes:
				SQLiteDatabase db = dbh.getWritableDatabase();

				ContentValues cv = new ContentValues();
				cv.put("isEnrolled", 1);
				String where = "courseId = '"+name+"' or (name = '"+name+"' and courseId = '')";
				int res = db.update("Courses", cv, where, null);

				dbh.close();

				callback.dialogDone(MainActivity.dialogs.DIALOG_ADD);
				MainActivity.showToast("Course added to your schedule");

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
