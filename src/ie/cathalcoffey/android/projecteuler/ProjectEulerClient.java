package ie.cathalcoffey.android.projecteuler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ProjectEulerClient 
{
	public class EulerProfile
	{    	
		 String username;
		 String alias;
		 String country;
		 String language;
		 String solved;
		 String level;
		 String solved_str;
		 
		 EulerProfile(String username, String alias, String  country,  String language, String solved, String level, String solved_str)
		 {
			 this.username = username;
			 this.alias = alias;
			 this.country = country;
			 this.language = language;
			 this.solved = solved;
			 this.level = level;
			 this.solved_str = solved_str;
		 }
	}
	
	public class EulerProblem
	{
		 int id;
    	 String description;
    	 int date_published;
    	 int date_last_update;
    	 int solved_by;
    	 Boolean solved_flag;
    	 String answer;
    	
		 EulerProblem(int id, String description, int date_published, int date_last_update, int solved_by, Boolean solved_flag, String answer)
		 {
			 this.id = id;
         	 this.description = description;
         	 this.date_published = date_published;
         	 this.date_last_update = date_last_update;
         	 this.solved_by = solved_by;
         	 this.solved_flag = solved_flag;
         	 this.answer = answer;
		 }
	}
	
	private HttpClient httpclient;
	private String error;
	private String logout_url = null;
	String solve_msg;
	
	ProjectEulerClient()
	{
		this.httpclient = new DefaultHttpClient();
	}
	
	Drawable loadImageFromWeb(String url)
    {
        try
        {
        	HttpGet httpget = new HttpGet(url);
    		HttpResponse response = httpclient.execute(httpget);
            HttpEntity responseEntity = response.getEntity();
            
            Drawable d = Drawable.createFromStream( responseEntity.getContent(), "src");
            return d;
        }
        
        catch (Exception e) 
        {
      	    return null;
        }
    }
	
	boolean solve(String id, String guess, String confirm) throws ClientProtocolException, IOException
	{
		HttpPost httppost = new HttpPost("http://projecteuler.net/problem=" + id);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("guess", guess));
        nameValuePairs.add(new BasicNameValuePair("confirm", confirm));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity responseEntity = response.getEntity();
        if(responseEntity != null) 
        {
            String html = EntityUtils.toString(responseEntity);
            Document doc = Jsoup.parse(html);
            
            Element message = doc.select("div[id=message]").first();
            if(message != null)
            {
            	this.solve_msg = message.text();
            	return false;
            }
            
            else
            {
	            Elements imgs = doc.select("img");
	            for(Element img : imgs)
	            {
	            	if(img.hasAttr("alt") && img.attr("alt").equals("Correct"))
	            	{
	            		return true;
	            	}
	            }
            }
        }
        
        this.solve_msg = "Sorry, but the answer you gave appears to be incorrect.";
		return false;
	}
	
	boolean login(String username, String password) throws ClientProtocolException, IOException
	{	    
	    HttpPost httppost = new HttpPost("http://projecteuler.net/login");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("login", "Login"));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity responseEntity = response.getEntity();
        if(responseEntity != null) 
        {
            String html = EntityUtils.toString(responseEntity);
            Document doc = Jsoup.parse(html);
            
            if(doc.title().contains("Project Euler"))
            {
            	Elements elms = doc.select("a[title=Logout]");
            	if (elms.size() > 0)
            	{
            		Element logout = elms.get(0);
            		this.logout_url = logout.attr("href");
            		
                	return true;	
            	}
            	
            	this.error = "Your username or password are incorrect.";
            	return false;	
            }
            
            else
            {
            	this.error = "Unable to connect to projecteuler.net, please check your internet connection";
                return false;
            }
        }

        this.error = "Login failed, please check your username and password";
		return false;
	}
	
	boolean logout() throws ClientProtocolException, IOException
	{
		if(logout_url != null)
		{
			quickGet("http://projecteuler.net/" + logout_url);
			
			return true;
		}
		
		return false;
	}
	
	Document getProblem(int id) throws ClientProtocolException, IOException
	{
		Document doc = null;
		
		String content = quickGet("http://projecteuler.net/minimal=" + id);
        if(content != null) 
            doc = Jsoup.parse(content);
		
		return doc;
	}
	
	String getCaptcha() throws ClientProtocolException, IOException
	{
		String content = quickGet("http://projecteuler.net/minimal=captcha");
		return content;
	}
	
	EulerProfile getProfile() throws ClientProtocolException, IOException
	{
		EulerProfile profile = null;
		
		String content = quickGet("http://projecteuler.net/minimal=profile");
        if(content != null) 
        {
            String[] data = content.split("##");
            String username = data[0];
            String alias = data[1];
            String country = data[2];
            String language = data[3];
            String solved = data[4];
            String level = data[5];
            String solved_str = data[6];
            
            profile = new EulerProfile(username, alias, country, language, solved, level, solved_str);
        }
        
		return profile;
	}
	
	String tmp;
	ArrayList<EulerProblem> getProblems() throws ClientProtocolException, IOException
	{
		ArrayList<EulerProblem> problems = new ArrayList<EulerProblem>();
		
		try
		{
			String content = quickGet("http://projecteuler.net/minimal=problems");
	        if(content != null) 
	        {
	            for(String line : content.trim().split("\n"))
	            {
	            	String[] data = line.trim().split("##");
	            	String desc = Jsoup.parse(data[1]).text();
	            	
	            	String answer = "";
	            	if(data.length == 7)
	            		answer = data[6];
	            	
	            	problems.add(new EulerProblem(Integer.parseInt(data[0]), desc, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), data[5].equals("1"), answer));
	            }
	        }
		}
		
		catch(Exception e)
		{
			String msg = e.getMessage();
		}
        return problems;
	}
	
	String quickGet(String url) throws ParseException, IOException
	{
		String content = null;
		
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);
        HttpEntity responseEntity = response.getEntity();
        
        if(responseEntity != null) 
        {
        	content = EntityUtils.toString(responseEntity);
        }
        
        return content;
	}
	
	String getError()
	{
		return this.error;
	}
}
