package ie.cathalcoffey.android.projecteuler;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class PageFragment extends Fragment 
{
	WebView webView1;
	static ScrollView solve;
	static ImageView img;
	Context context;
	MyApplication global;
	
	private SharedPreferences settings;
	
    private static class LongOperation extends AsyncTask<String, Void, String> 
	{	   
		  String username, password, html;
		  Drawable drawable;
		  FragmentActivity fragmentActivity;
		  
		  public LongOperation(FragmentActivity fragmentActivity)
		  {
			  this.fragmentActivity = fragmentActivity;
		  }
		
		  @Override
	      protected void onPostExecute(String result) 
		  {
			  img = (ImageView) solve.findViewById(R.id.imageView1);
			  img.setOnClickListener
			  (
					new OnClickListener() 
					{
				        @Override
				        public void onClick(View v) 
				        {
				        	img.setImageResource(R.drawable.loading);
				        	  
				        	new LongOperation(fragmentActivity).execute(new String[]{username, password, html});
				        }
				    }
			  );
			    
			  img.setImageDrawable(drawable);
	      }
		  
		  
	      @Override
	      protected String doInBackground(String... params) 
	      {
	    	    username = params[0];
				password = params[1];
				html = params[2];
				String captcha = "";
				
				MyApplication.pec = new ProjectEulerClient();
				
				try 
				{
					MyApplication.pec.login(username, password);
					
					Document soup = Jsoup.parse(MyApplication.pec.quickGet("http://projecteuler.net/minimal=captcha"));
					
					String src = soup.getElementsByTag("img").attr("src").toString();
					
					drawable = MyApplication.pec.loadImageFromWeb("http://projecteuler.net/" + src);
				} 
				
				catch (ClientProtocolException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return captcha;
	      }
	}
	
	public boolean solving = false;
	public boolean solved = false;
	private View view;
	private String html;
	
	public static PageFragment newInstance(long _id, String title, long published, long updated, long solvedby, boolean solved, String html, String answer)
	{
		PageFragment pageFragment = new PageFragment();
		pageFragment.setRetainInstance(true);
		
		Bundle bundle = new Bundle();
		bundle.putLong("_id", _id);
		bundle.putString("title", title);
		bundle.putLong("published", published);
		bundle.putLong("updated", updated);
		bundle.putLong("solvedby", solvedby);
		bundle.putBoolean("solved", solved);
		bundle.putString("html", html);
		bundle.putString("answer", answer);
		pageFragment.setArguments(bundle);
		
		pageFragment.solved = solved;
		
		return pageFragment;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	public void flip()
	{		
	    if (view != null)
	    {			
	    	webView1 = (WebView)view.findViewById(R.id.webView1);
	    	solve = (ScrollView)view.findViewById(R.id.solve);
			
		    Date published_date = new Date(getArguments().getLong("published") * 1000);
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
			String published = formatter.format(published_date);
			
			DecimalFormat commaFormatter = new DecimalFormat("#,###");
			String solvedby = "Solved by " + commaFormatter.format(getArguments().getLong("solvedby")) + " Eulerians";
			
			webView1.getSettings().setJavaScriptEnabled(true);
				
		    html = "<html bgcolor='#fff3f3f3'><head><style type='text/css'>img{max-width: 100%}</style></head><body><br><div style='color:#6B4E3D; font-weight: bold'>" + published  + "</div>" + "<div style='color:#666; font-weight: bold; font-size: 80%'>" + solvedby + "</div><br>" + "<div style='border-top: solid 1px #DDD; border-bottom: solid 1px #AAA; border-left: solid 1px #DDD; border-right: solid 1px #AAA; padding: 10px;'>";
			if (solving)
			{
				if (solved)
				{
			        html += "<p>You have already solved this problem.</p><p>Answer: <b>" + getArguments().getString("answer") + "</b></p>";
				
					webView1.setVisibility(View.VISIBLE);
					solve.setVisibility(View.GONE);
				}
				
				else
				{
					SharedPreferences settings = this.getActivity().getSharedPreferences("euler", this.getActivity().MODE_PRIVATE);
					
					if(!settings.contains("username"))
					{
						html += "<p>You need to be logged in to solve problems.</p>";
						html += "<center><img src='login.png'></img></center>";
						html += "</div></body></html>";
						
						webView1.setVisibility(View.VISIBLE);
						solve.setVisibility(View.GONE);
						
						webView1.loadDataWithBaseURL("file:///data/data/ie.cathalcoffey.android.projecteuler/", html, "text/html", "utf-8", null);
					}
					
					else
					{	
						String username = settings.getString("username", "");
						String password = settings.getString("password", "");
						new LongOperation(getActivity()).execute(new String[]{username, password, html});
						
						webView1.setVisibility(View.GONE);
						solve.setVisibility(View.VISIBLE);						
					}
				   
					return;
				}
			}
			
			else
			{
				html += getArguments().getString("html");
				
				solve.setVisibility(View.GONE);
				webView1.setVisibility(View.VISIBLE);
			}
		
			html += "</div></body></html>";
			
			webView1.loadDataWithBaseURL("file:///data/data/ie.cathalcoffey.android.projecteuler/", html, "text/html", "utf-8", null);
	    }
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
         super.onCreate(savedInstanceState);
        
	     context = getActivity();
	     
	     global = (MyApplication)getActivity().getApplication();
			
		 settings = context.getSharedPreferences("euler", Context.MODE_PRIVATE);
         settings.edit();
        
         setRetainInstance(true);
    }
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{	
		view = inflater.inflate(R.layout.fragment, container, false);
		
		Button b = (Button)view.findViewById(R.id.button1);
		b.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{
						long _id = getArguments().getLong("_id");
						TextView textView_guess = (TextView)view.findViewById(R.id.guess);
						TextView textView_confirm = (TextView)view.findViewById(R.id.confirm);
						
						if(MyApplication.solve_opt == null)
						{
						    MyApplication.solve_opt = new SolveOperation(getActivity());
						    MyApplication.solve_opt.execute(new String[]{"" + _id, textView_guess.getText().toString(), textView_confirm.getText().toString(), html});
						}
					}
				}
		);
		
		flip();
		
		return view;
	}
	
	public class SolveOperation extends AsyncTask<String, Void, String> 
	{
		  SolvingDialogFragment dialog;
		  String progressMsg;
		  boolean success;
		  boolean completed;
		  FragmentActivity fragmentActivity;
		  String id;
		  String html;
		  
		  public SolveOperation(FragmentActivity fragmentActivity)
		  {
			  this.fragmentActivity = fragmentActivity;
			  
			  dialog = new SolvingDialogFragment();
			  dialog.setCancelable(false);
			  dialog.show(fragmentActivity.getSupportFragmentManager(), "");
		  }
		
	      @Override
	      protected String doInBackground(String... params) 
	      {
	    	    success = false;
	    	  
	    	    id = params[0];
				String guess = params[1];
				String confirm = params[2];
				html = params[3];
				
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
				
			    ProjectEulerClient pec = global.pec;
			    	    	        
				this.progressMsg = "Login successful";
				publishProgress();
		    	
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
				  
				this.progressMsg = "Submitting answer";
				publishProgress();
				
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
				
				try 
				{
					success = pec.solve(id, guess, confirm);
				} 
				
				catch (ClientProtocolException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				
				catch (IOException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if(success)
		            this.progressMsg = "Congratulations, the answer you gave to problem " + id + " is correct.";
				else
					this.progressMsg = pec.solve_msg;
				
				completed = true;
				publishProgress();
				
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
			    
	            return null;
	      }      

	      @Override
	      protected void onPostExecute(String result) 
	      {     
			  String username = settings.getString("username", "");
			  String password = settings.getString("password", "");
				
			  img = (ImageView) solve.findViewById(R.id.imageView1);
			  if(img != null)
			  {
	    	      img.setImageResource(R.drawable.loading);  
	              new LongOperation(fragmentActivity).execute(new String[]{username, password, html});
			  }
	      }

	      @Override
	      protected void onPreExecute() 
	      {
	    	  
	      }

	      @Override
	      protected void onProgressUpdate(Void... values) 
	      {
	    	  try
	    	  {
		    	  if(dialog != null)
		    	  {
			    	  if(completed)
			    		  dialog.completed();
			    	  
			    	  MyApplication.solve_opt.progressMsg = progressMsg;
			    	  
			    	  if(success)
			    	  {
	        	          MyApplication.solve_opt.dialog.completed();
	        	          MyApplication.myDbHelper.solve(id);
			    	  }
			    	  
			    	  dialog.setMessage(progressMsg);
		    	  }
	    	  }
	    	  
	    	  catch(Exception e)
	    	  {
	    		  Log.e("Exception", e.getMessage());
	    	  }
	      }
	}
}
