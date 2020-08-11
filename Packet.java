import java.net.*;
import java.nio.ByteBuffer;
//Frank Mcfadden (afm0004)
//Ian Fair (izf0002)

public class Packet {
    private int length;
    private int seqNum;
    byte[] data;
    int checkSum;
    private static final int byteLimit = 256;
    public Packet(int length, int seqNum, byte[] data){
        this.length = length;
        this.seqNum = seqNum;
        this.data = data;

   }

   public DatagramPacket getDatagramPacket(int port, InetAddress ip){
        int packetLength = this.length + 12;
       ByteBuffer bb = ByteBuffer.allocate(packetLength);
       bb.putInt(this.length);
       bb.putInt(this.seqNum);
       bb.putInt(this.checkSum);
       bb.put(this.data);
       DatagramPacket servPacket = new DatagramPacket(bb.array(), packetLength, ip, port);
       return servPacket;
   }

}

