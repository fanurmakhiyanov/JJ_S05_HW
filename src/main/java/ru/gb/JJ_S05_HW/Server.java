package ru.gb.JJ_S05_HW;

import lombok.Getter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static final int PORT = 8181;
    private static long clientIdCounter = 1L;
    private static Map<Long, SocketWrapper> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                final Socket client = server.accept();
                long clientId = clientIdCounter++;

                SocketWrapper wrapper = new SocketWrapper(clientId, client);

                System.out.println("Подключился новый клиент [" + wrapper + "]");
                clients.put(clientId, wrapper);


                new Thread(() -> {
                    try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {

                        output.println("Подключение успешно. Список всех клиентов: " + clients);
                        while (true) {
                            String clientInput = input.nextLine();

                            if (Objects.equals("q", clientInput)) {
                                // todo разолслать это сообщение остальным клиентам
                                clients.remove(clientId);
                                clients.values().forEach(it -> it.getOutput().println("Клиент [" + clientId + "] отключился"));
                                break;
                            }
                            if (clientInput.startsWith("@")) {
                                // Формат сообщения: "@цифра сообщение"
                                long destinationId = Long.parseLong(clientInput.substring(1, 2));
                                SocketWrapper destination = clients.get(destinationId);
                                destination.getOutput().println("Личное сообщение от клиента[" + clientId + "]: " +
                                        clientInput.substring(2));
                            } else {
                                // Отправляем сообщение всем клиентам, кроме отправителя
                                clients.values()
                                        .stream()
                                        .filter(it -> it.getId() != clientId)
                                        .forEach(it -> it.getOutput().println("Сообщение от клиента[" + clientId + "]: " + clientInput));
                            }

                            long destinationId = Long.parseLong(clientInput.substring(1, 2));
                            SocketWrapper destination = clients.get(destinationId);
                            destination.getOutput().println(clientInput);
                        }
                    }
                }).start();
            }
        }
    }
}

@Getter

class SocketWrapper implements AutoCloseable {
    private final long id;
    private final Socket socket;
    private final Scanner input;
    private final PrintWriter output;

    SocketWrapper(long id, Socket socket) throws IOException {
        this.id = id;
        this.socket = socket;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void close() throws Exception {
        socket.close();

    }

    @Override
    public String toString() {
        return String.format("%s", socket.getInetAddress().toString());
    }
}
