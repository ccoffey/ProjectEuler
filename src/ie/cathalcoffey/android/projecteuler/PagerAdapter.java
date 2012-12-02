package ie.cathalcoffey.android.projecteuler;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter 
{
	public PagerAdapter(FragmentManager fm) 
	{
		super(fm);
	}
	
	@Override
	public Fragment getItem(int position) 
	{
		return MyApplication.fragments.get(position);
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		PageFragment f = (PageFragment)getItem(position);
		return ("Problem " + f.getArguments().getLong("_id")).toUpperCase();
	}
	
	@Override
	public int getCount() 
	{
		if(MyApplication.fragments == null)
			return 0;
		return MyApplication.fragments.size();
	}
}
