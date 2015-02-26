package ru.shika.app.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ru.shika.app.Lesson;
import ru.shika.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScheduleViewGroupAdapter extends PagerAdapter
{
	SparseArray<RelativeLayout> layouts;
	ArrayList <ArrayList<Lesson>> lessons;
	SparseArray <ScheduleListAdapter> adapters;
	String[] titles;

	LessonComparator comparator = new LessonComparator();

	public ScheduleViewGroupAdapter(ArrayList <ArrayList<Lesson>> lessons, String[] pageTitles)
	{
		this.lessons = lessons;
		layouts = new SparseArray<RelativeLayout>();
		adapters = new SparseArray<ScheduleListAdapter>();
		titles = pageTitles;

		//Log.w("Shika", lessons.size() + " pages in adapter");
	}

	@Override
	public int getCount()
	{
		return lessons.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object o)
	{
		return view == o;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		RelativeLayout layout;
		if (layouts.get(position) != null)
			layout = layouts.get(position);
		else
			layout = (RelativeLayout) getItem(container, position);

		layouts.put(position, layout);

		RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.fragment_schedule_list);
		recyclerView.setHasFixedSize(true);
		recyclerView.requestFocus();

		ScheduleListAdapter adapter = adapters.get(position);
		if(adapter == null)
		{
			adapter = new ScheduleListAdapter(lessons.get(position));
			adapters.put(position, adapter);
			//Log.w("Shika", "Add new adapter on position: "+position);
		}

		//Log.w("Shika", "Adapter on position: "+position);
		//logArray(adapters);

		recyclerView.setAdapter(adapter);

		recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));

		if(lessons.get(position).size() == 0)
		{
			(layout.findViewById(R.id.emptySchedule)).setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.GONE);
		}
		else
		{
			recyclerView.setVisibility(View.VISIBLE);
			(layout.findViewById(R.id.emptySchedule)).setVisibility(View.GONE);
		}

		container.addView(layout);
		return layout;
	}

	public Object getItem(ViewGroup container, int position)
	{
		return LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_schedule, container, false);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View) object);
		layouts.remove(position);
		object = null;
	}

	public void notifyDataSetChanged()
	{
		for(int i = 0; i < lessons.size(); i++)
		{
			Collections.sort(lessons.get(i), comparator);
			try
			{
				adapters.get(i).notifyDataSetChanged();
				//Log.w("Shika", "Adapter in load: "+i);
				updateVisibility(i);
			}
			catch (Exception e){/*Log.e("Shika", "Ex");*/}
		}
		//Log.w("Shika", "Adapter array contains: "+adapters.size());
		//logArray(adapters);
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return titles[position];
	}

	private void updateVisibility(int position)
	{
		RecyclerView recyclerView = (RecyclerView) (layouts.get(position))
			.findViewById(R.id.fragment_schedule_list);
		TextView empty = (TextView) (layouts.get(position))
			.findViewById(R.id.emptySchedule);
		if(lessons.get(position).size() == 0)
		{
			recyclerView.setVisibility(View.INVISIBLE);
			empty.setVisibility(View.VISIBLE);
		}
		else
		{
			//Log.d("Shika", "Adapter: in view " + position + " we have " + lessons.get(position).size() + " objects");
			empty.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);
		}
	}

	public void setTextToEmpty(String text)
	{
		int size = layouts.size();
		for(int i = 0; i < size; i++)
		{
			((TextView) layouts.valueAt(i).findViewById(R.id.emptySchedule)).setText(text);
		}
	}

	//For sort lessons
	public class LessonComparator implements Comparator<Lesson>
	{
		@Override
		public int compare(Lesson o1, Lesson o2) {
			return (o1.start.compareTo(o2.start) == 0 ? o1.end.compareTo(o2.end) : o1.start.compareTo(o2.start));
		}
	}

	private void logArray(SparseArray list)
	{
		int size = list.size();
		for(int i = 0; i < size; i++)
		{
			Log.w("Shika", list.get(i) + " " + list.keyAt(i));
		}
	}
}