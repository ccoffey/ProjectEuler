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
	ArrayList<Long> installed;
	boolean userStarted;
	
	BusyWork(ExampleService context, Intent intent)
	{
		this.context = context;
		
		if(intent != null && intent.hasExtra("userStarted"))
		    this.userStarted = intent.getBooleanExtra("userStarted", false); 
	}
	
    @Override
    protected void onPostExecute(Void result) 
    {
        super.onPostExecute(result);
        
        Intent intent = new Intent();
        intent.setAction("UPDATE_COMPLETE");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        context.stopSelf();
        
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }
    
	@Override
	protected Void doInBackground(String... params) 
	{
    	SharedPreferences settings = context.getSharedPreferences("euler", Context.MODE_PRIVATE);
    	SharedPreferences.Editor prefEditor = settings.edit();
    	
    	if(settings.contains("username") && settings.contains("password"))
    	{
    		String username = settings.getString("username", "");
    		String password =  settings.getString("password", "");
    		
	    	MyApplication.updater_pec = new ProjectEulerClient();
		    try 
		    {		        
				if(MyApplication.updater_pec.login(username, password))
				{
					EulerProfile ep = MyApplication.updater_pec.getProfile();
					
	    	        prefEditor.putString("username", username);
	    	        prefEditor.putString("password", password);
	    	        prefEditor.putString("alias", ep.alias);
	    	        prefEditor.putString("country", ep.country);
	    	        prefEditor.putString("language", ep.language);
	    	        prefEditor.putString("level", ep.level);
	    	        prefEditor.putString("solved", ep.solved);
	    	        
	    	        ArrayList<EulerProblem> problems = MyApplication.updater_pec.getProblems();
					
	    	        if(MyApplication.myDbHelper == null)
	    	        {
	    	            MyApplication.myDbHelper = new MyDataBaseHelper(context);
	    	            MyApplication.myDbHelper.openDataBase(SQLiteDatabase.OPEN_READWRITE);
	    	        }
	    	        
	    	        MyApplication.myDbHelper.updateProblems(MyApplication.updater_pec, problems, true, userStarted);
	    	        
					int[] counts = MyApplication.myDbHelper.getSolvedCount();
	    		    MyApplication.COUNT_SOLVED = counts[0];
	    		    MyApplication.COUNT_ALL = counts[1];
	    		    
	    	        prefEditor.commit();
				}
		    }
		    
		    catch (ClientProtocolException e) 
		    {
			} 
		    
		    catch (IOException e) 
		    {
			}	
	    }
    	
		return null;
	}
}
