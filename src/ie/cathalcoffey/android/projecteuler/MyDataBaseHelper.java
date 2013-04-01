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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

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
    	super(context, DB_NAME, null, 5);
        this.context = context; 
    }	

    public int[] getSolvedCount()
    {	
    	String query = "SELECT solved, COUNT(_id) FROM data GROUP BY solved";
    	Cursor c = myDataBase.rawQuery(query, new String[]{});
        
    	int solved = 0;
    	int unsolved = 0;
    	
    	while(c.moveToNext())
    	{
            switch(c.getInt(0))
            {
                case 0:
            	    unsolved = c.getInt(1);
            	    break;
            	    
                case 1:
            	    solved = c.getInt(1);
            	    break;
            }
    	}
    	
    	c.close();
        return new int[]{solved, solved+unsolved};
    }
    
    public Cursor getData(String constraint)
    {	
    	if (constraint == null || constraint == "")
    		return getData();
    	
    	String query;
    	
    	if (MyApplication.display_text != null && MyApplication.display_text.equals(Label.All.toString()))
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE ('PROBLEM ' || _id ) LIKE ? OR title LIKE ?";
    	else if (MyApplication.display_text != null && MyApplication.display_text.equals(Label.Solved.toString()))
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE solved = 1 AND (('PROBLEM ' || _id ) LIKE ? OR title LIKE ?)";
    	else if (MyApplication.display_text != null && MyApplication.display_text.equals(Label.Starred.toString()))
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE _id IN (" + toCommaList(MyApplication.stars) + ") AND (('PROBLEM ' || _id ) LIKE ? OR title LIKE ?)";
    	else
    		query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data WHERE solved = 0 AND (('PROBLEM ' || _id ) LIKE ? OR title LIKE ?)";
        
        return myDataBase.rawQuery(query, new String[]{"%" + constraint + "%", "%" + constraint + "%"});
    }

	public Cursor getData()
    {		
        String query = "SELECT _id, title, published, updated, solvedby, solved, html, answer FROM data";
        
        if(MyApplication.display_text != null)
        {
	        if (MyApplication.display_text.equals(Label.Solved.toString()))
	        	query += " WHERE solved = 1";
	        
	        else if (MyApplication.display_text.equals(Label.Unsolved.toString()))
	        	query += " WHERE solved = 0";
	        
	        else if (MyApplication.display_text.equals(Label.Starred.toString()))
	        	query += " WHERE _id IN (" + toCommaList(MyApplication.stars) + ")";
        }
        
        return myDataBase.rawQuery(query, new String[]{});
    }
 
    private String toCommaList(Hashtable<String, Boolean> stars) 
    {
    	if(stars.size() == 0)
    		return "";
    	
    	Set<String> keys = stars.keySet();
    	String[] sorted_keys = new String[keys.size()];
    	keys.toArray(sorted_keys);
    	Arrays.sort(sorted_keys);
    	
	    StringBuilder sb = new StringBuilder();
	    for(String id : sorted_keys)
	    {
	        sb.append(",");
	    	sb.append(id);
	    }
	    return sb.substring(1);
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

	public ArrayList<Long> getLastUpdated()
	{
		ArrayList<Long> last_updated = new ArrayList<Long>();
		Cursor cursor = myDataBase.rawQuery("select updated from data", null);
        while(cursor.moveToNext())
        {
	        last_updated.add(cursor.getLong(0));
        }
        cursor.close();
        return last_updated;	
	}
	
	public void updateProblems(ProjectEulerClient pec, ArrayList<EulerProblem> problems, boolean install, boolean userStarted)
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
    	
    	Intent intent = new Intent(context, PreferencesActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);        	
    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	builder.setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_notification)
        .setWhen(System.currentTimeMillis())
        .setAutoCancel(true)
        .setContentTitle("Updating problem set")
        .setContentText("Authenticating...");
    	
    	Notification notification = builder.build();
        
		ContentValues args = new ContentValues();
		
		long start, end;
		
		start = System.currentTimeMillis();
		
		// Step 1, update solved and solved_by
		for(EulerProblem ep : problems)
		{
			if(MyApplication.cancelUpdater)
				return;
	        
			if(userStarted)
			{
				builder.setContentText("Updating: Problem " + ep.id + " of " + problems.size());
		    	notification = builder.build();
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		        notificationManager.notify(1, notification);
			}
			
			args.put("solvedby", ep.solved_by);
		    args.put("solved", ep.solved_flag);
		    
		    myDataBase.beginTransaction();
			myDataBase.update("data", args, "_id = ?", new String[]{"" + ep.id});
			myDataBase.setTransactionSuccessful();
			myDataBase.endTransaction();
		}
		
		end = System.currentTimeMillis();
		Log.w("Euler upate: solved and solved_by", "" + (end-start) / 1000 + " seconds");
		
		if(install)
		{
			start = System.currentTimeMillis();
			
			// Step 2, figure out which problems have changed.
			ArrayList<Long> last_updated = getLastUpdated();
			int i;
			for(i = 0; i < Math.min(problems.size(), last_updated.size()); i++)
			{
				if(MyApplication.cancelUpdater)
					return;
				
				if(last_updated.get(i) < problems.get(i).date_last_update)
				{
					builder.setContentText("Modifying: Problem " + problems.get(i).id + " of " + problems.size());
			    	notification = builder.build();
			    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
			        notificationManager.notify(1, notification);
					
			        installOrUpdateProblem(pec, problems.get(i), false);
				}
			}
			
			end = System.currentTimeMillis();
			Log.w("Euler upate: Problem which have changed", "" + (end-start) / 1000 + " seconds");
			
			start = System.currentTimeMillis();
			
			// Step 3, add new problems.
			for(int j = i; j < problems.size(); j++)
			{
				if(MyApplication.cancelUpdater)
					return;
				
				builder.setContentText("Installing: Problem " + problems.get(j).id + " of " + problems.size());
		    	notification = builder.build();
		    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
		        notificationManager.notify(1, notification);
		        
				installOrUpdateProblem(pec, problems.get(j), true);
			}
			
			end = System.currentTimeMillis();
			Log.w("Euler upate: Install new problems", (end-start) / 1000 + " seconds");
		}
		
		notificationManager.cancel(1);
	}
	
	public void installOrUpdateProblem(ProjectEulerClient pec, EulerProblem ep, boolean install)
	{
		ContentValues args = new ContentValues();
		args.put("_id", ep.id);
		args.put("title", ep.description);
		args.put("published", ep.date_published);
		args.put("updated", ep.date_last_update);
		args.put("solvedby", ep.solved_by);
	    args.put("solved", ep.solved_flag);

		try 
		{
		    String html = pec.getProblem(ep.id).html();
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
			
			String body = soup.body().html();
			
		    args.put("html", body);
		    args.put("answer", ep.answer);
		} 
		
		catch (Exception e)
		{
			Log.w("Error", e.getMessage());
			
	        return;
		}
	    
		myDataBase.beginTransaction();
	    if(install)
	        myDataBase.insert("data", null, args);
	    else
			myDataBase.update("data", args, "_id = ?", new String[]{"" + ep.id});
		myDataBase.setTransactionSuccessful();
		myDataBase.endTransaction();
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

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		/*if(newVersion > oldVersion)
		{
			File file = context.getDatabasePath(DB_NAME);
			if(file.exists())
				file.delete();
			
			try 
	    	{
				copyDataBase();
			} 
	    	
	    	catch (IOException e)
	    	{
	    		throw new Error("Error copying database");
	    	}
		}
		*/
	}
	
	public void createDataBase() throws IOException {	
		//this.getReadableDatabase();
		
		File file = context.getDatabasePath(DB_NAME);
    	if(!file.exists())
    	{
        	try 
        	{
    			copyDataBase();
    		} 
        	
        	catch (IOException e)
        	{
        		throw new Error("Error copying database");
        	}
    	}	
	}
}