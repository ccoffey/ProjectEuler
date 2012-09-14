package ie.cathalcoffey.android;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Tabs extends TabActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);
        
    	int mAppWidgetId;
    	Bundle extras = getIntent().getExtras();
	    if (extras != null) 
	    	mAppWidgetId = extras.getInt("mAppWidgetId");
	    
    	TabHost tabHost = getTabHost();
    	 
        TabSpec problemsSpec = tabHost.newTabSpec("Problems");
        problemsSpec.setIndicator("Problems", getResources().getDrawable(R.drawable.problems_selected));
        Intent problemsIntent = new Intent(this, ProblemsTab.class);
        problemsIntent.putExtras(getIntent());
        problemsSpec.setContent(problemsIntent);
        
        TabSpec settingsSpec = tabHost.newTabSpec("Settings");
        settingsSpec.setIndicator("Settings", getResources().getDrawable(R.drawable.settings_selected));
        Intent settingsIntent= new Intent(this, Settings.class);
        settingsIntent.putExtras(getIntent());
        settingsSpec.setContent(settingsIntent);
        
        tabHost.addTab(problemsSpec);
        tabHost.addTab(settingsSpec);
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    }
}
