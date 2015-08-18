package ru.shika.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ru.shika.Application;
import ru.shika.android.CircleImageView;
import ru.shika.android.MaterialProgressDrawable;
import ru.shika.app.R;
import ru.shika.app.RecyclerItemClickListener;
import ru.shika.app.adapters.ListFragmentAdapter;
import ru.shika.app.interfaces.ControllerInterface;
import ru.shika.app.interfaces.LoaderCenterInterface;
import ru.shika.app.interfaces.ViewInterface;

import java.util.ArrayList;

public class ListFragment extends Fragment implements ViewInterface, SearchView.OnQueryTextListener {
    /**
     * Fragment is used three times: in drawer menu and two types in edit
     * isEditFragment = false, fragmentType = true : drawer
     * isEditFragment = true : edit (typeName = null || != null)
     */

    private ControllerInterface controller;
    private LoaderCenterInterface loader;

    //To sort them by course id
    private SparseArray<String> keys; //Ids
    private ArrayList<ArrayList<String>> names; //Full name
    private ArrayList<Boolean> visible;

    private ListFragmentAdapter adapter;
    private RecyclerView list;
    private TextView empty;

    private CircleImageView progressView;
    private MaterialProgressDrawable progressDrawable;

    private String fragmentType;
    private String typeName;
    private boolean isEditFragment;

    private TranslateAnimation appear, disappear;

    private String id;

    private String searchQuery;
    private SearchView searchView;
    private Thread searchFilterThread;

    public static Fragment newInstance(String listType, boolean isEditFragment) {
        Fragment fragment = new ListFragment();

        Bundle args = new Bundle();
        args.putString("type", listType);
        args.putBoolean("edit", isEditFragment);
        fragment.setArguments(args);

        return fragment;
    }

