import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, Function<String, String>> commands;
    private final List<ClientHandler> clients;
    private PrintWriter writer;
    private String userName;

    public ClientHandler(Socket socket, Map<String, Function<String, String>> commands, List<ClientHandler> clients) {
        this.socket = socket;
        this.commands = commands;
        this.clients = clients;
    }
    @Override
    public void run() {
        System.out.printf("Connected client: %s%n", socket);

        try(
                socket;
                Scanner reader = getReader(socket);
                PrintWriter writer = getWriter(socket);
        ){
            sendResponse("Введите ваше имя:", writer);
            while (true) {
                this.userName = reader.nextLine().trim();
                if (userName.isBlank() || userName.contains(" ")){
                    sendResponse("Имя не должно быть пустым и не должно содержать пробелов.", writer);
                }else {
                    break;
                }
            }
            sendResponse("Hello " + userName, writer);

            while (true){
                String message = reader.nextLine().trim();
                if (isEmptyMsg(message) || isQuitMsg(message)){
                    break;
                }
                if (message.startsWith("/")){
                    String commandKey = message.substring(1).split(" ")[0].toLowerCase();
                    if (commands.containsKey(commandKey)){
                        String response = commands.get(commandKey).apply(message);
                        sendResponse(response, writer);
                    }else {
                        sendResponse("Неизвестная команда: " + commandKey, writer);
                    }
                }else {
                    broadcast(message);
                }
            }
        }catch (NoSuchElementException e){
            System.out.println("Client dropped connection");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected");
    }

    public void broadcast(String msg){
        for (ClientHandler client : clients){
            if (client != this){
                try {
                    client.writer.write("<" + this.userName + ">: " + msg);
                    client.writer.write(System.lineSeparator());
                    client.writer.flush();
                }catch (NoSuchElementException e){
                    System.out.printf(client.userName + " отключился...");
                    clients.remove(client);
                }
            }
        }
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        return new PrintWriter(outputStream);
    }

    private Scanner getReader(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new Scanner(inputStreamReader);
    }

    private static boolean isQuitMsg(String msg){
        return "bye".equalsIgnoreCase(msg);
    }

    private static boolean isEmptyMsg(String msg){
        return msg == null || msg.isBlank();
    }

    private static void sendResponse(String response, Writer writer) throws IOException {
        writer.write(response);
        writer.write(System.lineSeparator());
        writer.flush();
    }
}
