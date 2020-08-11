import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//Albert McFadden (afm0004)
//Ian Fair (izf0002)

public class SimpleClient {

    private static final int TIMEOUT = 100000000; // Resend timeout (milliseconds)
    private static final int MAXTRIES = 5; // Maximum retransmissions
    private Gremlin gremlin;
    public SimpleClient(double dmgIn, double dropIn, double delayIn, int delayTimeIn){

        gremlin = new Gremlin(dmgIn,dropIn,delayIn,delayTimeIn);
    }

    private DatagramPacket gremlin(DatagramPacket packet) throws InterruptedException {
        return this.gremlin.touchPacket(packet);
    }

    public static void main(String[] args) throws IOException {
        double dmgin = Double.parseDouble(args[0]);
        double dropin = Double.parseDouble(args[1]);
        double delayin = Double.parseDouble(args[2]);
        int delayTimeIn = Integer.parseInt(args[3]);

        SimpleClient client = new SimpleClient(dmgin, dropin, delayin, delayTimeIn);
        InetAddress IPAddress = InetAddress.getByName("192.168.1.92");
        // Server address

        String userSentence = "";

        System.out.println("Use ctrl + c to exit the application");
        System.out.print("Request a file: ");

        Scanner scnr = new Scanner(System.in);
        userSentence = scnr.nextLine();

        String request = "GET " + userSentence + " HTTP/1.0";
        byte[] bytesToSend = request.getBytes();

        int servPort = 10000;

        DatagramSocket socket = new DatagramSocket();

        socket.setSoTimeout(TIMEOUT); // Maximum receive blocking time (milliseconds)

        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, // Sending packet
                bytesToSend.length, IPAddress, servPort);

        int sum = sendPacket.getLength();
        System.out.println("packet length is " + sum + " bytes long.");
        socket.send(sendPacket);

        //***************************begin while loop to receive packets from server************************//

        boolean check = true;
        String message = "";
        int checkSum = 0;
        int checkPacket = 0;
        StringBuffer content = new StringBuffer();
        while (check == true) {         //

            DatagramPacket receivePacket = new DatagramPacket(new byte[256], 256);
            socket.receive(receivePacket);
            byte[] data = new byte[256-12];
            int length = -1;
            int seq = -1;
            int packetCheckSum = -1;
            try {
                receivePacket = client.gremlin(receivePacket);
                if(receivePacket == null){continue;}


                else if(receivePacket.getLength() == 1){
                    check = false;
                }
                else {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());
                    length = byteBuffer.getInt(0);
                    seq = byteBuffer.getInt(4);
                    packetCheckSum = byteBuffer.getInt(8);
                    checkSum = receivePacket.getLength();
                    System.out.println("packetchecksum = " + packetCheckSum);
                    System.out.println("checksum = " + checkSum);
                    if (packetCheckSum != (checkSum - 12)){
                        System.out.println("this packet got corrupted and the project is finished");
                        System.exit(-1);
                    }
                    data = Arrays.copyOfRange(receivePacket.getData(), 12, length);

                }
            }
            catch (InterruptedException iException){
                System.out.print("You got Interrupted! Rude.");
            }
            if (length > 0 && seq > 0) {
                content.append(new String(data));
            }
            //checkPacket +=
            checkSum += receivePacket.getLength(); // compare this value to value in header
            System.out.println("Received: " + new String(receivePacket.getData()));
            System.out.println("packet length is " + length);
            System.out.println("Sequence value is " + seq);
            System.out.println("Packet Checksum = " + packetCheckSum);
            System.out.println ("Calculated checksum = " + checkSum);
            //BufferedWriter writer = new BufferedWriter(new FileWriter("output.html", true));
            //message = new String(receivePacket.getData());
            //writer.write(message);
            //if (receivePacket.getLength() == 1 ) {
                //writer.close();
                //check = false;
            //}

        }//while special end of file character is not present
        //new scanner
        //system out "what would you like to name this file
        //scan in = user input
        //user input = newFileName
        //insert newfilename where output.html is now
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.html", true));
        writer.write(content.toString());
        writer.flush();
        System.out.println("File successfully written.");
        System.out.println("Contents of file is: " + message);
        socket.close();
    }
}


