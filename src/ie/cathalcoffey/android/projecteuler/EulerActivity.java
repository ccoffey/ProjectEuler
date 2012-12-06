package ie.cathalcoffey.android.projecteuler;

import java.util.Vector;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import android.util.Log;

public class EulerActivity extends Activity implements SolvingDialogFragment.NoticeDialogListener
{
	private TitlePageIndicator titleIndicator;
	private FragmentStatePagerAdapter mPagerAdapter;
    private ViewPager pager;
    private MenuItem solve;
    private long _id = 0;
	private int position;
	private String queryText;
	
    @Override
	public void onSaveInstanceState(Bundle outState) 
    {
		super.onSaveInstanceState(outState);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
        solve = menu.add("Solve");
        solve.setIcon(R.drawable.ic_solve);
        solve.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	  if (item.getItemId() == android.R.id.home) 
    	  {
              finish();
              overridePendingTransition(0, 0);
              
              return true;
          }
    	  
          // Handle item selection
          if(item == solve)
          {
        	  PageFragment pf = (PageFragment)MyApplication.fragments.get(pager.getCurrentItem());
        	  if(pf != null)
        	  {
	  			  pf.solving = !pf.solving;
	        	  pf.flip();
	        	  
	        	  if (pf.solving)
	        	  {
	        	      item.setIcon(R.drawable.ic_read);
	        	      item.setTitle("View");
	        	  }
	        	  
	        	  else
	        	  {
	        		  item.setIcon(R.drawable.ic_solve);
	        		  item.setTitle("Solve");
	        	  }
	        	  
	        	  return true;
        	  }
          }
         
         return false;
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("UPDATE_COMPLETE"));
    }
    
    private class Receiver extends BroadcastReceiver 
	{
		 @Override
		 public void onReceive(Context arg0, Intent arg1) 
		 {
		     int last_id = 1;
		     if(MyApplication.fragments.size() > 0)
		     {
		    	 PageFragment pf = (PageFragment)MyApplication.fragments.lastElement();
		    	 last_id = (int)pf.getArguments().getLong("_id");
		     }
		     
             Cursor cursor = MyApplication.myDbHelper.getData(queryText);
			
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
			    
			     if((int)_id1 > last_id)
			     {
			         MyApplication.fragments.add(PageFragment.newInstance(_id1, title, published, updated, solvedby, solved, html, answer));
			         last_id = (int)_id1;
			     }
			 }
			
			 cursor.close();
			
			 titleIndicator.notifyDataSetChanged();
			 myOnPageSelected((int)_id);
		 }
	}
    
	@Override
	public void onDestroy()
	{
	    super.onDestroy();
	}
	
    @Override
	public void onBackPressed() 
	{
    	if(MyApplication.solve_opt != null)
		{
		    MyApplication.solve_opt.cancel(true);
		    MyApplication.solve_opt = null;
		}
    	
	    this.finish();
	    overridePendingTransition(0, 0);
	}
	
	Receiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewpager_layout);
		
		receiver = new Receiver();
	    
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
        try 
        {
    		Intent i = getIntent();
    		if(i != null)
    		{
	    		Bundle extras = i.getExtras();
	    		if(extras != null)
	    		{
	    			if(extras.containsKey("_id") && extras.containsKey("displayText") && extras.containsKey("constraint"))
	    			{
	    				_id = extras.getLong("_id");
	    		        MyApplication.display_text = extras.getString("displayText");
		    			queryText = extras.getString("constraint");
	    			}
	    		}
    		}
    		
    	    if(MyApplication.settings == null)
    	        MyApplication.settings = getSharedPreferences("euler", MODE_PRIVATE);
    	    
    	    if(MyApplication.prefEditor == null)
    	        MyApplication.prefEditor = MyApplication.settings.edit();
    	    
    	    if(MyApplication.myDbHelper == null)
    	    {
    	        MyApplication.myDbHelper = new MyDataBaseHelper(this);
    	        MyApplication.myDbHelper.openDataBase(SQLiteDatabase.OPEN_READWRITE);
    	    }
    	    
    		if(MyApplication.fragments == null)
    		{
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
    		}
    		
    		initialisePaging();
    		myOnPageSelected((int)_id);
 	    } 
        
        catch (Exception e) 
        {
 		    Log.e("BUG", e.getMessage());
 	    }
	}

	private void myOnPageSelected(int position) 
	{
		this.position = position;
		if(MyApplication.fragments != null && MyApplication.fragments.size() >= position)
		{
			PageFragment pf = (PageFragment)MyApplication.fragments.get(position);
			Bundle b = pf.getArguments();
			
			if(b != null)
			{
				TextView solved = (TextView)findViewById(R.id.solved);
				if(solved != null)
				    solved.setText(b.getBoolean("solved") ? "Solved": "Unsolved");
				
				if (solve != null)
				{
					if (pf.solving)
		        	    solve.setIcon(R.drawable.ic_read);
		        	else
		        		solve.setIcon(R.drawable.ic_solve);
				}
			}
		}
	}

	private void initialisePaging() 
	{				
		this.mPagerAdapter  = new PagerAdapter(getSupportFragmentManager());
		
		pager = (ViewPager)findViewById(R.id.viewpager);
		if(pager != null)
	        pager.setAdapter(mPagerAdapter);
		
		titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
		if(titleIndicator != null)
		{
			titleIndicator.setViewPager(pager);
		
			titleIndicator.setOnPageChangeListener
			( 
					new OnPageChangeListener()
					{
						@Override
						public void onPageScrollStateChanged(int arg0) 
						{
	
						}
	
						@Override
						public void onPageScrolled(int arg0, float arg1, int position) 
						{
	
							
						}
	
						@Override
						public void onPageSelected(int position) 
						{
							myOnPageSelected(position);
						}
					}
			);
		}
		
		if(pager != null)
		    pager.setCurrentItem((int)_id);
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		if(MyApplication.solve_opt != null)
		{
		    MyApplication.solve_opt.cancel(true);
		    MyApplication.solve_opt = null;
		}
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) 
	{
		if(MyApplication.solve_opt != null)
		{
		    MyApplication.solve_opt.cancel(true);
		    MyApplication.solve_opt = null;
		}
	}

	@Override
	public void solved() 
	{
		TextView solved = (TextView)findViewById(R.id.solved);
		if(solved != null)
		    solved.setText("Solved");
		
		PageFragment pf = (PageFragment)MyApplication.fragments.get(position);
		Bundle b = pf.getArguments();
		pf.solved = true;
		
		if(pf != null)
		    pf.flip();
		
		PageFragment newPf = PageFragment.newInstance(b.getLong("_id"), b.getString("title"), b.getLong("published"), b.getLong("updated"), b.getLong("solvedby"), true, b.getString("html"), b.getString("answer"));
		newPf.solving = true;
		MyApplication.fragments.set(position, newPf);
	}
}
