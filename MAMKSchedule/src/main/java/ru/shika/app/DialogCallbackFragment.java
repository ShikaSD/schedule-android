package ru.shika.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ru.shika.mamkschedule.mamkschedule.R;

public class DialogCallbackFragment extends android.support.v4.app.DialogFragment implements View.OnClickListener
{
	private Interfaces.dialogCallback callback;

	private Button yes, no;

	private String title, text;

	public static DialogCallbackFragment newInstance(String title, String text)
	{
		DialogCallbackFragment fragment = new DialogCallbackFragment();

		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("text", text);
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

		title = getArguments().getString("title");
		text = getArguments().getString("text");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_dialog, container, false);

		yes = (Button) rootView.findViewById(R.id.dialog_yes);
		no = (Button) rootView.findViewById(R.id.dialog_no);

		yes.setOnClickListener(this);
		no.setOnClickListener(this);

		((TextView) rootView.findViewById(R.id.dialog_header)).setText(title);
		((TextView) rootView.findViewById(R.id.dialog_text)).setText(text);

		return rootView;
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.dialog_yes:
				callback.dialogDone(MainActivity.dialogs.DIALOG_REMOVE);
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
