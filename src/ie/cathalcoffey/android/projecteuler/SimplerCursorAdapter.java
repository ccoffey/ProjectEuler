package ie.cathalcoffey.android.projecteuler;

import java.text.DecimalFormat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SimplerCursorAdapter extends SimpleCursorAdapter 
{
	ProblemsTab appWidgetProblems;
	DecimalFormat formatter = new DecimalFormat("###,###,###");
	
	public SimplerCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) 
	{
		super(context, layout, c, from, to);
		
		this.appWidgetProblems = (ProblemsTab) context;
	}
	
	@Override
    public void bindView(View view, Context context, Cursor cursor) 
	{
		super.bindView(view, context, cursor);
		
		try
		{
			Boolean solved = cursor.getInt(cursor.getColumnIndex("solved")) == 1 ? true: false;
			int show = appWidgetProblems.prefs.getInt("show", 0);
			
			TextView id = (TextView) view.findViewById(R.id.id);
			TextView problem = (TextView) view.findViewById(R.id.problem);
			
	        
			if(appWidgetProblems.prefs.getBoolean("coloredText", true))
			{
				if (solved)
				{
					id.setTextColor(Color.parseColor("#4C9900"));
					problem.setTextColor(Color.parseColor("#4C9900"));
				}
				
				else
				{
					id.setTextColor(Color.parseColor("#CC0000"));
					problem.setTextColor(Color.parseColor("#CC0000"));	
				}
			}
			
	        String solved_by = cursor.getString(cursor.getColumnIndex("solved_by"));
	
	        TextView name_text = (TextView) view.findViewById(R.id.solved_by);
	        if (name_text != null) 
	        {
	            name_text.setText(formatter.format(Float.parseFloat(solved_by)));
	        }
		}
		
		catch(Exception e)
		{
			Log.w("ProjectEuler", e.getMessage());
		}
    }
}

