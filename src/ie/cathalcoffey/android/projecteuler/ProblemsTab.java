package ie.cathalcoffey.android.projecteuler;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ProblemsTab extends Activity 
{
    SQLHelper sqlHelper;
	TextView output;

	public SharedPreferences prefs;
	public Editor editor;
	int mAppWidgetId;

	Cursor cursor;
	SimplerCursorAdapter cursorAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.problems);
	    
	    onResume();
	}
	
	@Override
	public void onPause() 
	{
		super.onPause();
		
		ListView listView = (ListView)findViewById(R.id.listview1);
		View v = listView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		
		editor.putInt("index", listView.getFirstVisiblePosition());
		editor.putInt("top", top);
		editor.commit();
	}
	  
	@Override
	public void onResume() 
	{
		super.onResume();
		
		Bundle extras = getIntent().getExtras();
	    if (extras != null) 
	    	mAppWidgetId = extras.getInt("mAppWidgetId");
	    
	    prefs = getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + mAppWidgetId, Context.MODE_PRIVATE);
	    editor = getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + mAppWidgetId, Context.MODE_PRIVATE).edit();
		    
		try
		{
			String[] from = new String[]{SQLHelper.ID, SQLHelper.TITLE, SQLHelper.SOLVED_BY};
		    int[] to = new int[]{R.id.id, R.id.title, R.id.solved_by};
	
		    sqlHelper = new SQLHelper(this);
		    cursor = getProblems();
			
		    cursorAdapter = new SimplerCursorAdapter(this, R.layout.row, cursor, from, to);
	
		    final ListView listView = (ListView)findViewById(R.id.listview1);
		    listView.setAdapter(cursorAdapter);
		    listView.setClickable(true);
		    
		    listView.setSelectionFromTop(prefs.getInt("index", 0), prefs.getInt("top", 0));
		    
		    listView.setOnItemClickListener
		    (
		        new OnItemClickListener() 
		        {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
					{
						editor.putInt("index", listView.getFirstVisiblePosition());
						View v = listView.getChildAt(0);
						int top = (v == null) ? 0 : v.getTop();
						editor.putInt("top", top);
						editor.commit();
						
						TextView txt = (TextView)parent.getChildAt(position-listView.getFirstVisiblePosition()).findViewById(R.id.id);
						
						Intent intent = new Intent(getApplicationContext(), ProblemViewer.class);
						
						Bundle bundle = new Bundle();
						bundle.putString("id", txt.getText().toString()); 
						intent.putExtras(bundle);
					    
						startActivity(intent);	
					}
		        }
		   );
		}
		
		catch(Exception e)
		{
		    Log.w("ProjectEuler", e.getMessage());
		}
		
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		
		sqlHelper.close();
	}

	private Cursor getProblems() 
	{
		Cursor cursor = null;
		try
		{
		    SQLiteDatabase db = sqlHelper.getReadableDatabase();
		    
		    if (prefs.getInt("show", 0) == 0)
		    {
			    cursor = db.rawQuery
			    (
		    		"SELECT problems._id, problems.id, problems.title, problems.solved_by, users.solved " + 
			        "FROM problems INNER JOIN users ON problems.[id] = users.[id] " +
		    		"WHERE users.username='" + prefs.getString("username", "") + "'",
		    		null
			    );
		    }
		    
		    else
		    {
		    	int show = prefs.getInt("show", 0);
		    	 
		    	cursor = db.rawQuery
			    (
		    		"SELECT problems._id, problems.id, problems.title, problems.solved_by, users.solved " + 
			        "FROM problems INNER JOIN users ON problems.[id] = users.[id] " +
		    		"WHERE users.username='" + prefs.getString("username", "") + "' AND users.solved="  + (show == 2 ? 0 : 1),
		    		null
			    );
		    }
		    startManagingCursor(cursor);
		}
		
		catch(Exception e)
		{
		    Log.w("ProjectEuler", e.getMessage());
		}
		
	    return cursor;
	}
}
