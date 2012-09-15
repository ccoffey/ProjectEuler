package ie.cathalcoffey.android.projecteuler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class SQLHelper extends SQLiteOpenHelper 
{
	private static final String DATABASE_NAME = "project_euler.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE_PROBLEMS = "problems";
	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String SOLVED_BY = "solved_by";

	public static final String TABLE_USERS = "users";
	public static final String USERNAME = "username";
	public static final String SOLVED = "solved";

	public SQLHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String sql = "create table " + TABLE_PROBLEMS 
        + "( " 
        + BaseColumns._ID + " integer primary key autoincrement, " 
        + ID + " integer, " 
        + TITLE + " text, "
        + SOLVED_BY + " integer" 
		+ ");";

		db.execSQL(sql);
		
		sql = "create table " + TABLE_USERS 
        + "( " 
        + BaseColumns._ID + " integer primary key autoincrement, " 
        + ID + " integer, " 
        + USERNAME + " text, " 
        + SOLVED + " integer"
		+ ");";
		
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{

	}
	
	void addProblem(String username, int id, String title, int solved_by, boolean solved) 
	{
	    SQLiteDatabase db = getWritableDatabase();
	    
	    ContentValues values = new ContentValues();
	    values.put(ID, id);
	    values.put(TITLE, title);
	    values.put(SOLVED_BY, solved_by);
	    
	    Cursor cursor = db.query
	    (
    		TABLE_PROBLEMS, 
    		new String[] {"id"}, 
    		ID + "=" + id, 
    		null, null, null, null
	    );
	    
	    if (cursor.getCount() > 0)
	    {
	    	db.update
	    	(
    			TABLE_PROBLEMS, 
    			values, 
    			ID + "=" + id, 
    			null
	    	);
	    	
	    	Log.w("ProjectEuler", "Problem " + id + " updated");
	    }
	    
	    else
	    {	
		    db.insert
		    (
				TABLE_PROBLEMS, 
				null, 
				values
		    );
		    
	        Log.w("ProjectEuler", "Problem " + id + " inserted");
	    }
	    
	    try
	    {
		    values = new ContentValues();
		    values.put(USERNAME, username);
		    values.put(ID, id);
		    values.put(SOLVED, solved? 1 : 0);
		    
		    cursor = db.query
		    (
	    		TABLE_USERS, 
	    		new String[] {"id", "username"}, 
	    		ID + "=" + id + " AND " + USERNAME + "='" + username + "'", 
	    		null, 
	    		null, 
	    		null, 
	    		null
		    );
		    
		    if (cursor.getCount() > 0)
		    {
		    	db.update
		    	(
	    			TABLE_USERS, 
	    			values,
	    			ID + "=" + id + " AND " + USERNAME + "='" + username + "'", 
		    		null
		    	);
		    	
		    	Log.w("ProjectEuler", "Problem " + id + "(" + username + ") " + " updated");
		    }
		    
		    else
		    {
		    	db.insert
			    (
					TABLE_USERS, 
					null, 
					values
			    );
		    	
		    	Log.w("ProjectEuler", "Problem " + id + "(" + username + ") " + " inserted");
		    }
	    }
	    
	    catch(Exception e)
	    {
	    	Log.w("ProjectEuler", e.getMessage());
	    }
	    
	    db.close();
	}
}