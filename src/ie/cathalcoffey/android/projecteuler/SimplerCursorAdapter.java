package ie.cathalcoffey.android.projecteuler;

import java.text.DecimalFormat;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import org.holoeverywhere.widget.TextView;

public class SimplerCursorAdapter extends SimpleCursorAdapter 
{
	DecimalFormat formatter = new DecimalFormat("###,###,###");
	
	@SuppressWarnings("deprecation")
	public SimplerCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) 
	{
		super(context, layout, c, from, to);
	}
	
	@Override
    public void bindView(View view, Context context, Cursor cursor) 
	{
		super.bindView(view, context, cursor);
		
		try
		{
	        String solved_by = cursor.getString(cursor.getColumnIndex("solvedby"));
	        TextView name_text = (TextView) view.findViewById(R.id.solved_by);
	        if (name_text != null) 
	            name_text.setText("Solved by " + formatter.format(Float.parseFloat(solved_by)) + " Eulerians");
	        
	        String id = "PROBLEM " + cursor.getInt(cursor.getColumnIndex("_id"));
	        TextView id_text = (TextView) view.findViewById(R.id.id);
	        
	        id_text.setText(Html.fromHtml(id));
	        
	        String title = cursor.getString(cursor.getColumnIndex("title"));
	        TextView title_text = (TextView) view.findViewById(R.id.title);

	        title_text.setText(Html.fromHtml(title));
		}
		
		catch(Exception e)
		{
			Log.w("ProjectEuler", e.getMessage());
		}
    }
}