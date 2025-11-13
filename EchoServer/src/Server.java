import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public class Server {
    private final int port;
    private final Map<String, Function<String, String>> commands = new HashMap<>();

    private Server(int port) {
        this.port = port;

        commands.put("date", str -> "Сегодня " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        commands.put("time", str -> "Сейчас " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        commands.put("reverse", str -> {
            String[] parts = str.split(":", 2);
            if (parts.length < 2) return  "Не указан текст";
            return new StringBuilder(parts[1].trim()).reverse().toString();
        });
        commands.put("upper", str -> {
            String[] parts = str.split(":", 2);
            if (parts.length < 2) return  "Не указан текст";
            return parts[1].trim().toUpperCase();
        });
    }

    public static Server bindToServer(int port){
        return  new Server(port);
    }

    public String reversed(String str){
        return new StringBuilder(str).reverse().toString();
    }

    public void run(){
        try (ServerSocket server = new ServerSocket(port)){
            //...
            try(Socket socket = server.accept()){
                handle(socket);
            }
        }catch (IOException e){
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) throws IOException{
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        OutputStream outputStream = socket.getOutputStream();

        try (Scanner scanner = new Scanner(inputStreamReader)){

            PrintWriter printWriter = new PrintWriter(outputStream);

            while (true){
                String message = scanner.nextLine().trim();
                System.out.printf("Got message: %s%n", message);
                String reversed = reversed(message);
                printWriter.write(reversed);
                System.out.printf("Sent message: %s%n", reversed);
                printWriter.write(System.lineSeparator());
                printWriter.flush();
                if (message.equalsIgnoreCase("bye")){
                    System.out.println("Bye-bye!");
                    return;
                }
            }
        }catch (NoSuchElementException e){
            System.out.println("Client disconnected");
        }
    }
}
