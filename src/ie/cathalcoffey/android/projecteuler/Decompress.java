package ie.cathalcoffey.android.projecteuler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File; 
import java.io.FileOutputStream; 
import java.io.InputStream;
import java.util.zip.ZipEntry; 
import java.util.zip.ZipInputStream; 

import android.util.Log;

public class Decompress 
{ 
    public Decompress(InputStream fin, String location) 
    { 

    } 

    public static void unzip(InputStream fin, String location) 
    { 
        dirChecker(location, "");
        
        try  
        { 
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin)); 
            ZipEntry ze = null; 
      
            while ((ze = zin.getNextEntry()) != null) 
            { 
                if(ze.isDirectory()) 
                    dirChecker(location, ze.getName());
                
                else 
                { 
        	        File f = new File(location + ze.getName());
        	        f.getParentFile().mkdirs();

                    BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(location + ze.getName()));
                    
                    int BUFFER_SIZE = 2048;
                    byte b[] = new byte[BUFFER_SIZE];
                    int n;
                    while ((n = zin.read(b, 0, BUFFER_SIZE)) >= 0) 
                    	fout.write(b, 0, n);
                    
                    zin.closeEntry(); 
                    fout.close(); 
                }
            }
            
            zin.close(); 
        } 
    
	    catch(Exception e) 
	    {
	    	String s = e.getMessage();
	    	Log.e("DB Error", s);
	    } 
    }

    private static void dirChecker(String location, String dir) 
    { 
        File f = new File(location + dir); 

        if(!f.isDirectory()) 
            f.mkdirs(); 
    } 
} 