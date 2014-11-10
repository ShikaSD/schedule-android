package ru.shika.mamkschedule.mamkschedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.shika.android.SlidingTabLayout;

import java.util.ArrayList;

public class ScheduleViewGroupFragment extends Fragment
{
	WeekPagerAdapter pagerAdapter;
	ViewPager viewPager;
	String[] days;
	SlidingTabLayout tabLayout;

	ArrayList<Fragment> fragments = new ArrayList <Fragment>();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.w("Shika", "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.w("Shika", "onCreateView");
		return inflater.inflate(R.layout.fragment_schedule_viewgroup, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		days = getResources().getStringArray(R.array.days);

		viewPager = (ViewPager) view.findViewById(R.id.pager);
		tabLayout = (SlidingTabLayout) view.findViewById(R.id.tabs);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		Log.w("Shika", "onActivityCreated");

		//Init viewpager with adapter
		pagerAdapter = new WeekPagerAdapter(getChildFragmentManager());
		viewPager.setAdapter(pagerAdapter);

		//Init tabs
		tabLayout.setViewPager(viewPager);
		tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.light_blue));

	}

	public class WeekPagerAdapter extends FragmentPagerAdapter
	{
		public WeekPagerAdapter(FragmentManager fm)
		{
			super(fm);
			for(int i = 0; i < days.length; i++)
			{
				fragments.add(ScheduleFragment.newInstance("#ffffff"));
			}
		}

		@Override
		public Fragment getItem(int position)
		{
			return fragments.get(position);
		}

		@Override
		public int getCount() {

			return days.length;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return days[position];
		}
	}
}
