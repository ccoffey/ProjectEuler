package ie.cathalcoffey.android.projecteuler;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.FilterQueryProvider;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.TextView;

public class ProblemList extends Activity implements SearchView.OnQueryTextListener, ActionBar.OnNavigationListener
{
	private SimplerCursorAdapter cursorAdapter;
    private String queryText;
    private Spinner spinner;
    private ArrayAdapter spinnerArrayAdapter;
    private SearchView searchView;
	private boolean first = true;
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
        //Create the search view
        searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        
        menu.add("Search")
            .setIcon(R.drawable.abs__ic_search)
            .setActionView(searchView)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
        spinner = new Spinner(getSupportActionBar().getThemedContext());
        spinnerArrayAdapter = new CustomArrayAdapter
        (
            getSupportActionBar().getThemedContext(), 
            R.layout.spinner_item, 
            new String[]{Label.Unsolved.toString(), Label.Solved.toString(), Label.Starred.toString(), Label.All.toString()}
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
          
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerArrayAdapter, this);
     
        menu.add(Menu.NONE, 456, Menu.NONE, "Settings")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        
        MenuItem loginlogout;
		if (MyApplication.settings != null && MyApplication.settings.contains("username"))
        	loginlogout = menu.add(Menu.NONE, 123, Menu.NONE, "Logout");
        else
        	loginlogout = menu.add(Menu.NONE, 123, Menu.NONE, "Login");
        loginlogout.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);	
        
        return true;
    }
	
	@Override
	public void onBackPressed() 
	{
	    this.finish();
	    overridePendingTransition(0, 0);
	}
	
	Receiver receiver;
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		super.onPrepareOptionsMenu (menu);
		
	    if (MyApplication.settings != null && MyApplication.settings.contains("username"))
		    menu.findItem(123).setTitle("Logout");
        else
        	menu.findItem(123).setTitle("Login");
	   
	    return true;        
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case 456:
		    	Intent settings = new Intent(this, Settings.class);
	        	settings.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	        	startActivity(settings);
	        	break;
		    
			case 123:
				Intent loginlogout = new Intent(this, LoginLogout.class);
				loginlogout.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		        startActivity(loginlogout);
		        break;
		    
			default:
		        return super.onOptionsItemSelected(item);
		}
	    return true;
	}
	
    @Override
	public void onResume() 
	{
	    super.onResume();
		
	    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("UPDATE_COMPLETE"));
	    
	    TextView solved = (TextView)findViewById(R.id.solved); 
	    solved.setText(String.format("Solved %d of %d", MyApplication.COUNT_SOLVED, MyApplication.COUNT_ALL));

	    MyApplication.stars.clear();
    	
    	if(MyApplication.settings != null && MyApplication.settings.contains("username"))
		{
			String username = MyApplication.settings.getString("username", "");
			SharedPreferences user_stars = getSharedPreferences(username + "_stars", Context.MODE_PRIVATE);
			
			Map<String, ?> items = user_stars.getAll();
			for(String id : items.keySet())
				MyApplication.stars.put(id, (Boolean)items.get(id));
		}
    	
    	cursorAdapter.getFilter().filter("");
	}
	
	@Override
	public void onPause()
	{
	    super.onPause();
	    
	    if(MyApplication.settings != null && MyApplication.settings.contains("username"))
		{
			String username = MyApplication.settings.getString("username", "");
			SharedPreferences.Editor user_stars_editor = getSharedPreferences(username + "_stars", Context.MODE_PRIVATE).edit();
			user_stars_editor.clear();
			for(String id : MyApplication.stars.keySet())
				user_stars_editor.putBoolean(id, MyApplication.stars.get(id));
			user_stars_editor.commit();
		}
	    
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}
	
	private class Receiver extends BroadcastReceiver 
	{
		 @Override
		 public void onReceive(Context arg0, Intent arg1) 
		 {  
			 if(MyApplication.settings != null && MyApplication.settings.contains("username"))
			 {
			     String username = MyApplication.settings.getString("username", "");
				 SharedPreferences.Editor user_stars_editor = getSharedPreferences(username + "_stars", Context.MODE_PRIVATE).edit();
				 user_stars_editor.clear();
				 for(String id : MyApplication.stars.keySet())
					 user_stars_editor.putBoolean(id, MyApplication.stars.get(id));
				 user_stars_editor.commit();
		     }
			 
             onResume();
		 }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    receiver = new Receiver();
	    
	    if(MyApplication.settings == null)
	        MyApplication.settings = getSharedPreferences("euler", MODE_PRIVATE);
	    
	    if(MyApplication.prefEditor == null)
	        MyApplication.prefEditor = MyApplication.settings.edit();
	    
	    if(MyApplication.myDbHelper == null)
	    {
	        MyApplication.myDbHelper = new MyDataBaseHelper(this);
	        
	        try 
	        {
	        	MyApplication.myDbHelper.createDataBase();
	        }
	        
	        catch (IOException ioe) 
	        {
	        	throw new Error("Unable to create database");
	        }
	 
	        try 
	        {
	        	MyApplication.myDbHelper.openDataBase(SQLiteDatabase.OPEN_READWRITE);
	        }
	        
	        catch(SQLException sqle)
	        {
	        	throw sqle;
	        }
	    }
	    
	    getSupportActionBar().setDisplayShowTitleEnabled(false); 
	    
	    setContentView(R.layout.problemlist);
	    
	    ListView list = (ListView)findViewById(R.id.list);
	    
	    list.setTextFilterEnabled(true);
	    list.setFastScrollEnabled(true);
	    
        try 
        {
        	String[] from = new String[]{"_id", "title", "solvedby"};
    	    int[] to = new int[]{R.id.id, R.id.title, R.id.solved_by};

    	    MyApplication.display_text = Label.Unsolved.toString();
    	    Cursor cursor = MyApplication.myDbHelper.getData();
    	    
    	    cursorAdapter = new SimplerCursorAdapter(this, R.layout.row, cursor, from, to);
    		cursorAdapter.setFilterQueryProvider
    		(
    				new FilterQueryProvider()
    				{
						@Override
						public Cursor runQuery(CharSequence constraint) 
						{
							return MyApplication.myDbHelper.getData(MyApplication.filter_text);
						}
    				}
    		);
    		
    		list.setAdapter(cursorAdapter);
    		
    		list.setOnItemClickListener
    		(
    				new android.widget.AdapterView.OnItemClickListener() 
    				{
						public void onItemClick(android.widget.AdapterView<?> arg0, View v, int position, long  _id) 
						{
							Intent intent = new Intent(getApplicationContext(), EulerActivity.class);
    		    			
    		    			Bundle bundle = new Bundle();
    		    			bundle.putLong("_id", position);
    		    		    bundle.putString("displayText", MyApplication.display_text);
    		    			bundle.putString("constraint", queryText);
    		    			intent.putExtras(bundle);
    		    			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    		    		    
    		    			Cursor cursor = MyApplication.myDbHelper.getData(queryText);
    		    			
		    				MyApplication.fragments = new Vector<Fragment>();
		    				while (cursor.moveToNext()) 
		    				{
		    				    long _id1 = cursor.getLong(0);
		    				    String title = cursor.getString(1);
		    				    long published = cursor.getLong(2);
		    				    long updated = cursor.getLong(3);
		    				    long solvedby = cursor.getLong(4);
		    				    boolean solved = cursor.getLong(5) == 1 ? true: false;
		    				    String html = cursor.getString(6);
		    				    String answer = cursor.getString(7);
		    				    
		    				    MyApplication.fragments.add(PageFragment.newInstance(_id1, title, published, updated, solvedby, solved, html, answer));
		    				}
		    				
    		    			cursor.close();
		    				
    		    			startActivity(intent);	
						}
                    }
            );
    	
		    int[] counts = MyApplication.myDbHelper.getSolvedCount();
		    MyApplication.COUNT_SOLVED = counts[0];
		    MyApplication.COUNT_ALL = counts[1];
    	
    	    TextView solved = (TextView)findViewById(R.id.solved);
    	    solved.setText(String.format("Solved %d of %d", MyApplication.COUNT_SOLVED, MyApplication.COUNT_ALL));
 	    } 
        
        catch (Exception e) 
        {
 		    throw new Error("Unable to create database");
 	    }
        
        if(!ExampleService.isRunning(this) && MyApplication.settings != null && MyApplication.settings.getBoolean("autoUpdate", true) && MyApplication.settings.contains("username"))
        {
	        Intent serviceIntent = new Intent(ExampleService.ACTION_FOREGROUND);
			serviceIntent.setClass(this, ExampleService.class);
	        startService(serviceIntent);
        }
	}

	@Override
	public void onDestroy()
	{
	    super.onDestroy();
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) 
	{
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) 
	{
		queryText = newText;
		
		MyApplication.filter_text = newText;
    	cursorAdapter.getFilter().filter("");

		return true;
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long itemId) 
	{
		if(first)
		{
			first = false;
			return false;
		}
		
		if(spinnerArrayAdapter != null && spinnerArrayAdapter.getCount() > position)
		{
		    MyApplication.display_text = spinnerArrayAdapter.getItem(position).toString();
		
		    Cursor c = MyApplication.myDbHelper.getData(MyApplication.filter_text);
		    cursorAdapter.changeCursor(c);
		}
		
		return false;
	}
}
