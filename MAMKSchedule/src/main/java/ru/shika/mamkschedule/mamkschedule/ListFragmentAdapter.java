package ru.shika.mamkschedule.mamkschedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class ListFragmentAdapter extends BaseAdapter
{
	Map<String, Integer> keys;
	ArrayList <ArrayList <String>> names;
	Context context;
	LayoutInflater layoutInflater;

	public ListFragmentAdapter(Context ctx, Map<String, Integer> keys, ArrayList<ArrayList<String>> names)
	{
		this.keys = keys;
		this.names = names;
		context = ctx;
		layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			v = layoutInflater.inflate(R.layout.fragment_list_item, viewGroup, false);
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

		return v;
	}
}
