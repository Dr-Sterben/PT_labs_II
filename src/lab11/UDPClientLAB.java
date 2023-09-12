package lab11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClientLAB {
    public static void main(String args[]) throws Exception {
        System.out.print("Enter the port you want to connect: ");
        Scanner in = new Scanner(System.in);
        int serverPort = in.nextInt();

        DatagramSocket newSocket = new DatagramSocket();
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                    String sentence = null;
                    try {
                        sentence = inFromUser.readLine();

                        //DatagramSocket clientSocket = new DatagramSocket();
                        InetAddress ipAddress = InetAddress.getByName("localhost");
                        byte[] sendData = new byte[1024];
                        sendData = sentence.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, serverPort);
                        newSocket.send(sendPacket);
                        //clientSocket.send(sendPacket);

                        if (sentence.equals("@quit")) {
                            break;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!newSocket.isClosed())
                    newSocket.close();
            }
        });

        Thread recieveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //DatagramSocket serverSocket = new DatagramSocket(9876);
                        byte[] receiveData = new byte[1024];

                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        if (newSocket.isClosed())
                            break;
                        newSocket.receive(receivePacket);
                        //serverSocket.receive(receivePacket);
                        String modifiedSentence = new String(receivePacket.getData());

                        if (modifiedSentence.charAt(0) == '@' &&
                                modifiedSentence.charAt(1) == 'q' &&
                                modifiedSentence.charAt(2) == 'u' &&
                                modifiedSentence.charAt(3) == 'i' &&
                                modifiedSentence.charAt(4) == 't') {
                            //clientSocket.close();
                            break;
                        }

                        System.out.print("FROM SERVER: ");
                        for (int i = 0; i < modifiedSentence.length(); i++) {
                            if ((int) modifiedSentence.charAt(i) >= 32)
                                System.out.print(modifiedSentence.charAt(i));
                        }
                        System.out.print("\n");
                    } catch (Exception e) {
                        break;
                    }
                }
                if (!newSocket.isClosed())
                    newSocket.close();
            }
        });

        sendThread.start();
        recieveThread.start();
    }
}
