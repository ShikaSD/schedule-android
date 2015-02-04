package ru.shika.app.loaders;

import android.content.Context;
import ru.shika.app.interfaces.LocalLoaderInterface;

public class ListLocalLoader extends LocalLoader
{
	private String type;
	private int downloaded;

	public ListLocalLoader(int id, Context ctx, LocalLoaderInterface callback, String type, int downloaded)
	{
		super(id, ctx, callback, LoaderCode.LIST);

		this.type = type;
		this.downloaded = downloaded;
	}


	@Override
	protected void load()
	{
		cursor = dbh.rawQuery("select max(id) from "+ type, null);
		if(cursor.moveToFirst() && downloaded != -1)
			downloaded = cursor.getInt(0) - downloaded;
		else
			downloaded = 0;

		cursor = dbh.rawQuery("select * from "+ type +" order by name limit "+ downloaded +", 1000000", null); //1000000 -> just big number
	}
}
