/**
 * FileReceiver : A file server that receives a file.
 *
 * Usage: java FileReceiver <port> <name of file received>
 *
 * This program waits for the client to connect and upload one
 * file, which will be saved as the given filename given in the
 * second command line argument.
 *
 * Ooi Wei Tsang
 * CS2105, National University of Singapore
 * 1 March 2011
 */
import java.util.*;
import java.io.*;

class FileReceiver {
	
	public static void main(String args[])
	{
		// Parse the command line arguments.
		if (args.length < 2) 
		{
			System.err.println("Usage: java FileReceiver <port> <filename>");
			return;
		} 
		int port = Integer.parseInt(args[0]);
		String filename = args[1];
        
		try 
		{
			// Create new reliable transport object and file output stream.
			
			RDTReceiver rdt = new RDTReceiver(port);        
			FileOutputStream fos = new FileOutputStream(filename);
	        
			// Repeatedly receive data from the reliable transport until
			// the end of file.
			byte data[] = rdt.recv();
			while (data != null)
			{
				fos.write(data);
				data = rdt.recv();
			}
			System.out.println("R: DONE!");
			rdt.close();
			fos.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
    }
}
