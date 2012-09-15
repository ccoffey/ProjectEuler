package ie.cathalcoffey.android.projecteuler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ParseException;
import android.os.AsyncTask;
import android.util.Log;


public class ProjectEuler 
{
	private Context context;
	private HttpClient httpclient;
	private String name = null, level = null, solved = null;
	private String errorMessage = "";
	
	ProjectEuler(Context context)
	{
		this.context = context;
		httpclient = new DefaultHttpClient();
	}
	
	String getErrorMessage()
	{
		return this.errorMessage;
	}
	
	Object[] getDetails()
	{
		int total = Integer.parseInt(solved.substring(solved.indexOf("of ") + 3));
	    
	    int progress;
	    try
	    {
	    	progress = Integer.parseInt(solved.substring(solved.indexOf("Solved ") + 7, solved.indexOf(" out")));
	    }
	    
	    catch(Exception e)
	    {
	        progress = 0;
	        level = "Level 0";
	        solved = "Solved 0 out of " + total;
	    }
	    
		return new Object[]{this.name, this.level, total, progress, this.solved};
	}
	
	boolean login(String username, String password) throws ClientProtocolException, IOException
	{	    
	    HttpPost httppost = new HttpPost("http://projecteuler.net/login");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("login", "Login"));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity responseEntity = response.getEntity();
        if(responseEntity != null) 
        {
            String html = EntityUtils.toString(responseEntity);
            Document doc = Jsoup.parse(html);
            
            if(!doc.title().contains("Project Euler"))
            {
            	this.errorMessage = "Unable to connect to projecteuler.net, please check your internet connection.";
                return false;
            }
            
            Element message = doc.getElementById("message");
            if(message != null && message.text().contains("Login successful"))
            {
            	HttpGet httpget = new HttpGet("http://projecteuler.net/progress");
	            response = httpclient.execute(httpget);
    	        responseEntity = response.getEntity();
    	        if(responseEntity != null) 
    	        {
    	            html = EntityUtils.toString(responseEntity);
    	            doc = Jsoup.parse(html);
    	            this.name = doc.getElementsByTag("h2").get(0).text();
    	            
    	            if (doc.getElementsByTag("h3").size() == 5)
    	            {
    	            	this.level = doc.getElementsByTag("h3").get(0).text();
    	            	this.solved = doc.getElementsByTag("h3").get(1).text();
    	            }
    	            
    	            else
    	            {
    	            	this.level = "0";
    	            	this.solved = doc.getElementsByTag("h3").get(0).text();
    	            }
    	            
    	            return true;
    	        }
            }
        }

        this.errorMessage = "Login failed, please check your username and password.";
		return false;
	}

	public void getProblems(SQLHelper sqlHelper, EulerAsyncTask asyncTask, int total) 
	{
		Document doc = null;
        int page = 1;
        int pageCount = 1;
        do
        {
	        HttpGet httpget = new HttpGet("http://projecteuler.net/problems;page=" + page);
            HttpResponse response = null;
			try 
			{
				response = httpclient.execute(httpget);
				HttpEntity responseEntity = response.getEntity();
		        if(responseEntity!=null) 
		        {
		            String html = null;
					try 
					{
						html = EntityUtils.toString(responseEntity);
					} 
					
					catch (ParseException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
					doc = Jsoup.parse(html);
		            
			        Element table = doc.select("table[class=grid]").first();
			        Elements rows = table.children().first().children();
			         	
			        for(int i = 1; i < rows.size(); i++)
			        {
			        	Elements tds = rows.get(i).children();
			        	
			        	int id = Integer.parseInt(tds.get(0).text());
			        	asyncTask.update(id, "Downloading problems");
			        	
			        	String title = tds.get(1).text();
			        	int solved_by = Integer.parseInt(tds.get(2).text());
			        	
			        	boolean solved_by_me = false;
			        	Elements imgs = tds.get(3).getElementsByTag("img");
			        	if(imgs.size() > 0 && imgs.get(0).attr("src").equalsIgnoreCase("images/icon_tick.png"))
			        	    solved_by_me = true;
			        	
			        	downloadProblemHTML(id);
			        	sqlHelper.addProblem(name, id, title, solved_by, solved_by_me);
			        }
		        }
			} 
			
			catch (ClientProtocolException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                    
	        page += 1;
	        pageCount = doc.select("div[class=pagination]").first().select("a").size();
        } while(page <= pageCount);
	}
	
	private void downloadProblemHTML(int id) 
	{
		try 
	    {
	    	String baseUrl = "http://projecteuler.net/problem=" + id;
			Document doc = Jsoup.connect(baseUrl).get();
			
			Element head = doc.head();
			Element problem_content = doc.select("div[id=content]").first();
			Element footer = doc.select("div[id=footer]").first();
			
			// Download missing css
			for(Element link : doc.select("link[rel=stylesheet]"))
			{
				String css = link.attr("href");
				
				File file = context.getFileStreamPath(css);
				if(!file.exists())
				{
				    Document f = Jsoup.connect("http://projecteuler.net/" + css).get();
				    
				    PrintStream ps = new PrintStream(context.openFileOutput(css, context.MODE_PRIVATE));
				    ps.print(f.text());
				    ps.close();
				}
			}
			
			// Download missing images
			for(Element img : problem_content.select("img"))
			{
				String href = img.attr("src");
				
				File file = new File(context.getFilesDir() + "/" + href.substring(0, href.indexOf("/")), href.substring(href.indexOf("/")));
				
				if(!file.exists())
				{
					file.getParentFile().mkdirs();
					
				    URL url = new URL("http://projecteuler.net/" + href);
				    FileOutputStream fos = new FileOutputStream(file);
				    InputStream is = url.openConnection().getInputStream();
				    int current = -1;
				    while((current = is.read()) != -1)
				    	fos.write((byte)current);
				    is.close();
				    fos.close();
				}
			}
			
			// Download missing text files
			for(Element a : problem_content.select("a"))
			{
				String href = a.attr("href");
				if(href.endsWith(".txt"))
				{
					File file = new File(context.getFilesDir() + "/" + href.substring(0, href.indexOf("/")), href.substring(href.indexOf("/")));
					
					if(!file.exists())
					{
						file.getParentFile().mkdirs();
						
					    URL url = new URL("http://projecteuler.net/" + href);
					    FileOutputStream fos = new FileOutputStream(file);
					    InputStream is = url.openConnection().getInputStream();
					    int current = -1;
					    while((current = is.read()) != -1)
					    	fos.write((byte)current);
					    is.close();
					    fos.close();
					}
				}
			}
			
			String html = "<html>" + head + "<body>" + problem_content + footer + "<body></html>";
			
		    PrintStream ps = new PrintStream(context.openFileOutput(id + ".html", context.MODE_PRIVATE));
		    ps.print(html);
		    ps.close();
		} 
	    
	    catch (IOException e) 
	    {
		    String msg = e.getMessage();
		    Log.w("Project Euler", e.getMessage());
		}
		
	}
}
