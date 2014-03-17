/**
 * UDTReceiver: An unreliable data receiver.
 *
 * Ooi Wei Tsang
 * CS2105, National University of Singapore
 * 12 March 2013
 */

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * UDTReceiver
 *
 * This class represents the network channel that deliver packets unreliably. 
 * This implementation ensures that data, if delivered, are in order.
 * Packets, however, can be lost with a certain probability
 * given determined by the member P_DROP, and can be corrupted with
 * a certain probability determiend by the member P_CORRUPT.
 *
 * UDTReceiver is SIMULATED over a TCP connection (to ensure in order delivery)
 * with a manual, random, packet drop/corruption.
 */ 
class UDTReceiver {
	ServerSocket serverSocket;
	Socket      socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	static double P_DROP = 0.2;
	static double P_CORRUPT = 0.2;

	Random random;
	
	UDTReceiver(int port) throws IOException {
		
		// Creates a TCP server socket and accepts new connection.
		serverSocket = new ServerSocket(port);
		// System.out.println("R: waiting for connection..");
		socket = serverSocket.accept();
		// System.out.println("R: connection accepted");
		
		// Retrieve the input and output stream, and wrap an
		// object I/O stream around it, so that we can read 
		// and write DataPacket/AckPacket object to the socket.
		// NOTE: we need to create the output stream first, or
		// else the constructor for input stream will hang.
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		
		// Initialize the random number generator.
		random = new Random();
	}
	
	/**
	 * Reads a data packet from the socket and returns the
	 * packet.  
	 * 
	 * Packets can be randomly ignored to simulate packet drops,
	 * and can be randomly corrupted.
	 */
	DataPacket recv() throws IOException, ClassNotFoundException {
		Object obj;
		obj = ois.readObject();
		while (random.nextDouble() < P_DROP) {
			// Randomly drop a packet
			System.out.println("R: packet drop");
			obj = ois.readObject();
		}

		DataPacket dataPkt = ((DataPacket)obj);

		// Randomly corrupt the packet
		if (random.nextDouble() < P_CORRUPT) {
			System.out.println("R: corrupt " + dataPkt.seq);
			// create a new random packet
			byte data[] = null;
			if (dataPkt.length > 0) {
				data = new byte[dataPkt.length];
				random.nextBytes(data);
			} 
			dataPkt = new DataPacket(data, dataPkt.length, 
				random.nextInt(1));
			dataPkt.isCorrupted = true;
		}
		System.out.println("R: recv " + dataPkt.seq);
		return dataPkt;
	}
	
	/**
	 * Given an acknowledgement packet from RDTReceiver, this method
	 * sends ACK to the sender by writing it into the object output
	 * stream.
	 */
	void send(AckPacket ack) throws IOException {
		System.out.println("R: send ACK " + ack.ack);
		oos.writeObject(ack);
		oos.flush();
	}
	
	void close() throws IOException
	{
		ois.close();
		oos.close();
		socket.close();
		serverSocket.close();
	}
}
