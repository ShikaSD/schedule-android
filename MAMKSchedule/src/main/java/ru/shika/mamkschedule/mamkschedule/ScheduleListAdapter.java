package ru.shika.mamkschedule.mamkschedule;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleListAdapter extends RecyclerView.Adapter <ScheduleListAdapter.ViewHolder>
{
	private ArrayList<Lesson> lessons;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public CardView layout;

		public TextView start, end, room, name, teacher;

		public ViewHolder (View layout)
		{
			super(layout);
			this.layout = (CardView) layout;
			start = (TextView) layout.findViewById(R.id.startTime);
			end = (TextView) layout.findViewById(R.id.endTime);
			room = (TextView) layout.findViewById(R.id.room);
			name = (TextView) layout.findViewById(R.id.lessonName);
			teacher = (TextView) layout.findViewById(R.id.teacher);
		}
	}

	public ScheduleListAdapter(ArrayList<Lesson> lessons) {
		this.lessons = lessons;
	}

	@Override
	public ScheduleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext())
			                   .inflate(R.layout.fragment_schedule_card, parent, false);

		return new ViewHolder(v);
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
}
