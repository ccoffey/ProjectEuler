package ie.cathalcoffey.android.projecteuler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomArrayAdapter extends ArrayAdapter {

	Context context;
	Object[] labels;
	
	public CustomArrayAdapter(Context context, int resource, int textViewResourceId, Object[] objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		this.labels = objects;
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		
		if(convertView == null)
		{
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(R.layout.spinner_dropdown_item, parent, false);
		}
		
		TextView label = (TextView)row.findViewById(R.id.text1);
		String label_text = labels[position].toString();
		label.setText(label_text);
		
		TextView count = (TextView)row.findViewById(R.id.text2);
		
		int[] counts = MyApplication.myDbHelper.getSolvedCount();
		
		int count_text = 0;
		switch(Label.valueOf(label_text))
		{
		    case All:
		    	count_text = counts[1];
		    	break;
		    case Solved:
		    	count_text = counts[0];
		    	break;
		    case Unsolved:
		    	count_text = counts[1] - counts[0];
		    	break;  
		}
		count.setText("" + count_text);
		
		return row;
	}
}
