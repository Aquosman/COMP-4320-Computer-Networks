import java.net.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.file.Files;
//Frank Mcfadden (afm0004)
//Ian Fair (izf0002)

public class SimpleServer {

    private static final int BYTEMAX = 256;  // Maximum size of datagram
    private static final int headerLength = 12;

    public static int errorDetectionFunction (byte[] data){                //checks length of data to compare clientside
        return data.length;
    }

    public static String getFileNameFromPacket(DatagramPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb = new StringBuilder(new String(packet.getData()).trim()); //use client side
        String result = sb.toString();
        System.out.println(result);
        String[] results = result.split("\\s+");
        return results[1];
    }

    public static ArrayList<DatagramPacket> createPacketArray (byte[] byteList, InetAddress ip, int port) {
        ArrayList<DatagramPacket> packetList = new ArrayList<DatagramPacket>();
        int byteLimit = BYTEMAX - headerLength;
        Packet packet;                                         //from packet class
        byte[] packetBytes = new byte[byteLimit];
        int counter = 0;
        int seqNum = 1;
        for(int i = 0; i < byteList.length; i++){
            if (counter < byteLimit){
                packetBytes[counter] = byteList[i];
                counter ++;
            }
            else {
                packet = new Packet(packetBytes.length, seqNum, packetBytes);
                packet.checkSum = errorDetectionFunction(packet.data);
                seqNum ++;
                packetList.add(packet.getDatagramPacket(port, ip));
                counter = 0;
                packetBytes = new byte[byteLimit];
            }

        }
        if (counter > 0){
            packet = new Packet(packetBytes.length, seqNum, packetBytes);
            packet.checkSum = errorDetectionFunction(packet.data);
            seqNum ++;
            packetList.add(packet.getDatagramPacket(port, ip));
        }
        return packetList;
       /* while (content != null) {

            System.out.print(content);
            String header = ("CS = ");// should contain checksum
            //design this header as how many bytes it will take
            String needsToSend = (header + content);
            packet.setData(needsToSend.getBytes());
            socket.send(packet);       // Send the same packet back to client
            packet.setData(new byte[ECHOMAX]);
            //checkSum += packet.getLength();
            packet.setLength(ECHOMAX); // Reset length to avoid shrinking buffer
            content = br.readLine();
            //packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);
        }
        */
    }

    public static void main(String[] args) throws IOException {



        int servPort = 10000;

        DatagramSocket socket = new DatagramSocket(servPort);
        DatagramPacket packet = new DatagramPacket(new byte[BYTEMAX], BYTEMAX);

        //StringBuilder sb = new StringBuilder();

        for (;;) {  // Run forever
            socket.receive(packet);     // Receive packet from client


            System.out.println("Handling client at " +
                    packet.getAddress().getHostAddress() + " on port " + packet.getPort());
            String filename = getFileNameFromPacket(packet);


           /* sb = new StringBuilder(new String(packet.getData()).trim()); //use client side
            String result = sb.toString();
            System.out.println(result);
            String[] results = result.split("\\s+");
            File file = new File(filename);
            */
            File file;
            file = new File(filename);
            byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
            ArrayList<DatagramPacket> packetList = createPacketArray(fileBytes, packet.getAddress(), packet.getPort());

            String generalHeader = ("HTTP/1.0 " + "200 Document " + "Follows\r\n"
                    + "Content-Type: text/plain\r\n" + "Content-Length: 256\r\n "
                    + "\r\n" + "Data" + "\n\0");
            byte[] generalHeaderBytes = generalHeader.getBytes();
            Packet packetWrapper = new Packet(generalHeaderBytes.length, 0, generalHeaderBytes);
            packetWrapper.checkSum = generalHeaderBytes.length;
            System.out.println("checksum = " + packetWrapper.checkSum);
            int checkSum = 0;
            //System.out.println("packet length is " + checkSum + " bytes long.");
            socket.send(packetWrapper.getDatagramPacket(packet.getPort(), packet.getAddress()));
            for(DatagramPacket pkt : packetList){
                socket.send(pkt);
            }
            System.out.println(" end of file reached");
            packet.setData(new byte[1]);
            //System.out.println("your final packet length is " packet.getLength());
            socket.send(packet);
        }

    }
}
