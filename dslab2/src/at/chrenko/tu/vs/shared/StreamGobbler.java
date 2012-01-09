package at.chrenko.tu.vs.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Thomas Chrenko - 0728121
 *
 * StreamGobbler redirects the input from given InputStream to given OutputStream.
 *
 */
public class StreamGobbler extends Thread {
	InputStream is;
    OutputStream os;
    
    public StreamGobbler(InputStream is, OutputStream os)
    {
    	if(is == null)
    		throw new IllegalArgumentException("InputStream can't be null!");
    	
    	if(os == null)
    		throw new IllegalArgumentException("OutputStream can't be null!");
    	
        this.is = is;
        this.os = os;
    }
    
    public void run()
    {
        try
        {
        	InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            PrintWriter pw = new PrintWriter(os, true);
            
            String inputLine = null;
            
            while ( (inputLine = br.readLine()) != null)
            {
                pw.println(inputLine);
            }
        } catch (IOException e)
        {
            e.printStackTrace();  
        }
    }
}
