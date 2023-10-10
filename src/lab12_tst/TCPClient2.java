package lab12_tst;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class TCPClient2 {
    //static final
    private static String SERVER_IP = "127.0.0.1"; // IP адрес сервера
    private static int SERVER_PORT = 12345; // Порт сервера

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            try {
                SERVER_IP = args[0];
                SERVER_PORT = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Wrong cmd parameters");
            }
        }
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Вы подключены к серверу. Введите ваше имя:");

            String userName = consoleInput.readLine();
            out.println(userName);
            Thread recieveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String serverResponse = "";
                        try {
                            serverResponse = in.readLine();
                        } catch (Exception e) {

                        }
                        if (!Objects.equals(serverResponse, "")) {
                            System.out.println(serverResponse);
                        }
                    }
                }
            });

            Thread sendTread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String message = consoleInput.readLine();
                            if (!message.equals(""))
                                out.println(message);
                            if (message.equals("@exit")) {
                                System.exit(0);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            });

            sendTread.start();
            recieveThread.start();
            while (true) {
                String message = consoleInput.readLine();
                out.println(message);
                if (message.equals("@exit")) {
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//