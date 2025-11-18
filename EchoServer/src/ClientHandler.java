import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, Function<String, String>> commands;

    public ClientHandler(Socket socket, Map<String, Function<String, String>> commands) {
        this.socket = socket;
        this.commands = commands;
    }
    @Override
    public void run() {
        System.out.printf("Connected client: %s%n", socket);

        try(
                socket;
                Scanner reader = getReader(socket);
                PrintWriter writer = getWriter(socket);
        ){
            sendResponse("Hello " + socket.getPort(), writer);

            while (true){
                String message = reader.nextLine().trim();
                if (isEmptyMsg(message) || isQuitMsg(message)){
                    break;
                }

                System.out.printf("Got message: %s%n", message);

                String response = commands.getOrDefault(
                        message.split(" ")[0].toLowerCase(),
                        str -> str).apply(message);
                System.out.printf("Sent message: %s%n", response);
                sendResponse(response, writer);
            }
        }catch (NoSuchElementException e){
            System.out.println("Client dropped connection");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected");
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
