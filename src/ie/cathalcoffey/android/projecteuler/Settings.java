package ie.cathalcoffey.android.projecteuler;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Settings extends Activity 
{
	Editor editor;
	SharedPreferences prefs;
	
	int mAppWidgetId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.settings);
	    
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) 
	    	mAppWidgetId = extras.getInt("mAppWidgetId");
	    
	    editor = getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + mAppWidgetId, Context.MODE_PRIVATE).edit();
	    prefs = getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + mAppWidgetId, Context.MODE_PRIVATE);
	    
	    ToggleButton coloredTextButton = (ToggleButton) findViewById(R.id.toggleButton1);
	    coloredTextButton.setChecked(prefs.getBoolean("coloredText", true));
	    
	    coloredTextButton.setOnCheckedChangeListener
	    (
	    		new CompoundButton.OnCheckedChangeListener() 
	    		{
			        @Override
			        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			        {
				        editor.putBoolean("coloredText", isChecked);
				        editor.commit();
			        }
			    }
	    );
	    
        final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		List<String> list = new ArrayList<String>();
		list.add("All problems");
		list.add("Solved only");
		list.add("Unsolved only");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
		spinner.setOnItemSelectedListener
		(
				new AdapterView.OnItemSelectedListener() 
				{
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
					{						
						editor.putInt("show", spinner.getSelectedItemPosition());
				        editor.commit();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) 
					{

						
					}
				}
		);
		
		final Button updateStats = (Button) findViewById(R.id.button2);
		updateStats.setOnClickListener
		(
				new View.OnClickListener() 
				{	
					@Override
					public void onClick(View v) 
					{
						  update();
					}
				}
		);
		
		onResume();
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		
		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
		toggleButton.setChecked(prefs.getBoolean("coloredText", true));
		
		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setSelection(prefs.getInt("show", 0));
		
		TextView lastUpdated = (TextView) findViewById(R.id.lastupdated);
		lastUpdated.setText("Last updated: " + prefs.getString("lastupdated", "unknown"));
	}
	
	private void update()
	{
		EulerAsyncTask asyncTask = (EulerAsyncTask) new EulerAsyncTask()
        {
  	      private String msg;
	    	  private int total;
	    	  
            @Override
            protected void onPreExecute()
            {	
                progressDialog = new MyProgressDialog(Settings.this);
                
                progressDialog.setTitle("Project Euler");
                progressDialog.setMessage("Attempting login");       
                progressDialog.setIndeterminate(false);
                
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                
                progressDialog.setCancelable(true);
                progressDialog.show();
            }
            
            @Override
            protected void update(int progress, String msg) 
            {
                this.msg = msg;
                
                if(progress > 0)
              	  progressDialog.setProgress(progress);
                
                publishProgress(progress);
            }
            
            @Override
            protected void onProgressUpdate(Object... i) 
            {
                super.onProgressUpdate(i);
                
                if((Integer)i[0] == 1)
                {
                    progressDialog.setMax(total);
          	      progressDialog.setTextVisibility(View.VISIBLE);
                }
                
                progressDialog.setMessage(this.msg);
            }

            @Override
            protected Object doInBackground(Object... params)
            {
          	    String username = prefs.getString("username", "unknown");
		    	String password = prefs.getString("password", "unknown");
		    	    
                ProjectEuler pe = new ProjectEuler(Settings.this);
                try 
                {
				      if(pe.login(username, password))
				      {
				    	  update(0, "Login successful");
				    	  Thread.sleep(1000);
				    	  
				    	  Object[] details = pe.getDetails();
				    	
				    	  String name = (String)details[0];
				    	  String level = (String)details[1];
				    	  total = (Integer)details[2];
				    	  int progress = (Integer)details[3];
				    	  String solved = (String)details[4];
				    	  	  
				          Editor editor = getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + mAppWidgetId, Context.MODE_PRIVATE).edit();
				          editor.putString("username", name);
				          editor.putString("level", level);
				          editor.putString("solved", solved);
				          editor.putInt("progress", progress);
				          editor.putInt("total", total);
				          
				          SQLHelper sqlHelper = new SQLHelper(getApplicationContext());
				          pe.getProblems(sqlHelper, this, total);
				          
				          update(0, "Finished");
				          Thread.sleep(3000);
				          progressDialog.dismiss();
				          
				          sqlHelper.close();
					        
				          String lastupdated = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date());
				          editor.putString("lastupdated", lastupdated);
				          editor.commit();
				      }
				      
				      else
				      {
				    	  update(0, pe.getErrorMessage());
				    	  Thread.sleep(3000);
				      }
				  } 
                
                catch (ClientProtocolException e) 
                {
					  e.printStackTrace();
				  } 
                
                catch (IOException e) 
                {
              	  update(0, "Unable to connect to projecteuler.net, please check your internet connection.");
			    	  try 
			    	  {
						Thread.sleep(3000);
					  } 
			    	  
			    	  catch (InterruptedException e1) 
			    	  {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					  }
				  } 
                
                catch (InterruptedException e) 
                {
					// TODO Auto-generated catch block
					e.printStackTrace();
				  }
                
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                progressDialog.dismiss();
            }
        }.execute();
	}
}
