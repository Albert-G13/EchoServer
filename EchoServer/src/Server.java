import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;


public class Server {
    private final int port;
    private final Map<String, Function<String, String>> commands = new HashMap<>();

    private final ExecutorService pool = Executors.newCachedThreadPool();

    private final ArrayList<ClientHandler> clients = new ArrayList<>();

    Server(int port) {
        this.port = port;

        commands.put("date", str -> "Сегодня " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        commands.put("time", str -> "Сейчас " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        commands.put("reverse", str -> {
            String[] parts = str.split(" ", 2);
            if (parts.length < 2) return  "Не указан текст";
            return new StringBuilder(parts[1].trim()).reverse().toString();
        });
        commands.put("upper", str -> {
            String[] parts = str.split(" ", 2);
            if (parts.length < 2) return  "Не указан текст";
            return parts[1].trim().toUpperCase();
        });
    }

    public static Server bindToServer(int port){
        return  new Server(port);
    }

    public void run(){
        try (ServerSocket server = new ServerSocket(port)){
            while (!server.isClosed()){
                Socket socket = server.accept();
                ClientHandler client = new ClientHandler(socket, commands, clients);
                clients.add(client);
                pool.submit(client);
            }
        }catch (IOException e){
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }
}
