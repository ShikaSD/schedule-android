package ru.shika.mamkschedule.mamkschedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Interfaces
{
	public abstract interface Download
	{
		public void onDownloadEnd(String result);
		public void updateInProgress(int amount);
		public void onDateChanged(Date date);
	}

	public abstract interface needDownload
	{
		public void needDownload(String group, String teacher, String course, Calendar date);
	}

	public abstract interface updateFragment
	{
		public void update(ArrayList< ArrayList<Lesson>> lessons);
	}

	public abstract interface groupFragmentCallback
	{
		public void listSelected(String type, String item);
	}

}
