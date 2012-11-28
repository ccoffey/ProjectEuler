package ie.cathalcoffey.android.projecteuler;

import ie.cathalcoffey.android.projecteuler.PageFragment.SolveOperation;

import java.util.Vector;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;

public class MyApplication extends Application 
{
	static ProjectEulerClient pec;
	static SolveOperation solve_opt;
	static Vector<Fragment> fragments;
	static String display_text;
	static String filter_text;
	public static MyDataBaseHelper myDbHelper;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() 
	{   
		super.onCreate();
	}
	
	@Override
	public void onLowMemory() 
	{
		super.onLowMemory();
	}

	@Override
	public void onTerminate() 
	{
		super.onTerminate();
	}
}