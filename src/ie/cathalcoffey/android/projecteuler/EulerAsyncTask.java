package ie.cathalcoffey.android.projecteuler;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class EulerAsyncTask extends AsyncTask<Object, Object, Object>
{
    protected MyProgressDialog progressDialog;
    
	protected void update(int i, String msg) 
	{
		
	}

	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		
	}

	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		return null;
	}
}
