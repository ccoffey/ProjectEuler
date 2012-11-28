package ie.cathalcoffey.android.projecteuler;

import java.util.Vector;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;

import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Spinner;

public class ProblemList extends Activity implements SearchView.OnQueryTextListener
{
	private SimplerCursorAdapter cursorAdapter;
	private SharedPreferences settings;
    private MenuItem loginlogout;
    private String queryText;
    private Spinner spinner;
    
    @Override
	public void onResume() 
	{
	    super.onResume();
	    
	    if(loginlogout != null)
	    {
		    if (settings.contains("username"))
	        	loginlogout.setTitle("Logout");
	        else
	        	loginlogout.setTitle("Login");
	    }
	    
	    TextView solved = (TextView)findViewById(R.id.solved);
	    
	    int[] counts = {0, 0};
	    if(MyApplication.myDbHelper != null)
	    	counts = MyApplication.myDbHelper.getSolvedCount();
	    
	    solved.setText(String.format("Solved %d of %d", counts[0], counts[1]));
	    
    	cursorAdapter.getFilter().filter("");
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
        settings = getSharedPreferences("euler", MODE_PRIVATE);

        //Create the search view
        SearchView searchView = new SearchView(getSupportActionBarContext());
        searchView.setQueryHint("Search for problems…");
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        
        menu.add("Search")
            .setIcon(R.drawable.abs__ic_search)
            .setActionView(searchView)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
        spinner = new Spinner(getSupportActionBarContext());
        spinner.setOnItemSelectedListener
        (
        		new org.holoeverywhere.widget.AdapterView.OnItemSelectedListener()
        		{

					@Override
					public void onItemSelected(org.holoeverywhere.widget.AdapterView<?> parent, View view, int position, long id) 
					{
						MyApplication.display_text = parent.getItemAtPosition(position).toString();
						
						Cursor c = MyApplication.myDbHelper.getData(MyApplication.filter_text);
						cursorAdapter.changeCursor(c);
					}

					@Override
					public void onNothingSelected(org.holoeverywhere.widget.AdapterView<?> arg0) 
					{
						
					}
				}
        );
        
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getSupportActionBarContext(), R.layout.simple_spinner_dropdown_item, new String[]{"All", "Solved", "Unsolved"});
        spinner.setAdapter(spinnerArrayAdapter);
        
        menu.add("Display")
            .setActionView(spinner)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        if (settings.contains("username"))
        	loginlogout = menu.add("Logout");
        
        else
        	loginlogout = menu.add("Login");
        
        Intent intent = new Intent(this, LoginLogout.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        loginlogout.setIntent(intent);
        loginlogout.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);	
            
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
	    
	    setContentView(R.layout.problemlist);
	    	    
	    ListView list = (ListView)findViewById(R.id.list);
	    
	    list.setTextFilterEnabled(true);
	    list.setFastScrollEnabled(true);
	    
	    MyApplication.myDbHelper = new MyDataBaseHelper(this);
	    MyApplication.myDbHelper.openDataBase(SQLiteDatabase.OPEN_READWRITE);
	    
        try 
        {
        	String[] from = new String[]{"_id", "title", "solvedby"};
    	    int[] to = new int[]{R.id.id, R.id.title, R.id.solved_by};

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
    				new OnItemClickListener() 
    				{
						public void onItemClick(AdapterView<?> arg0, View v, int position, long  _id) 
						{
							Intent intent = new Intent(getApplicationContext(), EulerActivity.class);
    		    			
    		    			Bundle bundle = new Bundle();
    		    			bundle.putLong("_id", position);
    		    		    bundle.putString("displayText", spinner.getSelectedItem().toString());
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
    	    TextView solved = (TextView)findViewById(R.id.solved);
    	    solved.setText(String.format("Solved %d of %d", counts[0], counts[1]));
 	    } 
        
        catch (Exception e) 
        {
 		    throw new Error("Unable to create database");
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
}
