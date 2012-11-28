package ie.cathalcoffey.android.projecteuler;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

public class EulerActivity extends SherlockFragmentActivity implements SolvingDialogFragment.NoticeDialogListener
{
	private FragmentStatePagerAdapter mPagerAdapter;
    private ViewPager pager;
    private MenuItem solve;
    private long _id = 0;
	private int position;
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewpager_layout);
		
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
        try 
        {
    		Intent i = getIntent();
    		Bundle extras = i.getExtras();
    		if(extras != null)
    		{
    			_id = extras.getLong("_id");
    		    extras.getString("constraint");
    		    MyApplication.display_text = extras.getString("displayText");
    		}
    	
    		initialisePaging();
    		
    		myOnPageSelected((int)_id);
 	    } 
        
        catch (Exception e) 
        {
 		    throw new Error(e.getMessage());
 	    }
	}

	private void myOnPageSelected(int position) 
	{
		this.position = position;
		PageFragment pf = (PageFragment)MyApplication.fragments.get(position);
		Bundle b = pf.getArguments();
		
		TextView solved = (TextView)findViewById(R.id.solved);
		solved.setText(b.getBoolean("solved") ? "Solved": "Unsolved");
		
		if (solve != null)
		{
			if (pf.solving)
        	    solve.setIcon(R.drawable.ic_read);
        	else
        		solve.setIcon(R.drawable.ic_solve);
		}
	}

	private void initialisePaging() 
	{				
		this.mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager());
		
		pager = (ViewPager)super.findViewById(R.id.viewpager);
		pager.setAdapter(this.mPagerAdapter);
		
		TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
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
		
		long _id = 0;
		Bundle extras = getIntent().getExtras();
	    if (extras != null) 
	        _id = extras.getLong("_id");  
	    
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
