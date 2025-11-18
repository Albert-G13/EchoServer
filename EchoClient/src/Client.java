import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    private final int port;
    private final String host;

    private Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public static Client connectToServer(int port){
        String host = "127.0.0.1";
        return new Client(port, host);
    }

    public void run(){
        System.out.println("Напиши 'Bye', чтобы выйти");

        try(Socket socket = new Socket(host, port);
            Scanner scanner = new Scanner(System.in)){

            Scanner reader = getReader(socket);
            PrintWriter writer = getWriter(socket);

            String welcome = reader.nextLine();
            System.out.println(welcome);
            try (scanner; writer){
                while (true){
                    String message = scanner.nextLine();
                    writer.write(message);
                    writer.write(System.lineSeparator());
                    writer.flush();

                    System.out.printf("Sent message: %s%n", message);

                    message = reader.nextLine();
                    System.out.printf("Got message: %s%n", message);

                    if (message.equalsIgnoreCase("Bye")){
                        return;
                    }
                }
            }
        }catch (NoSuchElementException e){
            System.out.println("Connection dropped");
        }catch (IOException e){
            System.out.printf("Can't connect to server %s:%s %n", host, port);
            e.printStackTrace();
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
}
