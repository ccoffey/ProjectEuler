package ie.cathalcoffey.android.projecteuler;

import ie.cathalcoffey.android.projecteuler.R.color;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ToggleButton;

import android.widget.ImageView;
import android.widget.Toast;

import org.holoeverywhere.widget.TextView;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

public class PageFragment extends Fragment 
{
	WebView webView1;
	static LinearLayout solveProblem;
	static LinearLayout viewProblem;
	static ImageView img;
	Context context;
	MyApplication global;
	AlertDialog dialog = null;
	  
	private Button postStatusUpdateButton;
	  private LoginButton loginButton;
	  private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";
	  private ProfilePictureView profilePictureView;
	  private TextView greeting;
	  private TextView problemText;
	  	  
	  private void onClickPostStatusUpdate() {
	      performPublish(PendingAction.POST_STATUS_UPDATE);
	  }
	  
	  private UiLifecycleHelper uiHelper;
	  
	  private Session.StatusCallback callback = new Session.StatusCallback() {
	      @Override
	      public void call(Session session, SessionState state, Exception exception) {
	          onSessionStateChange(session, state, exception);
	      }
	  };
	  
	  @Override
	public void onResume() {
	      super.onResume();
	      uiHelper.onResume();

	      updateUI();
	  }

	  @Override
	public void onSaveInstanceState(Bundle outState) {
	      super.onSaveInstanceState(outState);
	      uiHelper.onSaveInstanceState(outState);

	      outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	  }

