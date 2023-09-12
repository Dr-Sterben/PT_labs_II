package lab12_tst;

import java.io.*;
import java.net.*;
import java.util.*;



public class TCPServer {
    private static int PORT = 12345;
    private static List<Socket> clients = new ArrayList<>();
    private static Map<Socket, PrintWriter> clientWriters = new HashMap<>();
    private static Map<Socket, String> clientNames = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                PORT = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Wrong cmd parameters");
            }
        }
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен. Ожидание подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                System.out.println("Новый клиент подключился: " + clientSocket.getInetAddress());

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.put(clientSocket, out);

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] extractNameAndMessage(String message) {
        String[] result = new String[2];

        if (message != null) {
            // Используем метод split для разделения строки по пробелу
            String[] parts = message.split(" ", 3);

            if (parts.length == 3) {
                result[0] = parts[2]; // Имя после пробела
                result[1] = parts[1].substring(10); // Само сообщение (без "@senduser ")
            }
        }

        else {
            result[0] = "none";
            result[1] = "none";
        }

        return result;
    }

    public static String[] parseMessage(String message) {
        // Инициализируем массив для хранения имени и сообщения
        String[] result = new String[2];

        // Проверяем, начинается ли строка с "@senuser"
        if (message.startsWith("@senduser")) {
            // Разделяем строку по пробелу
            String[] parts = message.split(" ", 3);

            // Проверяем, что массив parts имеет достаточное количество элементов
            if (parts.length == 3) {
                // Устанавливаем имя и сообщение
                result[0] = parts[1];
                result[1] = parts[2];
            }
        }

        return result;
    }


    public static void broadcastMessage(String message, Socket sender) {
        /*
        String[] personalMessage = extractNameAndMessage(message);
        if ((!Objects.equals(personalMessage[0], "none")) && (!Objects.equals(personalMessage[1], "none"))
            && !Objects.equals(personalMessage[0], null) && !Objects.equals(personalMessage[1], null)) {
            for (Socket client : clients) {
                String name = clientNames.get(client);
                if (!Objects.equals(name, null)) {
                    if (name.equals(personalMessage[0])) {
                        PrintWriter out = clientWriters.get(client);
                        String sendName = clientNames.get(sender);
                        out.println(sendName + ": " + personalMessage[1]);
                    }
                }
            }
        }
         */
        //else {
            for (Socket client : clients) {
                if (client != sender) {
                    PrintWriter out = clientWriters.get(client);
                    out.println(message);
                }
            }
        //}
    }
    public static void privateMessage(String name, String message, Socket sender) {
        for (Socket clientSocket : clients) {
            if (clientSocket != sender) {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    // Получаем имя клиента из сокса
                    String clientName = clientNames.get(clientSocket);
                    if (clientName.equals(name)) {
                        // Отправляем приватное сообщение
                        out.println(name + ": " + message);
                        //return; // Прерываем цикл после отправки
                    }
                } catch (IOException e) {
                    // Обработка ошибок, если не удалось отправить сообщение
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String userName = in.readLine();
                clientNames.put(this.clientSocket, userName);
                System.out.println("Пользователь " + userName + " присоединился к чату.");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Пользователь " + userName + ": " + message);
                    String[] parsedMsg = parseMessage(message);
                    if ((parsedMsg[0] != null) && (parsedMsg[1] != null)) {
                        privateMessage(parsedMsg[0], parsedMsg[1], clientSocket);
                    }
                    else
                        broadcastMessage(userName + ": " + message, clientSocket);
                }

                System.out.println("Пользователь " + userName + " покинул чат.");
                clients.remove(clientSocket);
                clientWriters.remove(clientSocket);
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
