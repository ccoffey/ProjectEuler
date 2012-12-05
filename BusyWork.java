package ie.cathalcoffey.android;

import android.os.AsyncTask;

public class BusyWork extends AsyncTask<String, Void, Void>{

	@Override
	protected Void doInBackground(String... params) 
	{
		while(true)
		{
			try {
				Thread.sleep(100);
			} 
			
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
