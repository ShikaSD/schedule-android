package ru.shika.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import ru.shika.Application;
import ru.shika.app.R;
import ru.shika.app.adapters.EditListAdapter;
import ru.shika.app.adapters.EditSimpleAdapter;
import ru.shika.app.interfaces.ControllerInterface;
import ru.shika.app.interfaces.EditInterface;
import ru.shika.app.interfaces.LoaderCenterInterface;
import ru.shika.app.interfaces.ViewInterface;

import java.util.ArrayList;

public class EditFragment extends Fragment implements EditInterface, ViewInterface {
    private ControllerInterface controller;
    private LoaderCenterInterface loader;

    //To sort them by course id
    private SparseArray<String> keys; //Ids
    private ArrayList<ArrayList<String>> names; //Full name

    private Button add;
    private TextView empty;

    private ListView list;
    private EditSimpleAdapter editAdapter;
    private EditListAdapter listAdapter;
    private TextView header;

    private String[] strings;
    private String[] titles;

    private ActionMode actionMode;

    public static boolean wasInEditMode;

    private Animation editOpen, editClose;

    private String id;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = Application.getController();
        loader = Application.getController().getLoader();

        keys = new SparseArray<String>();
        names = new ArrayList<ArrayList<String>>();

        titles = getResources().getStringArray(R.array.drawer_strings);
        strings = getResources().getStringArray(R.array.edit_strings);

        editAdapter = new EditSimpleAdapter(getActivity(), strings);
        listAdapter = new EditListAdapter(getActivity(), keys, names, true);

        wasInEditMode = false;

        animationInit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        empty = (TextView) view.findViewById(R.id.fragment_edit_empty);
        empty.setText(getResources().getText(R.string.edit_empty));
        empty.setGravity(Gravity.CENTER);

        add = (Button) view.findViewById(R.id.fragment_edit_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddClick(view);
            }
        });

        list = (ListView) view.findViewById(R.id.fragment_edit_list);
        header = (TextView) view.findViewById(R.id.fragment_edit_list_header);

        if (wasInEditMode) {
            switchToEdit();
            header.setVisibility(View.VISIBLE);
        } else switchToNormal();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (list.getAdapter() instanceof EditListAdapter) {
                    if (listAdapter.getCheckedAmount() == 0) actionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(Application.getController());

                    listAdapter.toggle(i);

                    if (listAdapter.getCheckedAmount() == 0) actionMode.finish();

                    return;
                }

                controller.listChooserTypeSelected(titles[i + 1]);
            }
        });

        id = controller.register(this);
        load();
    }

    private void animationInit() {
        editOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.calendar_open);
        editOpen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                switchToEdit();
                header.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                header.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        editClose = AnimationUtils.loadAnimation(getActivity(), R.anim.calendar_close);
        editClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                header.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                switchToNormal();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void backPressed() {
        if (wasInEditMode) list.startAnimation(editClose);

        wasInEditMode = false;
    }


    public void onAddClick(View v) {
        controller.addClick();
    }

    @Override
    public void addClick() {
        list.startAnimation(editOpen);
        wasInEditMode = true;
    }

    @Override
    public void showCheckboxes(final boolean show) {
        if (!show) listAdapter.unCheck();
        listAdapter.showCheckboxes(show);
    }

    @Override
    public String[] getChecked() {
        return listAdapter.getChecked();
    }

    private void load() {
        controller.load(id, "Edit", null, null);
    }

    private void update(Object o) {
        if (o == null) return;

        ArrayList<Object> items = (ArrayList<Object>) o;

        names.clear();
        keys.clear();
        names.addAll((ArrayList<ArrayList<String>>) items.get(0));
        for (int i = 0; i < ((SparseArray) items.get(1)).size(); i++) {
            keys.append(((SparseArray<String>) items.get(1)).keyAt(i), ((SparseArray<String>) items.get(1)).valueAt(i));
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();

                if (!wasInEditMode) switchToNormal();
            }
        });
    }

    private void switchToEdit() {
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        list.setVisibility(View.VISIBLE);
        list.setAdapter(editAdapter);

        header.setText(R.string.edit_header);

        empty.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
    }

    private void switchToNormal() {
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        list.setAdapter(listAdapter);

        if (keys.size() > 0) {
            header.setText(R.string.edit_list_header);
            header.setVisibility(View.VISIBLE);
            list.setVisibility(View.VISIBLE);
            add.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
        } else {
            header.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void downloadEnd() {

    }

    @Override
    public void updateIsRunning() {
        update(loader.getData(id));
        loader.ready(id);
    }

    @Override
    public boolean visible() {
        return isVisible();
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void dismissProgress() {

    }
}
