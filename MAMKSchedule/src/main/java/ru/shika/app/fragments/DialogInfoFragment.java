package ru.shika.app.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ru.shika.app.BuildConfig;
import ru.shika.app.R;

public class DialogInfoFragment extends DialogFragment implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogAddFragment.STYLE_NO_FRAME, R.style.AppTheme_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_info, container, false);

        ((TextView) rootView.findViewById(R.id.dialog_text)).setText(getString(R.string.info_dialog_text) + ' ' + BuildConfig.VERSION_NAME);

        Button yes = (Button) rootView.findViewById(R.id.dialog_yes);
        yes.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_yes:
                dismiss();
                break;
        }

    }
}
