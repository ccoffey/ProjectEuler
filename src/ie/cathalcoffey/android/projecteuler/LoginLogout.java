package ie.cathalcoffey.android.projecteuler;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;

import com.actionbarsherlock.view.MenuItem;

import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProblem;
import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProfile;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;

public class LoginLogout extends Activity implements LoginDialogFragment.NoticeDialogListener
{
	Activity fragmentActivity;
	Context context;
    
	public class LoginOperation extends AsyncTask<String, Void, String> 
	{
		  LoginDialogFragment dialog;
		  String progressMsg;
		  boolean success;
		  boolean completed;
		   
		  public LoginOperation(Activity fragmentActivity)
		  {
			  dialog = new LoginDialogFragment();
			  dialog.setCancelable(false);
			  dialog.show(fragmentActivity.getSupportFragmentManager(), "");
		  }
		
	      @Override
	      protected String doInBackground(String... params) 
	      {
	    	    success = false;
	    	  
	    	    String username = params[0];
				String password = params[1];
				
				MyApplication.pec = new ProjectEulerClient();
				
			    try 
			    {
					if(MyApplication.pec.login(username, password))
					{
						EulerProfile ep = MyApplication.pec.getProfile();
						
						MyApplication.prefEditor.putString("username", username);
						MyApplication.prefEditor.putString("password", password);
						MyApplication.prefEditor.putString("alias", ep.alias);
						MyApplication.prefEditor.putString("country", ep.country);
						MyApplication.prefEditor.putString("language", ep.language);
						MyApplication.prefEditor.putString("level", ep.level);
						MyApplication.prefEditor.putString("solved", ep.solved);
		    	        
						this.progressMsg = "Login successful";
						publishProgress();
				    	
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
						  
						this.progressMsg = "Syncing data";
						publishProgress();
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
						
						ArrayList<EulerProblem> problems = MyApplication.pec.getProblems();
					
						MyApplication.cancelUpdater = false;
						MyApplication.myDbHelper.updateProblems(MyApplication.pec, problems, false, false);	
						
					    int[] counts = MyApplication.myDbHelper.getSolvedCount();
					    MyApplication.COUNT_SOLVED = counts[0];
					    MyApplication.COUNT_ALL = counts[1];
					  
						success = true;
						completed = true;
						
				        this.progressMsg = "Finished";
						publishProgress();
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
					}
					
					else
					{
						this.progressMsg = MyApplication.pec.getError();
						completed = true;
						
						publishProgress();
					}
				} 
			    
			    catch (ClientProtocolException e) 
			    {
			        this.progressMsg = "Unable to connect to projecteuler.net, please check your internet connection.";
					publishProgress();
				} 
			    
			    catch (IOException e) 
			    {
			    	 this.progressMsg = "Unable to connect to projecteuler.net, please check your internet connection.";
					 publishProgress();
				}
			    
	          return null;
	      }      

	      @Override
	      protected void onPostExecute(String result) 
	      {               	    	  

	      }

	      @Override
	      protected void onPreExecute() 
	      {

	      }

	      @Override
	      protected void onProgressUpdate(Void... values) 
	      {
	    	  try
	    	  {
		    	  if(dialog != null)
		    	  {  
			    	  if(MyApplication.login_opt != null)
		    		      MyApplication.login_opt.progressMsg = progressMsg;
			    	  
			    	  dialog.setMessage(progressMsg);
			    	  
			    	  if(completed)
			    		  dialog.completed();
		    	  }
	    	  }
	    	  
	    	  catch(Exception e)
	    	  {
	    		  Log.e("Exception", e.getMessage());
	    	  }
	      }
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
    {
    	  if (item.getItemId() == android.R.id.home) 
    	  {
              finish();
              overridePendingTransition(0, 0);
              
              return true;
          }
    	  
    	  return true;
    }
	
