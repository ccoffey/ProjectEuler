package ie.cathalcoffey.android.projecteuler;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.ToggleButton;

import com.actionbarsherlock.view.MenuItem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Settings extends Activity 
{
    private Receiver receiver;
    
	@Override
	public void onResume() 
	{
		super.onResume();
		
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("UPDATE_COMPLETE"));
		checkSettings();
	}
	
	@Override
	public void onPause()
	{
	    super.onPause();
	    
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

	private class Receiver extends BroadcastReceiver 
	{
		 @Override
		 public void onReceive(Context arg0, Intent arg1) 
		 {   
			 MyApplication.cancelUpdater = false;
			 
			 Button b = (Button)findViewById(R.id.button1);
		     if(ExampleService.isRunning(getApplicationContext()))
		         b.setText("Cancel Update");
		     else
		         b.setText("Update Now");
		     
			 SharedPreferences prefs = getSharedPreferences("euler", MODE_PRIVATE);
			 
			 TextView tv = (TextView)findViewById(R.id.textView2);
			 
			 if(prefs.contains("username"))
			 {
			     b.setEnabled(true);
			     tv.setVisibility(View.GONE);
			 }
			 
			 else
			 {
				 b.setEnabled(false);
			     tv.setVisibility(View.VISIBLE);
			 }
		 }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState); 
	
	    receiver = new Receiver();
	    
	    setContentView(R.layout.settings);
	    
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    checkSettings();
	    
	    ToggleButton tg = (ToggleButton)findViewById(R.id.toggleButton1);
	    tg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{	
				SharedPreferences.Editor prefEditor = getSharedPreferences("euler", MODE_PRIVATE).edit();
				
				prefEditor.putBoolean("autoUpdate", isChecked);
				prefEditor.commit();
			}
		});
	    
	    final Button b = (Button)findViewById(R.id.button1);
	    if(ExampleService.isRunning(this))
	    	b.setText("Cancel Update");
	    else
	    	b.setText("Update Now");
	    
	    SharedPreferences prefs = getSharedPreferences("euler", MODE_PRIVATE); 	
	    TextView tv = (TextView)findViewById(R.id.textView2);
		 
		if(prefs.contains("username"))
		{
		    b.setEnabled(true);
		    tv.setVisibility(View.GONE);
		}
		 
		else
		{
			b.setEnabled(false);
		    tv.setVisibility(View.VISIBLE);
		}
		 
	    b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!ExampleService.isRunning(getApplicationContext()))
				{
					MyApplication.cancelUpdater = false;
					
					Intent serviceIntent = new Intent(ExampleService.ACTION_FOREGROUND);
					serviceIntent.setClass(getApplicationContext(), ExampleService.class);
					serviceIntent.putExtra("userStarted", true);
			        startService(serviceIntent);
			        
			    	b.setText("Cancel Update");
				}
			    
				else
				{
					MyApplication.cancelUpdater = true;
					
					if(MyApplication.updater_pec != null)
					{
						if(MyApplication.updater_pec.httppost != null)
						    MyApplication.updater_pec.httppost.abort();
						
						if(MyApplication.updater_pec.httpget != null)
					        MyApplication.updater_pec.httpget.abort();
					}
					
			    	b.setText("Update Now");
				}
			}
		});
	}
	
	private void checkSettings()
	{
		SharedPreferences prefs = getSharedPreferences("euler", MODE_PRIVATE);
		ToggleButton tg = (ToggleButton)findViewById(R.id.toggleButton1);
		tg.setChecked(prefs.getBoolean("autoUpdate", true));
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
}
