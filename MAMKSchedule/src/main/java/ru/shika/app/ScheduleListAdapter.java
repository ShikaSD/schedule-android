package ru.shika.app;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.shika.mamkschedule.mamkschedule.R;

import java.util.ArrayList;

public class ScheduleListAdapter extends RecyclerView.Adapter <ScheduleListAdapter.ViewHolder>
{
	private ArrayList<Lesson> lessons;
	private long ids;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public CardView layout;

		public TextView start, end, room, name, teacher;

		public long id;

		public ViewHolder (View layout, long id)
		{
			super(layout);
			this.layout = (CardView) layout;
			start = (TextView) layout.findViewById(R.id.startTime);
			end = (TextView) layout.findViewById(R.id.endTime);
			room = (TextView) layout.findViewById(R.id.room);
			name = (TextView) layout.findViewById(R.id.lessonName);
			teacher = (TextView) layout.findViewById(R.id.teacher);

			this.id = id;
		}
	}

	public ScheduleListAdapter(ArrayList<Lesson> lessons) {
		this.lessons = lessons;
		ids = 0;
	}

	@Override
	public ScheduleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.fragment_schedule_item, parent, false);

		return new ViewHolder(v, ids++);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		holder.start.setText(lessons.get(position).start);
		holder.end.setText(lessons.get(position).end);
		holder.room.setText(lessons.get(position).room);
		holder.name.setText(lessons.get(position).name);
		holder.teacher.setText(lessons.get(position).teacher);
	}

	@Override
	public int getItemCount() {
		return lessons.size();
	}

	public void swapData(ArrayList<Lesson> data)
	{
		int size = lessons.size();

		for(int i = 0; i < size; i++)
			notifyItemRemoved(i);

		lessons = data;

		size = data.size();

		for(int i = 0; i < size; i++)
			notifyItemRemoved(0);
	}

	@Override
	public long getItemId(int position)
	{
		return lessons.get(position).hashCode();
	}
}