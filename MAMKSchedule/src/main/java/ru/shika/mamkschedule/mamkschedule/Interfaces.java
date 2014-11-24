package ru.shika.mamkschedule.mamkschedule;

import java.util.ArrayList;
import java.util.Calendar;

public class Interfaces
{
	public abstract interface Download
	{
		public void onDownloadEnd(String result);
		public void updateEnded();
	}

	public abstract interface needDownload
	{
		public void needDownload(String group, Calendar date);
	}

	public abstract interface updateFragment
	{
		public void update(ArrayList< ArrayList<Lesson>> lessons);
	}

}
