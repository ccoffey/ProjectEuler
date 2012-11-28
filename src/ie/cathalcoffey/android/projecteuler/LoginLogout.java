package ie.cathalcoffey.android.projecteuler;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProblem;
import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProfile;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginLogout extends SherlockActivity 
{
	Context context;
	private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    
	public class LongOperation extends AsyncTask<String, Void, String> 
	{
		  private ProgressDialog dialog;
		  private String progressMsg;
		  private boolean success;
		   
		  public LongOperation()
		  {
			  dialog = new ProgressDialog(context);
			  dialog.setCanceledOnTouchOutside(false);
		  }
		
	      @Override
	      protected String doInBackground(String... params) 
	      {
	    	    success = false;
	    	  
	    	    String username = params[0];
				String password = params[1];
				
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
		    	        
						this.progressMsg = "Login successful";
						publishProgress();
				    	
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
						  
						this.progressMsg = "Syncing data";
						publishProgress();
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
						
						ArrayList<EulerProblem> problems = pec.getProblems();
					
						MyApplication.myDbHelper.updateProblems(pec, problems, false);	
						
						success = true;
						
				        this.progressMsg = "Finished";
						publishProgress();
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
					}
					
					else
					{
						this.progressMsg = pec.getError();
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
	    	  if(success && dialog.isShowing())
	    	  {
    	          prefEditor.commit();
	    	        
	    		  dialog.dismiss();
	    		  
	    		  finish();
	    		  overridePendingTransition(0, 0);
	    	  }
	      }

	      @Override
	      protected void onPreExecute() 
	      {
	    	  this.dialog.setMessage("Attempting login");
	          this.dialog.show();
	      }

	      @Override
	      protected void onProgressUpdate(Void... values) 
	      {
	    	  this.dialog.setMessage(progressMsg);
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
	    this.finish();
	    overridePendingTransition(0, 0);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);

	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    context = this;
		settings = getSharedPreferences("euler", MODE_PRIVATE);
        prefEditor = settings.edit();

        if(settings.contains("username"))
        {
        	setContentView(R.layout.logout);
        	
        	TextView tv = (TextView)findViewById(R.id.textView1);
        	tv.setText(settings.getString("username", "unknown"));
        	
        	Button b = (Button)findViewById(R.id.button1);
    	    b.setOnClickListener
    	    (
    	    		new OnClickListener()
    	    		{
    					@Override
    					public void onClick(View v) 
    					{
    						MyApplication.myDbHelper.updateSolved();	
    						
    						prefEditor.clear();
    						prefEditor.commit();
    						
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
    						EditText et1 = (EditText)findViewById(R.id.editText1);
    						EditText et2 = (EditText)findViewById(R.id.editText2);
    						
    						String username = et1.getText().toString();
    						String password = et2.getText().toString();
    						
    						new LongOperation().execute(new String[]{username, password});
    					}
    				}
    	    );
        }
	}
}
