package ru.shika.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListFragmentAdapter extends RecyclerView.Adapter<ListFragmentAdapter.ViewHolder>
{
	private SparseArray <String> keys;
	private ArrayList <ArrayList <String>> names;

	private int layoutId;
	public boolean isCheckingList;
	private ArrayList<Boolean> checkedItems;
	private boolean showCheckboxes;

	private int size;

	public ListFragmentAdapter(SparseArray <String> keys, ArrayList<ArrayList<String>> names, boolean isCheckingList)
	{
		/*this.keys = new SparseArray<String>();
		this.names = new ArrayList<ArrayList<String>>();

		int size = keys.size();
		for(int i = 0; i < size; i++)
			this.keys.append(keys.keyAt(i), keys.valueAt(i));this.names.addAll(names);    */

		this.keys = keys;
		this.names = names;

		size = keys.size();

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

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		public LinearLayout layout;

		public TextView name, courseId;
		public CheckBox check;

		public ViewHolder(View itemView)
		{
			super(itemView);

			layout = (LinearLayout) itemView;

			name = (TextView) layout.findViewById(R.id.fragment_list_name);
			courseId = (TextView) layout.findViewById(R.id.fragment_list_id);

			if(isCheckingList)
				check = (CheckBox) layout.findViewById(R.id.list_checkbox);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{
		View v = LayoutInflater.from(viewGroup.getContext())
			.inflate(layoutId, viewGroup, false);

		return new ViewHolder(v);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public int getItemCount()
	{
		return keys.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i)
	{
		String name = names.get(i).get(0);
		/*for(int j = 1; j < size; j++)
			name += "|"+names.get(i).get(j);*/

		viewHolder.name.setText(name);

		if(!keys.get(i).equals(names.get(i).get(0)))
			viewHolder.courseId.setText(keys.get(i));
		else
			viewHolder.courseId.setText("");

		if(isCheckingList)
		{
			if(i > checkedItems.size() - 1)
				while(checkedItems.size() - 1 < i)
					checkedItems.add(Boolean.FALSE);

			viewHolder.check.setChecked(checkedItems.get(i));

			if(showCheckboxes)
			{
				viewHolder.check.setVisibility(View.VISIBLE);
				viewHolder.check.setButtonDrawable(R.drawable.checkbox);
			}
			else
				viewHolder.check.setVisibility(View.GONE);
		}
	}

	public void swapData(int amount)
	{
		int size = this.names.size();

		StringComparator comparator = new StringComparator();
		for(int i = 0; i < size; i++)
		{
			Collections.sort(this.names.get(i), comparator);
		}

		notifyDataSetChanged();
	}

	public void toggle(int key)
	{
		if (key < checkedItems.size())
			checkedItems.set(key, !checkedItems.get(key));
		else
		{
			while (key > checkedItems.size()) checkedItems.add(Boolean.FALSE);
			checkedItems.add(Boolean.TRUE);
		}

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
				ans[j++] = keys.get(i);
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

	public static class StringComparator implements Comparator <String>
	{
		@Override
		public int compare(String o1, String o2)
		{
			if(o1.length() > o2.length())
				return 1;

			if(o1.length() == o2.length())
				return 0;

			return -1;
		}
	}
}
