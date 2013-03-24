package ie.cathalcoffey.android.projecteuler;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ExampleService extends ForegroundService 
{
	BusyWork updater;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
	}
	
	@Override
	public void handleCommand(Intent intent)
	{
		super.handleCommand(intent);
		
		if (intent == null)
    		return;
    	
        updater = new BusyWork(this, intent);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
        	  updater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else 
        	  updater.execute();
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	    
		MyApplication.cancelUpdater = false;
	}
	
	@Override
	public void onStart(Intent intent, int startId) 
	{
		super.onStart(intent, startId);
	}
		
	static boolean isRunning(Context c) 
	{
	    ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
	        if (ExampleService.class.getName().equals(service.service.getClassName()))
	            return true;
	    
	    return false;
	}
}