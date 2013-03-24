package ie.cathalcoffey.android.projecteuler;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomArrayAdapter extends ArrayAdapter<Object> {

	Context context;
	Object[] labels;
	
	public CustomArrayAdapter(Context context, int resource, Object[] objects) {
		super(context, resource, objects);
		this.context = context;
		this.labels = objects;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		
		if(convertView == null)
		{
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(R.layout.spinner_item, parent, false);
		}
		
		TextView label = (TextView)row.findViewById(R.id.text1);
		String label_text = labels[position].toString();
		label.setText(label_text);
		
		return row;
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
		count.setVisibility(View.GONE);
		String text = count.getText().toString();
		
		if (!((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && ViewConfiguration.get(context).hasPermanentMenuKey()) || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)) 
		{
			int count_text = 0;
			switch(Label.valueOf(label_text))
			{
			    case All:
			    	count_text = MyApplication.COUNT_ALL;
			    	break;
			    case Solved:
			    	count_text = MyApplication.COUNT_SOLVED;
			    	break;
			    case Starred:
			    	count_text = MyApplication.stars.size();
			    	break;
			    case Unsolved:
			    	count_text = MyApplication.COUNT_ALL - MyApplication.COUNT_SOLVED;
			    	break;  
			}
			count.setText("" + count_text);
			count.setVisibility(View.VISIBLE);
		}
		return row;
	}
}
