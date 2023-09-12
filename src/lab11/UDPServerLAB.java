package lab11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPServerLAB {
    public static void main(String args[]) throws Exception {
        while (true) {
            System.out.print("Enter the port you need (or enter 0 to stop): ");
            Scanner in = new Scanner(System.in);
            int serverPort = in.nextInt();
            if (serverPort == 0)
                break;

            System.out.println("Waiting for the client...");
            DatagramSocket newSocket = new DatagramSocket(serverPort);
            int[] port = {0};
            final InetAddress[] IPAddress = new InetAddress[1];

            int[] nextChat = {0};
            int[] isPort = {0};
            String[] name = {" "};

            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        synchronized (port) {
                            if (port[0] != 0)
                                isPort[0] = 1;
                        }

                        synchronized (nextChat) {
                            if (nextChat[0] != 0) {
                                nextChat[0] = 0;
                                break;
                            }
                        }

                        if (isPort[0] != 0) {
                            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                            String sentence = null;
                            try {
                                //System.out.println("Continue process");
                                sentence = inFromUser.readLine();

                                byte[] sendData = new byte[1024];
                                sendData = sentence.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress[0], port[0]);

                                //System.out.println("Sending...");
                                newSocket.send(sendPacket);
                                //System.out.println("Sent!");


                                if (sentence.equals("@quit")) {
                                    //clientSocket.close();
                                    newSocket.close();
                                    break;
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
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
                            byte[] receiveData = new byte[1024];

                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            newSocket.receive(receivePacket);
                            synchronized (port) {
                                port[0] = receivePacket.getPort();
                            }
                            IPAddress[0] = receivePacket.getAddress();
                            //System.out.println(port[0] + " " + IPAddress[0]);
                            String modifiedSentence = new String(receivePacket.getData());

                            if (modifiedSentence.charAt(0) == '@' &&
                                    modifiedSentence.charAt(1) == 'q' &&
                                    modifiedSentence.charAt(2) == 'u' &&
                                    modifiedSentence.charAt(3) == 'i' &&
                                    modifiedSentence.charAt(4) == 't') {
                                System.out.println("Client closed the chat");
                                synchronized (nextChat) {
                                    //nextChat[0] = 1;
                                    sendThread.stop();
                                    break;
                                }
                            }

                            if (modifiedSentence.charAt(0) == '@' &&
                                modifiedSentence.charAt(1) == 'n' &&
                                modifiedSentence.charAt(2) == 'a' &&
                                modifiedSentence.charAt(3) == 'm' &&
                                modifiedSentence.charAt(4) == 'e') {
                                StringBuilder nameBuilder = new StringBuilder();
                                for (int i = 6; i < modifiedSentence.length(); i++) {
                                    if ((int)modifiedSentence.charAt(i) >= 32)
                                        nameBuilder.append(modifiedSentence.charAt(i));
                                }
                                name[0] = nameBuilder.toString();
                                continue;
                            }

                            if (!name[0].equals(" "))
                                System.out.print(name[0] + ": ");
                            else
                                System.out.print("FROM CLIENT: ");
                            for (int i = 0; i < modifiedSentence.length(); i++) {
                                if ((int) modifiedSentence.charAt(i) >= 32)
                                    System.out.print(modifiedSentence.charAt(i));
                            }
                            System.out.print("\n");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            recieveThread.start();
            sendThread.start();
            recieveThread.join();
            sendThread.join();
            newSocket.close();
        }
    }
}