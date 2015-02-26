package ru.shika.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import ru.shika.app.R;

public class EditSimpleAdapter extends BaseAdapter
{
	private Context context;
	private LayoutInflater layoutInflater;
	private String[] items;


	public EditSimpleAdapter(Context ctx, String[] items)
	{
		context = ctx;
		layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.items = items;
	}

	@Override
	public int getCount()
	{
		return items.length;
	}

	@Override
	public Object getItem(int i)
	{
		return items[i];
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
			v = layoutInflater.inflate(R.layout.simple_list_item, viewGroup, false);
		}

		((TextView) v.findViewById(R.id.text)).setText(items[i]);

		return v;
	}
}
