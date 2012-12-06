package ie.cathalcoffey.android.projecteuler;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProblem;
import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProfile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

public class BusyWork extends AsyncTask<String, Void, Void>
{
	ExampleService context;
	
	BusyWork(ExampleService context)
	{
		this.context = context;
	}
	
    @Override
    protected void onPostExecute(Void result) 
    {
        super.onPostExecute(result);
        
        Intent intent = new Intent();
        intent.setAction("UPDATE_COMPLETE");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        
        context.stopForegroundCompat(1);
        context.stopSelf();
    }
    
	@Override
	protected Void doInBackground(String... params) 
	{
		NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
    	SharedPreferences settings = context.getSharedPreferences("euler", Context.MODE_PRIVATE);
    	SharedPreferences.Editor prefEditor = settings.edit();
    	
    	if(settings.contains("username") && settings.contains("password"))
    	{
    		String username = settings.getString("username", "");
    		String password =  settings.getString("password", "");
    		
	    	MyApplication.pec = new ProjectEulerClient();
		    try 
		    {		        
				if(MyApplication.pec.login(username, password))
				{
					EulerProfile ep = MyApplication.pec.getProfile();
					
	    	        prefEditor.putString("username", username);
	    	        prefEditor.putString("password", password);
	    	        prefEditor.putString("alias", ep.alias);
	    	        prefEditor.putString("country", ep.country);
	    	        prefEditor.putString("language", ep.language);
	    	        prefEditor.putString("level", ep.level);
	    	        prefEditor.putString("solved", ep.solved);
	    	        
	    	        ArrayList<EulerProblem> problems = MyApplication.pec.getProblems();
					
	    	        if(MyApplication.myDbHelper == null)
	    	        {
	    	            MyApplication.myDbHelper = new MyDataBaseHelper(context);
	    	            MyApplication.myDbHelper.openDataBase(SQLiteDatabase.OPEN_READWRITE);
	    	        }
	    	        
	    	        MyApplication.myDbHelper.updateProblems(MyApplication.pec, problems, true);
	    	        prefEditor.commit();
	    	        
	    			notificationManager.cancel(1);
				}
				
				else
				{
					NotificationCompat.Builder builder = new  NotificationCompat.Builder(context);
				    
					Intent intent = new Intent(context, LoginLogout.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );

					builder.setContentIntent(contentIntent)
				    .setSmallIcon(R.drawable.ic_notification)
				    .setWhen(System.currentTimeMillis())
				    .setAutoCancel(true)
				    .setContentTitle("Updating problem set")
				    .setContentText("Unable to login, please check your login details.");
					Notification notification = builder.build();
					notification.flags |= Notification.FLAG_AUTO_CANCEL;	
					
					notificationManager.notify(1, notification);
				}
		    }
		    
		    catch (ClientProtocolException e) 
		    {
    			notificationManager.cancel(1);
			} 
		    
		    catch (IOException e) 
		    {
    			notificationManager.cancel(1);
			}	
	    }
    	
		return null;
	}
}