	  @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      uiHelper.onActivityResult(requestCode, resultCode, data);
	  }

	  @Override
	  public void onDestroy() {
	      super.onDestroy();
	      uiHelper.onDestroy();
	  }

	  private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	      if (pendingAction != PendingAction.NONE &&
	              (exception instanceof FacebookOperationCanceledException ||
	              exception instanceof FacebookAuthorizationException)) {
	              new AlertDialog.Builder(getActivity())
	                  .setTitle(R.string.cancelled)
	                  .setMessage(R.string.permission_not_granted)
	                  .setPositiveButton(R.string.ok, null)
	                  .show();
	          pendingAction = PendingAction.NONE;
	      } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
	          handlePendingAction();
	      }
	      updateUI();
	  }
	  
	  private void updateUI() {
		  Session session = Session.getActiveSession();
	      boolean enableButtons = (session != null && session.isOpened());

	      postStatusUpdateButton.setEnabled(enableButtons);

	      if (enableButtons && user != null) {
	          profilePictureView.setProfileId(user.getId());
	          greeting.setText(user.getName());
	      } 
	      
	      else {
	          profilePictureView.setProfileId(null);
	          greeting.setText(null);
	      }
	  }
	  
	  private PendingAction pendingAction = PendingAction.NONE;
	  private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	  private GraphUser user;
	  
	  private enum PendingAction {
	      NONE,
	      POST_PHOTO,
	      POST_STATUS_UPDATE
	  }
	  
	  private void postStatusUpdate() {
	      if (user != null && hasPublishPermission()) {
	          String message = problemText.getText().toString();
	          Request request = Request
	                  .newStatusUpdateRequest(Session.getActiveSession(), message, new Request.Callback() {
	                      @Override
	                      public void onCompleted(Response response) {
	                    	  if(dialog != null)
	                    	  {
	                    		  dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
	                    		  dialog.setMessage("Facebook status update successful!");
	                    	  }
	                      }
	                  });
	          request.executeAsync();
	          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	          
	          builder.setMessage("Posting status update to Facebook.");
	          builder.setCancelable(false);
          	  builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          		  
  	             public void onClick(DialogInterface dialog, int id) {
  	            	dialog.dismiss();
  	             }
  	          });
          	
          	  // Create the AlertDialog
          	  dialog = builder.create();
          	  dialog.show();
              dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
	      } 
	      
	      else {
	          pendingAction = PendingAction.POST_STATUS_UPDATE;
	      }
	  }
	  
	  @SuppressWarnings("incomplete-switch")
	  private void handlePendingAction() {
	      PendingAction previouslyPendingAction = pendingAction;
	      // These actions may re-set pendingAction if they are still pending, but we assume they
	      // will succeed.
	      pendingAction = PendingAction.NONE;

	      switch (previouslyPendingAction) {
	          case POST_STATUS_UPDATE:
	              postStatusUpdate();
	              break;
	      }
	  }
	  
	  private boolean hasPublishPermission() {
	      Session session = Session.getActiveSession();
	      return session != null && session.getPermissions().contains("publish_actions");
	  }

	  private void performPublish(PendingAction action) {
	      Session session = Session.getActiveSession();
	      if (session != null) {
	          pendingAction = action;
	          if (hasPublishPermission()) {
	              // We can do the action right away.
	              handlePendingAction();
	          } else {
	              // We need to get new permissions, then complete the action when we get called back.
	              session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
	          }
	      }
	  }

    private static class LongOperation extends AsyncTask<String, Void, String> 
	{	   
		  String username, password, html;
		  Drawable drawable;
		  Activity fragmentActivity;
		  
		  public LongOperation(Activity fragmentActivity)
		  {
			  this.fragmentActivity = fragmentActivity;
		  }
		
		  @Override
	      protected void onPostExecute(String result) 
		  {
			  img = (ImageView) solveProblem.findViewById(R.id.imageView1);
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
					if(src.contains("captcha"))
					{
					    drawable = MyApplication.pec.loadImageFromWeb("http://projecteuler.net/" + src);
					}
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
	private ViewGroup container;
	
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
	  public void onPause() {
	      super.onPause();
	      uiHelper.onPause();
	  }
	
	
	public void flip()
	{		
	    if (view != null)
	    {				    	
	    	webView1 = (WebView)view.findViewById(R.id.webView1);
	    	webView1.setOnTouchListener(new View.OnTouchListener() 
	    	{
	    	    public boolean onTouch(View v, MotionEvent event) 
	    	    {
	    	        return (event.getAction() == MotionEvent.ACTION_MOVE);
	    	    }
	    	});
	    	
	    	viewProblem = (LinearLayout)view.findViewById(R.id.viewProblem);
	    	solveProblem = (LinearLayout)view.findViewById(R.id.solveProblem);
	    	TextView brag0 = (TextView)view.findViewById(R.id.brag0);
	    	LinearLayout brag1 = (LinearLayout)view.findViewById(R.id.brag1);
	    	TextView brag2 = (TextView)view.findViewById(R.id.brag2);
	    	LinearLayout brag3 = (LinearLayout)view.findViewById(R.id.brag3);
	    	
		    Date published_date = new Date(getArguments().getLong("published") * 1000);
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
			String published = formatter.format(published_date);
			
	    	TextView textView_published = (TextView)view.findViewById(R.id.published);
	    	textView_published.setText(published);
	    	
			DecimalFormat commaFormatter = new DecimalFormat("#,###");
			String solvedby = "Solved by " + commaFormatter.format(getArguments().getLong("solvedby")) + " Eulerians";
			
			TextView textView_solvedby = (TextView)view.findViewById(R.id.solvedby);
	    	textView_solvedby.setText(solvedby);
	    	
			webView1.getSettings().setJavaScriptEnabled(true);
			
		    html = "<html><head><style type='text/css'>img{max-width: 90%}</style></head><body bgcolor='#fbfbfb'><div style='border-top: solid 1px #DDD; border-bottom: solid 1px #DDD; border-left: solid 1px #DDD; border-right: solid 1px #DDD; padding: 10px;'>";
			if (solving)
			{
				if (solved)
				{
			        TextView answer = (TextView)view.findViewById(R.id.alreadySolved);
			        answer.setText(Html.fromHtml("You have already solved this problem.<br/><br/>Answer: <b>" + getArguments().getString("answer") + "</b>"));
			        
			        TextView problemText = (TextView)view.findViewById(R.id.problemText);
			        String title = getArguments().getString("title");
			        title = title.trim();
			        if(!title.endsWith("."))
			        	title = title + ".";
			        
			        problemText.setText(Html.fromHtml("I just solved projecteuler.net, Problem " +  getArguments().getLong("_id") + ": " + title + " http://projecteuler.net/problem=" + getArguments().getLong("_id") + "<br/><br/>Get the ProjectEuler Android app here: http://tinyurl.com/bmgbk7g"));
			        
					viewProblem.setVisibility(View.VISIBLE);
					brag0.setVisibility(View.VISIBLE);
					brag1.setVisibility(View.VISIBLE);
					brag2.setVisibility(View.VISIBLE);
					brag3.setVisibility(View.VISIBLE);
					webView1.setVisibility(View.GONE);
					solveProblem.setVisibility(View.GONE);
				}
				
				else
				{
					SharedPreferences settings = (SharedPreferences) this.getActivity().getSharedPreferences("euler", this.getActivity().MODE_PRIVATE);
					
					if(!settings.contains("username"))
					{
						html += "<p>You need to be logged in to solve problems.</p>";
							
						if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && ViewConfiguration.get(context).hasPermanentMenuKey()) || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) 
						    html += "<center><img src='login_old.png'></img></center>";
						else
							html += "<center><img src='login.png'></img></center>";
						
						html += "</div></body></html>";
						
						viewProblem.setVisibility(View.VISIBLE);
						solveProblem.setVisibility(View.GONE);
						brag0.setVisibility(View.GONE);
						brag1.setVisibility(View.GONE);
						brag2.setVisibility(View.GONE);
						brag3.setVisibility(View.GONE);
						webView1.setVisibility(View.VISIBLE);
						
						webView1.loadDataWithBaseURL("file:///data/data/ie.cathalcoffey.android.projecteuler/", html, "text/html", "utf-8", null);
					}
					
					else
					{	
						String username = settings.getString("username", "");
						String password = settings.getString("password", "");
						new LongOperation((Activity) getActivity()).execute(new String[]{username, password, html});
						
						viewProblem.setVisibility(View.GONE);
						brag0.setVisibility(View.GONE);
						brag1.setVisibility(View.GONE);
						brag2.setVisibility(View.GONE);
						brag3.setVisibility(View.GONE);
						webView1.setVisibility(View.VISIBLE);
						solveProblem.setVisibility(View.VISIBLE);						
					}
				   
					return;
				}
			}
			
			else
			{
				html += getArguments().getString("html");
				
				solveProblem.setVisibility(View.GONE);
				brag0.setVisibility(View.GONE);
				brag1.setVisibility(View.GONE);
				brag2.setVisibility(View.GONE);
				brag3.setVisibility(View.GONE);
				webView1.setVisibility(View.VISIBLE);
				viewProblem.setVisibility(View.VISIBLE);
			}
		
			html += "</div></body></html>";
			https://www.google.com/search?q=git+referencing+another+git+project&aq=f&oq=git+referencing+another+git+project&aqs=chrome.0.57j0.9155&sourceid=chrome&ie=UTF-8
			webView1.loadDataWithBaseURL("file:///data/data/ie.cathalcoffey.android.projecteuler/", html, "text/html", "utf-8", null);
	    }
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
         super.onCreate(savedInstanceState);
        
	     context = getActivity();
			
 	     if(MyApplication.settings == null)
 	    	MyApplication.settings = (SharedPreferences) context.getSharedPreferences("euler", Context.MODE_PRIVATE);
 	     
 	     uiHelper = new UiLifecycleHelper(getActivity(), callback);
	     uiHelper.onCreate(savedInstanceState);

	     if (savedInstanceState != null) {
	        String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
	        pendingAction = PendingAction.valueOf(name);
	     }
    }
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{	
		this.container = container;
		view = inflater.inflate(R.layout.fragment, container, false);
		
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.profilePicture);
	    greeting = (TextView) view.findViewById(R.id.greeting);
	    problemText = (TextView) view.findViewById(R.id.problemText);
	    
	    loginButton = (LoginButton) view.findViewById(R.id.login_button);
	    loginButton.setFragment(this);
	    loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
	        @Override
	        public void onUserInfoFetched(GraphUser user) {
	            PageFragment.this.user = user;
	            updateUI();
	            // It's possible that we were waiting for this.user to be populated in order to post a
	            // status update.
	            handlePendingAction();
	        }
	    });
	    
	    postStatusUpdateButton = (Button) view.findViewById(R.id.postStatusUpdateButton);
	    postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {
	            onClickPostStatusUpdate();
	        }
	    });
	    
		final long _id = getArguments().getLong("_id");
		
		Button b = (Button)view.findViewById(R.id.button1);
		b.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{
						EditText editText_guess = (EditText)view.findViewById(R.id.guess);
						EditText editText_confirm = (EditText)view.findViewById(R.id.confirm);
						
						if(MyApplication.solve_opt == null)
						{
						    MyApplication.solve_opt = new SolveOperation((Activity) getActivity());
						    MyApplication.solve_opt.execute(new String[]{"" + _id, editText_guess.getText().toString(), editText_confirm.getText().toString(), html});
						}
					}
				}
		);
		
		final ToggleButton star = (ToggleButton)view.findViewById(R.id.star);
        star.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	callOnClick(star, _id);
            }
        });
        star.setChecked(MyApplication.stars.containsKey(""+_id));
        
	    LinearLayout right = (LinearLayout)view.findViewById(R.id.right);
	    right.setOnClickListener(new View.OnClickListener() 
	    {
	        @Override
	        public void onClick(View view) 
	        {
	        	 star.toggle();
	        	 callOnClick(star, _id);
	        }
	    });
	    
		flip();
		
		return view;
	}
	
	public void callOnClick(ToggleButton star, long _id)
	{
		 if(MyApplication.settings == null || !MyApplication.settings.contains("username"))
      	 {
      		 Toast.makeText(context, "You must be logged in to star problems.", Toast.LENGTH_SHORT).show();
      		 star.setChecked(false);
      	 }
   	 
	   	 else
	   	 {
	   		 if(star.isChecked())
	   		 {
	   			 MyApplication.stars.put(""+_id, true);
	   		 }
	   		 
	   		 else
	   		 {
	   			 if(MyApplication.stars.containsKey(""+_id))
	   				 MyApplication.stars.remove(""+_id);
	   			 
	   			 if(MyApplication.display_text != null && MyApplication.display_text.equals(Label.Starred.toString()))
	   			 {
	   				/*
	   				if(container != null)
	   				{
		   				ViewPager pager = (ViewPager)container.findViewById(R.id.viewpager);
		   				if(pager != null)
		   				{
		   					int index = pager.getCurrentItem();
		   					MyApplication.fragments.remove(index);
		   					
		   					PagerAdapter mPagerAdapter  = new PagerAdapter(getSupportFragmentManager());
		   			        pager.setAdapter(mPagerAdapter);
		   				}
	   				}
	   				*/
	   			 }
	   		 }
	   	 }	
	}
	
	public class SolveOperation extends AsyncTask<String, Void, String> 
	{
		  SolvingDialogFragment dialog;
		  String progressMsg;
		  boolean success;
		  boolean completed;
		  Activity fragmentActivity;
		  String id;
		  String html;
		  
		  public SolveOperation(Activity fragmentActivity)
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
				
			    ProjectEulerClient pec = MyApplication.pec;
			    if(MyApplication.pec == null)
			    {
			    	MyApplication.pec = new ProjectEulerClient();
			    	pec = MyApplication.pec;
			    }
			         
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
				{
		            this.progressMsg = "Congratulations, the answer you gave to problem " + id + " is correct.";
		            
		            // A problem has just been solved, update counts.
		            int[] counts = MyApplication.myDbHelper.getSolvedCount();
		    		MyApplication.COUNT_SOLVED = counts[0];
		    		MyApplication.COUNT_ALL = counts[1];
				}
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
			  String username = MyApplication.settings.getString("username", "");
			  String password = MyApplication.settings.getString("password", "");
				
			  img = (ImageView) solveProblem.findViewById(R.id.imageView1);
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
