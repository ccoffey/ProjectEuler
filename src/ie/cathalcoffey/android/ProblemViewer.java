package ie.cathalcoffey.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ProblemViewer extends Activity 
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
	    setContentView(R.layout.problem_viewer);
	    
	    WebView webView = (WebView)findViewById(R.id.webView);
	    webView.setWebViewClient(new WebViewClient());
	    webView.getSettings().setJavaScriptEnabled(true);
	    
	    String id = "1";
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) 
	        id = extras.getString("id");  
	    
	    try 
	    {			
	    	StringBuilder html = new StringBuilder();
	    	BufferedReader bf = new BufferedReader(new InputStreamReader(openFileInput(id + ".html")));
	    	String line = null;
	        while((line = bf.readLine()) != null)
	        	html.append(line);
	    	
			webView.loadDataWithBaseURL("file:///data/data/ie.cathalcoffey.android/files/", html.toString(), "text/html", "utf-8", null);
		} 
	    
	    catch (IOException e) 
	    {
		
		}
	}
}
