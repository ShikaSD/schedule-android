package ru.shika.mamkschedule.mamkschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Map;

public class ListFragmentAdapter extends BaseAdapter
{
	Map<String, String> list;
	Context context;
	LayoutInflater layoutInflater;

	public ListFragmentAdapter(Context ctx, Map<String, String> list)
	{
		this.list = list;
		context = ctx;
		layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public Object getItem(int i)
	{
		return list.get(i);
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
			v = layoutInflater.inflate(R.layout.fragment_list_item, viewGroup, false);
		}

		String[] array = new String[list.keySet().size()];
		array = list.keySet().toArray(array);

		((TextView) v.findViewById(R.id.fragment_list_name)).setText(list.get(array[i]));
		if(!array[i].equals(list.get(array[i])))
			((TextView) v.findViewById(R.id.fragment_list_id)).setText(array[i]);

		return v;
	}
}