	@Override
	public void onBackPressed() 
	{
    	if(MyApplication.login_opt != null)
		{
		    MyApplication.login_opt.cancel(true);
		    MyApplication.login_opt = null;
		}
    	
	    this.finish();
	    overridePendingTransition(0, 0);
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		
		Log.d("cathal", "LoginLogout: onResume()");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);

	    Log.d("cathal", "LoginLogout: onCreate()");
	    
	    fragmentActivity = this;
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    context = this;

	    if(MyApplication.settings != null && MyApplication.settings.contains("username"))
        {
        	setContentView(R.layout.logout);
        	
        	TextView tv = (TextView)findViewById(R.id.textView1);
        	tv.setText(MyApplication.settings.getString("username", "unknown"));
        	
        	Button b = (Button)findViewById(R.id.button1);
    	    b.setOnClickListener
    	    (
    	    		new OnClickListener()
    	    		{
    					@Override
    					public void onClick(View v) 
    					{
    						MyApplication.cancelUpdater = true;
    						
    						if(MyApplication.updater_pec != null)
    						{
    							if(MyApplication.updater_pec.httppost != null)
    							    MyApplication.updater_pec.httppost.abort();
    							
    							if(MyApplication.updater_pec.httpget != null)
    						        MyApplication.updater_pec.httpget.abort();
    						}
    						
    						MyApplication.myDbHelper.updateSolved();
    						MyApplication.settings.edit().clear();
    						MyApplication.settings.edit().commit();
    						
    						MyApplication.prefEditor = null;
    						MyApplication.settings = null;
    						
    						MyApplication.COUNT_SOLVED = 0;
    						
    						finish();
    						overridePendingTransition(0, 0);
    					}
    				}
    	    );
        }
        
        else
        {
        	setContentView(R.layout.login);
        	
        	Button b = (Button)findViewById(R.id.button1);
    	    b.setOnClickListener
    	    (
    	    		new OnClickListener()
    	    		{
    					@Override
    					public void onClick(View v) 
    					{
                            if(MyApplication.settings == null)
    						    MyApplication.settings = getSharedPreferences("euler", MODE_PRIVATE);

                            if(MyApplication.prefEditor == null)
    						    MyApplication.prefEditor = MyApplication.settings.edit();
                            
    						EditText et1 = (EditText)findViewById(R.id.editText1);
    						EditText et2 = (EditText)findViewById(R.id.editText2);
    						
    						String username = et1.getText().toString();
    						String password = et2.getText().toString();
    						
    						if(username.equalsIgnoreCase("cathal") && password.equalsIgnoreCase("coffey"))
    						{
    							ImageView img_login = (ImageView)findViewById(R.id.imgLogin);
    							img_login.setImageResource(R.drawable.ccoffey);
    						}
    						
    						else if (MyApplication.login_opt == null)
    						{
    						    MyApplication.login_opt = new LoginOperation(fragmentActivity);
    						    MyApplication.login_opt.execute(new String[]{username, password});
    						}
    					}
    				}
    	    );
        }
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		if(MyApplication.login_opt.success)
		{
			MyApplication.prefEditor.commit();

			if(!ExampleService.isRunning(this) && MyApplication.settings != null && MyApplication.settings.getBoolean("autoUpdate", true) && MyApplication.settings.contains("username"))
	        {
		        Intent serviceIntent = new Intent(ExampleService.ACTION_FOREGROUND);
				serviceIntent.setClass(this, ExampleService.class);
		        startService(serviceIntent);
	        }
			
			MyApplication.cancelUpdater = false;
			
			finish();
			overridePendingTransition(0, 0);
		}
		
		if(MyApplication.login_opt != null)
		{
		    MyApplication.login_opt.cancel(true);
		    MyApplication.login_opt = null;
		}
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) 
	{
		if(MyApplication.prefEditor != null)
		{
			MyApplication.prefEditor.clear();
			MyApplication.prefEditor.commit();
		}
		
		if(MyApplication.login_opt != null)
		{
		    MyApplication.login_opt.cancel(true);
		    MyApplication.login_opt = null;
		}
	}
}