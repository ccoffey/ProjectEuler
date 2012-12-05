package ie.cathalcoffey.android.projecteuler;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class Settings extends SherlockActivity 
{
	@Override
	public void onResume() 
	{
		super.onResume();
		
		checkSettings();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState); 
	    
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
