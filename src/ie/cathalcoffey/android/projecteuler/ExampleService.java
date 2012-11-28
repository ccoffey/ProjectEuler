package ie.cathalcoffey.android.projecteuler;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProblem;
import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProfile;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public class ExampleService extends IntentService 
{
	static MyDataBaseHelper myDbHelper;
	
	public ExampleService() 
	{
	    super("");
	}
	
	public ExampleService(String name) 
	{
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		SharedPreferences settings = getSharedPreferences("euler", MODE_PRIVATE);
    	SharedPreferences.Editor prefEditor = settings.edit();
    	
    	if(settings.contains("username") && settings.contains("password"))
    	{
    		String username = settings.getString("username", "");
    		String password =  settings.getString("password", "");
    		
	    	ProjectEulerClient pec = new ProjectEulerClient();
		    try 
		    {		        
				if(pec.login(username, password))
				{
					EulerProfile ep = pec.getProfile();
					
	    	        prefEditor.putString("username", username);
	    	        prefEditor.putString("password", password);
	    	        prefEditor.putString("alias", ep.alias);
	    	        prefEditor.putString("country", ep.country);
	    	        prefEditor.putString("language", ep.language);
	    	        prefEditor.putString("level", ep.level);
	    	        prefEditor.putString("solved", ep.solved);
	    	        
	    	        ArrayList<EulerProblem> problems = pec.getProblems();
					
	    	        myDbHelper = new MyDataBaseHelper(this);
	    		    myDbHelper.openDataBase(SQLiteDatabase.OPEN_READWRITE);
	    	        myDbHelper.updateProblems(pec, problems, true);
	    	        myDbHelper.close();
				}
		    }
		    
		    catch (ClientProtocolException e) 
		    {

			} 
		    
		    catch (IOException e) 
		    {

			}		
	    }
    }
	
	static void kill()
	{
		if(myDbHelper != null)
		    myDbHelper.kill();
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