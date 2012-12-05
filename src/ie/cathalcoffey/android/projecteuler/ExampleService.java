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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ExampleService extends ForegroundService 
{
	Receiver receiver;
	BusyWork updater;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		receiver = new Receiver();
		registerReceiver(receiver, new IntentFilter("ie.cathalcoffey.android.ProjectEuler.CANCEL_UPDATE")); 
	}
    
	private class Receiver extends BroadcastReceiver 
	{
		 @Override
		 public void onReceive(Context arg0, Intent arg1) 
		 {
			 Log.w("ProjectEuler", "Cancel background updater service");
			 updater.cancel(true);
		 }
	}
	
	@Override
	public void handleCommand(Intent intent)
	{
		super.handleCommand(intent);
		
		if (intent == null)
    		return;
    	
        if (ACTION_FOREGROUND.equals(intent.getAction())) 
        {
        	NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    		
        	NotificationCompat.Builder builder = new  NotificationCompat.Builder(this);
        	
        	Intent i = new Intent(this, ProblemList.class);
        	i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);        	
        	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        	
        	builder.setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentTitle("Updating problem set")
            .setContentText("Authenticating...");
        	Notification notification = builder.build();
        	notification.flags |= Notification.FLAG_AUTO_CANCEL;
        	
            notificationManager.notify(1, notification);
            updater = new BusyWork(this);
    		updater.execute();
    		
            startForegroundCompat(1, notification);
        } 
        
        else if (ACTION_BACKGROUND.equals(intent.getAction())) 
        {
            stopForegroundCompat(1);
        }
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	    
	    unregisterReceiver(receiver);
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