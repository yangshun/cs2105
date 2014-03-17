import java.util.*;
/**
 * DataPacket : Encapsulate a data packet.
 *
 * A data packet contains an array of bytes, the size of the
 * array, a sequence number, and a flag to indicate if the 
 * packet has been corrupted.
 *
 * The object is serialized so that we can send it over the
 * network using Object I/O stream.
 *
 * Ooi Wei Tsang
 * CS2105, National University of Singapore
 * 13 March 2013
 */

class DataPacket implements java.io.Serializable {
	public int seq;
	public byte data[];
	public int length;
	public boolean isCorrupted;
	
	DataPacket(byte data[], int length, int seq)
	{
		if (length > 0) {
			this.data = new byte[length];
			System.arraycopy(data, 0, this.data, 0, length);
		} else {
			this.data = null;
		}
		this.seq = seq;
		this.length = length;
		this.isCorrupted = false;
	}
}
