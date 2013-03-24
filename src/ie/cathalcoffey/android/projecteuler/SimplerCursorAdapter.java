package ie.cathalcoffey.android.projecteuler;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.ToggleButton;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

public class SimplerCursorAdapter extends SimpleCursorAdapter 
{
	DecimalFormat formatter = new DecimalFormat("###,###,###");
	Context context;
	
	@SuppressWarnings("deprecation")
	public SimplerCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) 
	{
		super(context, layout, c, from, to);
		
		this.context = context;
		
		MyApplication.stars = new Hashtable<String, Boolean>();
		if(MyApplication.settings != null && MyApplication.settings.contains("username"))
		{
			String username = MyApplication.settings.getString("username", "");
			SharedPreferences user_stars = context.getSharedPreferences(username + "_stars", Context.MODE_PRIVATE);
			
			Map<String, ?> items = user_stars.getAll();
			for(String id : items.keySet())
				MyApplication.stars.put(id, (Boolean)items.get(id));
		}
	}
	
	@Override
    public void bindView(View view, final Context context, Cursor cursor) 
	{
		super.bindView(view, context, cursor);
		
		try
		{
	        String solved_by = cursor.getString(cursor.getColumnIndex("solvedby"));
	        TextView name_text = (TextView) view.findViewById(R.id.solved_by);
	        if (name_text != null) 
	            name_text.setText("Solved by " + formatter.format(Float.parseFloat(solved_by)) + " Eulerians");
	        
	        final int _id = cursor.getInt(cursor.getColumnIndex("_id"));
	        String id = "PROBLEM " + _id;
	        TextView id_text = (TextView) view.findViewById(R.id.id);
	        
	        id_text.setText(Html.fromHtml(id));
	        
	        String title = cursor.getString(cursor.getColumnIndex("title"));
	        TextView title_text = (TextView) view.findViewById(R.id.title);

	        title_text.setText(Html.fromHtml(title));

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
		}
		
		catch(Exception e)
		{
			Log.w("ProjectEuler", e.getMessage());
		}
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
	   			 Log.d("cathal", _id + " ON");
	   			 MyApplication.stars.put(""+_id, true);
	   		 }
	   		 
	   		 else
	   		 {
	   			 Log.d("cathal", _id + " OFF");
	   			 
	   			 if(MyApplication.stars.containsKey(""+_id))
	   				 MyApplication.stars.remove(""+_id);
	   			 
	   			 if(MyApplication.display_text != null && MyApplication.display_text.equals(Label.Starred.toString()))
	   			 {
	   				 Cursor c = MyApplication.myDbHelper.getData(MyApplication.filter_text);
	   				 changeCursor(c);
	   			 }
	   		 }
	   	 }	
	}
}