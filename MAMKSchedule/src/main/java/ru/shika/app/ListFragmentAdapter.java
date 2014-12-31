package ru.shika.app;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ListFragmentAdapter extends BaseAdapter
{
	private Map<String, Integer> keys;
	private ArrayList <ArrayList <String>> names;
	private Context context;
	private LayoutInflater layoutInflater;

	private int layoutId;
	public boolean isCheckingList;
	private ArrayList<Boolean> checkedItems;
	private boolean showCheckboxes;


	public ListFragmentAdapter(Context ctx, Map<String, Integer> keys, ArrayList<ArrayList<String>> names, boolean isCheckingList)
	{
		this.keys = keys;
		this.names = names;
		context = ctx;
		layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.isCheckingList = isCheckingList;
		showCheckboxes = false;
		if(isCheckingList)
		{
			layoutId = R.layout.list_checkable_item;
			checkedItems = new ArrayList<Boolean>(keys.size());
			Collections.fill(checkedItems, Boolean.FALSE);
		}
		else
			layoutId = R.layout.fragment_list_item;
	}

	@Override
	public int getCount()
	{
		return names.size();
	}

	@Override
	public Object getItem(int i)
	{
		return names.get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		View v = view;
		if (v == null) {
			v = layoutInflater.inflate(layoutId, viewGroup, false);
		}

		int size = names.get(i).size();
		String name = names.get(i).get(0);
		for(int j = 1; j < size; j++)
			name += "|"+names.get(i).get(j);

		((TextView) v.findViewById(R.id.fragment_list_name)).setText(name);
		if(keys.containsValue(i))
			for(String key : keys.keySet())
			{
				if (keys.get(key).equals(i))
				{
					if(!key.equals(name))
						((TextView) v.findViewById(R.id.fragment_list_id)).setText(key);

					break;
				}
			}

		if(isCheckingList)
		{
			if(i > checkedItems.size() - 1)
				while(checkedItems.size() - 1 < i)
					checkedItems.add(Boolean.FALSE);

			CheckBox checkBox = ((CheckBox) v.findViewById(R.id.list_checkbox));
			checkBox.setChecked(checkedItems.get(i));

			if(showCheckboxes)
			{
				checkBox.setVisibility(View.VISIBLE);
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					checkBox.setButtonDrawable(R.drawable.checkbox);
			}
			else
				checkBox.setVisibility(View.GONE);
		}

		v.clearFocus();

		return v;
	}

	public void toggle(int position)
	{
		checkedItems.set(position, !checkedItems.get(position));
		notifyDataSetChanged();
	}

	public void check(SparseArray<Boolean> checks)
	{
		if(checkedItems == null)
		{
			Log.w("Shika", "Something went wrong " + isCheckingList);
			return;
		}
		Collections.fill(checkedItems, Boolean.FALSE);

		int size = checks.size();
		for(int i = 0; i < size; i++)
		{
			int key = checks.keyAt(i);
			if (key < checkedItems.size())
				checkedItems.set(key, Boolean.TRUE);
			else
			{
				while (key > checkedItems.size()) checkedItems.add(Boolean.FALSE);
				checkedItems.add(Boolean.TRUE);
			}
		}

		notifyDataSetChanged();
	}

	public void unCheck()
	{
		Collections.fill(checkedItems, Boolean.FALSE);
		notifyDataSetChanged();
	}

	public int getCheckedAmount()
	{
		int checked = 0;
		int size = checkedItems.size();

		for(int i = 0; i < size; i++)
			if(checkedItems.get(i))
				checked++;

		return checked;
	}

	public String[] getChecked()
	{
		int checked = 0;
		int size = checkedItems.size();

		for(int i = 0; i < size; i++)
			if(checkedItems.get(i))
				checked++;

		String[] ans = new String[checked];
		int j = 0;

		for(int i = 0; i < size; i++)
			if(checkedItems.get(i))
			{
				if(keys.containsValue(i))
					for(String key : keys.keySet())
					{
						if (keys.get(key).equals(i))
						{
							ans[j++] = key;
						}
					}
			}

		return ans;
 	}

	public boolean isChecked(int position)
	{
		return checkedItems.get(position);
	}

	public void showCheckboxes(boolean show)
	{
		showCheckboxes = show;
		notifyDataSetChanged();
	}
}
