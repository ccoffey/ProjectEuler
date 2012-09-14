package ie.cathalcoffey.android;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

public class Configuration extends Activity 
{   
	int mAppWidgetId;
	RemoteViews remoteViews;
	AppWidgetManager appWidgetManager;
	
	SQLHelper sqlHelper;
	
	EulerAsyncTask asyncTask;
	
	@Override
	protected void onPause() 
	{
	    super.onPause();
	    
	    if (asyncTask != null)
	    	asyncTask.cancel(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
	    setContentView(R.layout.configure);
	    
	    Intent intent = getIntent();
	    Bundle extras = intent.getExtras();
	    if (extras != null) 
	    {
	        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	        appWidgetManager = AppWidgetManager.getInstance(this);
	        remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
	    }
	   
	   Button button = (Button)findViewById(R.id.button1);
	   
	   button.setOnClickListener
	   (
			  new OnClickListener() 
			  {
			      @Override
				  public void onClick(View arg0) 
				  {
			    	  asyncTask = (EulerAsyncTask) new EulerAsyncTask()
			          {
			    	      private String msg;
				    	  private int total;
				    	  
			              @Override
			              protected void onPreExecute()
			              {	
			                  progressDialog = new MyProgressDialog(Configuration.this);
			                  
			                  progressDialog.setTitle("Project Euler");
			                  progressDialog.setMessage("Attempting login");       
			                  progressDialog.setIndeterminate(false);
			                  
			                  progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			                  
			                  progressDialog.setCancelable(true);
			                  progressDialog.show();
			              }
			              
			              @Override
			              protected void update(int progress, String msg) 
			              {
			                  this.msg = msg;
			                  
			                  if(progress > 0)
			                	  progressDialog.setProgress(progress);
			                  
			                  publishProgress(progress);
			              }
			              
			              @Override
			              protected void onProgressUpdate(Object... i) 
			              {
			                  super.onProgressUpdate(i);
			                  
			                  if((Integer)i[0] == 1)
			                  {
			                      progressDialog.setMax(total);
		                	      progressDialog.setTextVisibility(View.VISIBLE);
			                  }
			                  
			                  progressDialog.setMessage(this.msg);
			              }

			              @Override
			              protected Object doInBackground(Object... params)
			              {
			            	  String username = ((EditText)findViewById(R.id.editText1)).getText().toString();
					    	  String password = ((EditText)findViewById(R.id.editText2)).getText().toString();
					    	    
			                  ProjectEuler pe = new ProjectEuler(Configuration.this);
			                  try 
			                  {
							      if(pe.login(username, password))
							      {
							    	  update(0, "Login successful");
							    	  Thread.sleep(1000);
							    	  
							    	  Object[] details = pe.getDetails();
							    	
							    	  String name = (String)details[0];
							    	  String level = (String)details[1];
							    	  total = (Integer)details[2];
							    	  int progress = (Integer)details[3];
							    	  String solved = (String)details[4];
							    	  	  
							          Editor editor = getSharedPreferences(MyAppWidgetProvider.class.toString() + "_" + mAppWidgetId, Context.MODE_PRIVATE).edit();
							          editor.putString("username", name);
							          editor.putString("password", password);
							          editor.putString("level", level);
							          editor.putString("solved", solved);
							          editor.putInt("progress", progress);
							          editor.putInt("total", total);
							          
							    	  remoteViews.setTextViewText(R.id.username, name);
							    	  remoteViews.setTextViewText(R.id.level, level);
							    	  remoteViews.setTextViewText(R.id.solved, solved);  
							    	  remoteViews.setProgressBar(R.id.progressBar, total, progress, false);
							    	    
							    	  Intent active = new Intent(getApplicationContext(), Tabs.class);
							  		  active.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							  		  active.putExtra("mAppWidgetId", mAppWidgetId);
							  		
							  		  Uri data = Uri.withAppendedPath(Uri.parse("ProjectEuler://widget/id/") ,String.valueOf(mAppWidgetId));
							  		  active.setData(data);
							  			
							  		  PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), mAppWidgetId, active, PendingIntent.FLAG_UPDATE_CURRENT);		
							  		  remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
							  		
							          appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);
							          
							          sqlHelper = new SQLHelper(getApplicationContext());
							          pe.getProblems(sqlHelper, this, total);
							          
							          update(0, "Finished");
							          Thread.sleep(3000);
							          progressDialog.dismiss();
							          
							          sqlHelper.close();
								        
							          String lastupdated = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date());
							          editor.putString("lastupdated", lastupdated);
							          editor.commit();
							        
						    	      Intent resultValue = new Intent();
									  resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
									  setResult(RESULT_OK, resultValue);
									  finish();
							      }
							      
							      else
							      {
							    	  update(0, pe.getErrorMessage());
							    	  Thread.sleep(3000);
							      }
							  } 
			                  
			                  catch (ClientProtocolException e) 
			                  {
								  e.printStackTrace();
							  } 
			                  
			                  catch (IOException e) 
			                  {
			                	  update(0, "Unable to connect to projecteuler.net, please check your internet connection.");
						    	  try 
						    	  {
									Thread.sleep(3000);
								  } 
						    	  
						    	  catch (InterruptedException e1) 
						    	  {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								  }
							  } 
			                  
			                  catch (InterruptedException e) 
			                  {
								// TODO Auto-generated catch block
								e.printStackTrace();
							  }
			                  
			                  return true;
			              }

			              @Override
			              protected void onPostExecute(Boolean result)
			              {
			                  progressDialog.dismiss();
			              }
			          }.execute();
				  }
	          }
	    );
	}
}
