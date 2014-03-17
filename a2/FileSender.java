/**
 * FileSender : A client that uploads a file to a server.
 *
 * Usage: java FileSender <filename> <hostname> <port>
 *
 * This program connects to FileReceiver running on a given
 * hostname and at a given port number, and uploads the file
 * given by filename.
 *
 * Ooi Wei Tsang
 * CS2105, National University of Singapore
 * 1 March 2011
 */
import java.util.*;
import java.io.*;

class FileSender {
	public static void main(String args[])
	{
		// Parse the command line argument.
		if (args.length < 3)
		{
			System.err.println("Usage: java FileSender <filename> <hostname> <port>");
			return;
		}
		String filename = args[0];
		String hostname = args[1];
		int port = Integer.parseInt(args[2]);
		
		try 
		{
			// Create new reliable transport object and file input stream.
			
			RDTSender rdt = new RDTSender(hostname, port);		  
			FileInputStream fis = new FileInputStream(filename);
			
			// Repeatedly read from the file (1000 bytes at a time) and
			// upload it to the server.
			
			int chunkSize = 1000;
			byte data[] = new byte[chunkSize];
			while (true)
			{
				int length = fis.read(data, 0, chunkSize);
				if (length == -1) 
					break;
				rdt.send(data, length);
			}
			rdt.close();
			fis.close();
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
