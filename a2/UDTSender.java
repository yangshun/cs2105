/**
 * UDTSender: An unreliable data sender.
 *
 * Ooi Wei Tsang
 * CS2105, National University of Singapore
 * 12 March 2013
 */
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * UDTSender
 *
 * This class represents the network channel that deliver packets unreliably. 
 * This implementation ensures that data, if delivered, are in order.
 * Packets, however, can be lost or corrupted with a certain probability
 * given determined by the member P_DROP and P_CORRUPT.
 *
 * UDTSender is SIMULATED over a TCP connection (to ensure in order delivery)
 * with a manual, random, packet drop and packet corruption.
 */ 
class UDTSender {

	Socket s;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	Random random;

	// Set the packet loss probability.  EDIT the following
	// line to change the loss probability.
	static double P_DROP = 0.2;
	static double P_CORRUPT = 0.2;

	UDTSender (String hostname, int port) throws IOException
	{
		// Connect to TCP server at the given hostname and port.
		s = new Socket(hostname, port);

		// Retrieve the input and output stream, and wrap an
		// object I/O stream around it, so that we can read 
		// and write DataPacket/AckPacket object to the socket.
		// NOTE: we need to create the output stream first, or
		// else the constructor for input stream will hang.
		oos = new ObjectOutputStream(s.getOutputStream());
		ois = new ObjectInputStream(s.getInputStream());

		// Initialize random number generator.
		random = new Random();
	}

	/**
	 * Given a data packet from RDTSender, this method sends
	 * the packet to the receiver by writing it into the 
	 * object output stream.
	 */
	synchronized void send(DataPacket p) throws IOException
	{
		System.out.println("S: send " + p.seq);
		oos.writeObject(p);
		oos.flush();
	}

	/**
	 * Reads an acknowledge packet from the socket and 
	 * returns the packet.
	 *
	 * Packets read can be randomly ignored to simulate
	 * packet drops, or randomly marked as corrupted.  
	 */
	AckPacket recv() throws IOException, ClassNotFoundException
	{
		Object obj = ois.readObject();
		while (random.nextDouble() < P_DROP) {
			System.out.println("S: packet drop");
			obj = ois.readObject();
		}
		AckPacket ack = (AckPacket)obj;

		// randomly corrupt the received packet.
		if (random.nextDouble() < P_CORRUPT) {
			System.out.println("S: corrupt ACK " + ack.ack);
			// create a return a new corrupted ACK.
			ack = new AckPacket(random.nextInt(1));
			ack.isCorrupted = true;
		}
		System.out.println("S: recv ACK " + ack.ack);
		return ack;
	}

	void close() throws IOException
	{
		s.close();
	}
}
