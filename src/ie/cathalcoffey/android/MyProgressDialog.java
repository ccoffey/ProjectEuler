package ie.cathalcoffey.android;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyProgressDialog extends ProgressDialog 
{
	public MyProgressDialog(Context context) 
	{
	    super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);

	    setTextVisibility(View.INVISIBLE);
	}
	
	void setTextVisibility(Integer visibility)
	{
		try 
	    {
	        Method method = TextView.class.getMethod("setVisibility", Integer.TYPE);
	        
	        Field[] fields = this.getClass().getSuperclass().getDeclaredFields();
	        for (Field field : fields) 
	        {
	            if (field.getName().equalsIgnoreCase("mProgressNumber") || field.getName().equalsIgnoreCase("mProgressPercent")) 
	            {
	                field.setAccessible(true);
	                TextView textView = (TextView) field.get(this);
	                method.invoke(textView, visibility);
	            }
	        }
	    } 
	    
	    catch (Exception e) 
	    {
	    	Log.w("Euler", e.getMessage());
	    }
	}
}