    public static Fragment newEditInstance(String listType, String name) {
        Fragment fragment = new ListFragment();

        Bundle args = new Bundle();
        args.putString("type", listType);
        args.putString("name", name);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //We have own menu here
        setHasOptionsMenu(true);

        controller = Application.getController();
        loader = Application.getController().getLoader();

        animationInit();

        keys = new SparseArray<String>();
        names = new ArrayList<ArrayList<String>>();
        visible = new ArrayList<Boolean>();

        fragmentType = getArguments().getString("type");
        typeName = null;
        isEditFragment = getArguments().getBoolean("edit", false);

        if (getArguments().getString("name") != null) {
            isEditFragment = true;
            typeName = getArguments().getString("name");
        }

        searchQuery = "";

        if (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser")) {
            //Log.d("Shika", "ListFragment: Checkboxes showed");
            adapter = new ListFragmentAdapter(keys, names, visible, true);
            adapter.showCheckboxes(true);
        } else adapter = new ListFragmentAdapter(keys, names, visible, false);

        id = controller.register(this);
        load();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        list = (RecyclerView) rootView.findViewById(R.id.groupsList);
        empty = (TextView) rootView.findViewById(R.id.empty);

        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {


                if (isEditFragment && (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser")) &&
                    adapter.isChecked(i)) return;

                String item = "";
                if (fragmentType.startsWith("Courses") || fragmentType.endsWith("Chooser")) {
                    item = ((TextView) view.findViewById(R.id.fragment_list_id)).getText().toString();
                }
                if(item.equals(""))
                    item = ((TextView) view.findViewById(R.id.fragment_list_name)).getText().toString();

                if (isEditFragment && typeName == null) {
                    controller.listEditItemSelected(fragmentType, item);
                } else if (typeName != null) {
                    controller.listEditItemSelected("Courses", item);
                } else controller.listItemSelected(fragmentType, item);
            }
        }));

        progressInit();

        ((ViewGroup) rootView).addView(progressView);
        progressView.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void animationInit() {
        appear = (TranslateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.progress_drawable_appear);
        appear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                progressView.setVisibility(View.VISIBLE);
                progressDrawable.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        disappear = (TranslateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.progress_drawable_disappear);
        disappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressDrawable.stop();
                progressView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void progressInit() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        int mCircleWidth = (int) (40 * metrics.density);
        int mCircleHeight = (int) (40 * metrics.density);

        progressView = new CircleImageView(getActivity(), getResources().getColor(R.color.white), 40 / 2);
        progressDrawable = new MaterialProgressDrawable(getActivity(), progressView);

        RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(mCircleWidth, mCircleHeight);
        lParams.bottomMargin = (int) getResources().getDimension(R.dimen.function_button_vertical_margin);
        lParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        progressView.setLayoutParams(lParams);

        progressDrawable.setColorSchemeColors(getResources().getColor(R.color.light_blue));
        progressView.setImageDrawable(progressDrawable);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(id.equals("CoursesCourses")) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }

        inflater.inflate(R.menu.list, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        controller.unregister(id);
    }

    private void load() {
        //Log.d("Shika", "ListFragment: Start loading with values: " + fragmentType + ", " + typeName);
        controller.load(id, fragmentType, typeName, null);
    }

    private void update(Object o) {
        //Adding some loaded objects to existing array
        if (o == null) return;

        ArrayList<Object> items = (ArrayList<Object>) o;
        ArrayList<ArrayList<String>> tempNames = (ArrayList<ArrayList<String>>) items.get(0);
        SparseArray<String> tempKeys = (SparseArray<String>) items.get(1);
        final SparseBooleanArray checks = (items.size() > 2) ? (SparseBooleanArray) items.get(2) : null;

        if (items.size() > 3) //It is checks update
        {
            updateChecks(tempKeys);
            return;
        }

        int tempSize = tempKeys.size();
        int size = keys.size();

        for (int i = 0; i < tempSize; i++) {
            boolean isAdded = false;

            for (int j = 0; j < size; j++) {
                if (tempKeys.valueAt(i).equals(keys.valueAt(j))) {
                    //If we have the same values here, then add them to the index that we know
                    int index = keys.keyAt(j);
                    int ind = tempKeys.keyAt(i);

                    int thisSize = tempNames.get(ind).size();
                    for (int z = 0; z < thisSize; z++) //We have to search for equal values in sub-arrays
                    {
                        String s = tempNames.get(ind).get(z);
                        boolean equal = false;

                        for (String p : names.get(index)) {
                            if (s.toLowerCase().replaceAll("[-.,:;\\\\/' ]+", "").toLowerCase().equals(p.toLowerCase().replaceAll("[-.,:;\\\\/' ]+", "").toLowerCase())) {
                                equal = true;
                                break;
                            }
                        }

                        if (!equal) names.get(index).add(s); //If they are not equal, add it
                    }

                    isAdded = true;
                    break;
                }
            }

            if (!isAdded)//If we haven't found the key, add new
            {
                keys.append(names.size(), tempKeys.valueAt(i));//It id now id previous size of name(last element)
                names.add(tempNames.get(tempKeys.keyAt(i)));
            }
        }

        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.swapData();

                if (checks != null) adapter.check(checks);

                if (keys.size() == 0) {
                    //Show that list is empty
                    empty.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                } else {
                    //Or not
                    empty.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                }
            }
        });

        filterItems(searchQuery);
        searchFilterThread = Thread.currentThread();
    }

    private void updateChecks(SparseArray<String> tempKeys) {
        SparseBooleanArray checks = new SparseBooleanArray();
        int tempSize = tempKeys.size();
        int size = keys.size();
        for (int i = 0; i < tempSize; i++) {
            for (int j = 0; j < size; j++) {
                if (tempKeys.valueAt(i).equals(keys.valueAt(j))) {
                    checks.append(keys.keyAt(j), true);
                    break;
                }
            }
        }

        final SparseBooleanArray temp = checks;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.check(temp);

                if (keys.size() == 0) {
                    //Show that list is empty
                    empty.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                } else {
                    //Or not
                    empty.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void filterItems(String s) {
        int size = names.size();
        visible.clear();

        for (int i = 0; i < size; i++) {
            if (Thread.interrupted()) {
                Log.d("Shika", "Filter thread is interrupted");
                for (int j = 0; j < size; j++)
                    visible.add(true);

                return;
            }

            boolean f = false;
            int tsize = names.get(i).size();
            for (int j = 0; j < tsize; j++) {
                if (names.get(i).get(j).toLowerCase().contains(s.toLowerCase())) {
                    f = true;
                    break;
                }
            }

            if (!f) if (keys.get(i).toLowerCase().contains(s.toLowerCase())) f = true;

            visible.add(f);
        }

        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void downloadEnd() {
        dismissProgress();
    }

    @Override
    public void updateIsRunning() {
        if(((ArrayList<Object>) loader.getData(id)).size() <= 3)
            showProgress();

        update(loader.getData(id));
        loader.ready(id);
    }

    @Override
    public boolean visible() {
        return isVisible();
    }

    @Override
    public void showProgress() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressView.getVisibility() == View.GONE) progressView.startAnimation(appear);
            }
        });
    }

    @Override
    public void dismissProgress() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressView.getVisibility() == View.VISIBLE) progressView.startAnimation(disappear);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String s) {
        //Log.d("Shika", "Text changed to " + s);
        searchQuery = s;
        if (searchFilterThread != null && searchFilterThread.isAlive())
            searchFilterThread.interrupt();

        searchFilterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                filterItems(s);
            }
        });
        searchFilterThread.start();
        return false;
    }
}
