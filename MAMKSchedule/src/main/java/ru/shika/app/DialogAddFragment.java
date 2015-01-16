package ru.shika.app;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ru.shika.mamkschedule.mamkschedule.R;

public class DialogAddFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener
{
	private Interfaces.dialogCallback callback;

	private Button yes, no;
	private DBHelper dbh;

	private String name, item;

	public static DialogAddFragment newInstance(String name, String item)
	{
		DialogAddFragment fragment = new DialogAddFragment();

		Bundle args = new Bundle();
		args.putString("name", name);
		args.putString("item", item);
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
		dbh = getDBH();

		name = getArguments().getString("name");
		item = getArguments().getString("item");
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

	//For working with activity dbh
	private DBHelper getDBH()
	{
		return ((MainActivity) getActivity()).getDBHelper();
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.dialog_yes:
				ContentValues cv = new ContentValues();
				cv.put("isEnrolled", 1);

				String where = "(courseId = ? or (name = ? and courseId = ''))";
				String[] args = new String[] {name, name, item, item};
				if(!item.equals("Courses"))
					where += " and (groups = ? or teacher = ?)";
				int res = dbh.update("Courses", cv, where, args);

				Log.d("Shika", where + " " + res);

				callback.dialogDone(MainActivity.Dialogs.DIALOG_ADD);

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
