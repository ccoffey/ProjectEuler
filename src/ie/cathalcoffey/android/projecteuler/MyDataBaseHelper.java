package ie.cathalcoffey.android.projecteuler;

import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProblem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyDataBaseHelper extends SQLiteOpenHelper
{	 
    private static String DB_PATH = "/data/data/ie.cathalcoffey.android.projecteuler/databases/";
    private static String DB_NAME = "euler.db";
    private SQLiteDatabase myDataBase; 
    private final Context context;
	private boolean cancel = false;
	
    public MyDataBaseHelper(Context context) 
    {
    	super(context, DB_NAME, null, 1);
        this.context = context; 
    }	

    public int[] getSolvedCount()
    {	
    	String query;
    	Cursor c;
    	
    	query = "SELECT count(_id) FROM data WHERE solved = 1";
        c = myDataBase.rawQuery(query, new String[]{});
        c.moveToFirst();
        int solved = c.getInt(0);
        c.close();
        
        query = "SELECT count(*) FROM data";
        c = myDataBase.rawQuery(query, new String[]{});
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        
        return new int[]{solved, count};
    }
    
    public Cursor getData(String constraint)
    {	
    	if (constraint == null || constraint == "")
    		return getData();
    	
    	String query;
    	
    	if (MyApplication.display_text != null && MyApplication.display_text.equals("All"))
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE ('PROBLEM ' || _id ) LIKE ? OR title LIKE ?";
    	else if (MyApplication.display_text != null && MyApplication.display_text.equals("Solved"))
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE solved = 1 AND (('PROBLEM ' || _id ) LIKE ? OR title LIKE ?)";
    	else
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE solved = 0 AND (('PROBLEM ' || _id ) LIKE ? OR title LIKE ?)";
        
        return myDataBase.rawQuery(query, new String[]{"%" + constraint + "%", "%" + constraint + "%"});
    }
    
    public Cursor getData()
    {		
        String query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data";
        
        if(MyApplication.display_text != null)
        {
	        if (MyApplication.display_text.equals("Solved"))
	        	query += " WHERE solved = 1";
	        
	        if (MyApplication.display_text.equals("Unsolved"))
	        	query += " WHERE solved = 0";
        }
        
        return myDataBase.rawQuery(query, new String[]{});
    }
 
    private void copyDataBase() throws IOException
    {
    	Decompress.unzip(context.getAssets().open("assets.zip"), "/data/data/ie.cathalcoffey.android.projecteuler/");
    }
 
    public boolean checkDataBase()
    {
    	File file = context.getDatabasePath(DB_NAME);
    	return file.exists();	
    }
    
    public void openDataBase(int mode) throws SQLException
    {
    	String myPath = DB_PATH + DB_NAME;
    	
    	File file = context.getDatabasePath(DB_NAME);
    	if(!file.exists())
    	{    	    
        	try 
        	{
    			copyDataBase();
    		} 
        	
        	catch (IOException ioe) 
        	{
        		throw new Error("Error copying database");
        	}
    	}
    	
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | mode);
    }
 
    @Override
	public synchronized void close() 
    {
    	super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
 
	}

	public synchronized void updateProblems(ProjectEulerClient pec, ArrayList<EulerProblem> problems, boolean install) 
	{
		for(EulerProblem ep : problems)
		{
			if(MyApplication.cancelUpdater)
			{
				return;
			}
			
			if(install)
			{
				NotificationManager notificationManager = (NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
				
				NotificationCompat.Builder builder = new  NotificationCompat.Builder(context);
			    
				Intent intent = new Intent(context, ProblemList.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );

				builder.setContentIntent(contentIntent)
			    .setSmallIcon(R.drawable.ic_notification)
			    .setWhen(System.currentTimeMillis())
			    .setAutoCancel(true)
			    .setContentTitle("Updating problem set")
			    .setContentText("Updating problem: " + ep.id + " of " + problems.size());
				Notification notification = builder.build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;	
				
				notificationManager.notify(1, notification);
			}
			
			if(cancel)
				return;
			
			myDataBase.beginTransaction();
			
			long last_updated = getLastModified(ep.id);
			if(last_updated != -1)
			{
				ContentValues args = new ContentValues();
				args.put("solvedby", ep.solved_by);
			    args.put("solved", ep.solved_flag);
			    
			    if(last_updated < ep.date_last_update)
			    {	
					args.put("title", ep.description);
					args.put("published", ep.date_published);
					args.put("updated", ep.date_last_update);
				    args.put("answer", ep.answer);
			    }
			    
				myDataBase.update("data", args, "_id = ?", new String[]{"" + ep.id});
			}
			
			else
			{
				if(install)
				{	
					ContentValues args = new ContentValues();
					args.put("_id", ep.id);
					args.put("title", ep.description);
					args.put("published", ep.date_published);
					args.put("updated", ep.date_last_update);
					args.put("solvedby", ep.solved_by);
				    args.put("solved", ep.solved_flag);
				    
				    String html;
					try 
					{
						html = pec.getProblem(ep.id).html();
						Document soup = Jsoup.parse(html);
						
						for(Element img : soup.select("img"))
						{
							if(img.hasAttr("src"))
							{
								String src = img.attr("src");
								if (src.startsWith("http://projecteuler.net/"))
					                src = src.substring("http://projecteuler.net/".length());
								
								File f = new File("/data/data/ie.cathalcoffey.android.projecteuler/" + src);
								if(!f.exists())
								{
								    f.getParentFile().mkdirs();
								    
								    InputStream input = new BufferedInputStream(new URL("http://projecteuler.net/" + src).openStream());
						            OutputStream output = new FileOutputStream("/data/data/ie.cathalcoffey.android.projecteuler/" + src);
	
						            byte data[] = new byte[1024];
						            int count;
						            while ((count = input.read(data)) != -1) 
						                output.write(data, 0, count);
	
						            output.flush();
						            output.close();
						            input.close();
								}
							}
						}
						
					    args.put("html", html);
					} 
					
					catch (Exception e)
					{
						Log.w("Error", e.getMessage());
						
				        return;
					}
				    
				    args.put("answer", ep.answer);
				    
				    myDataBase.insert("data", null, args);
				}
			}
			
		    myDataBase.setTransactionSuccessful();
	        myDataBase.endTransaction();
		}
	}

	public long getLastModified(long _id) 
	{
	   Cursor cursor = myDataBase.rawQuery("select updated from data where _id=?", new String[] { "" + _id });
	   long lastModified = -1;
	   if(cursor.getCount() > 0)
	   {
		   cursor.moveToFirst();
		   lastModified = cursor.getLong(0);
	   }
	   cursor.close();
	   return lastModified;
	}
	
	public synchronized void solve(String id) 
	{
		myDataBase.beginTransaction();
		
		ContentValues args = new ContentValues();
	    args.put("solved", 1);
	    
		myDataBase.update("data", args, "_id = ?", new String[]{id});
		
		myDataBase.setTransactionSuccessful();
		myDataBase.endTransaction();
	}
	
	public synchronized void updateSolved() 
	{
        myDataBase.beginTransaction();
		
        ContentValues args = new ContentValues();
	    args.put("solved", 0);
	    
		myDataBase.update("data", args, null, null);
		
        myDataBase.setTransactionSuccessful();
        myDataBase.endTransaction();
	}

	public void kill() 
	{
	    cancel = true;	
	}
}