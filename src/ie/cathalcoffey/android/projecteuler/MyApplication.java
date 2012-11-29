package ie.cathalcoffey.android.projecteuler;

import ie.cathalcoffey.android.projecteuler.LoginLogout.LoginOperation;
import ie.cathalcoffey.android.projecteuler.PageFragment.SolveOperation;

import java.util.Vector;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;

public class MyApplication extends Application 
{
	static ProjectEulerClient pec;
	static Vector<Fragment> fragments;
	static String display_text;
	static String filter_text;
	
	static SharedPreferences settings;
	static SharedPreferences.Editor prefEditor;
	
	public static MyDataBaseHelper myDbHelper;
	
	static SolveOperation solve_opt;
	static LoginOperation login_opt;
	
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