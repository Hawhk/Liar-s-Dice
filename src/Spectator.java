import java.io.IOException;

public class Spectator extends Thread  {
    public GameSocket socket;
    private Boolean spectating;
    private String name;
    Spectator (GameSocket socket, String name ) {
        this.socket = socket;
        this.spectating = true;
        this.name = name;

        System.out.println("Created spectator: " + this.name + "!");
    }

    public void run() {
        System.out.println("running!");
        try {
            while(socket.hasNext() && spectating) {
                String msg = socket.next();
                System.out.println("Spectator have sent: "+ msg );
                switch (msg.toLowerCase()) {
                    case "q":
                    case "quit":
                        spectating = false;
                        break;
                
                    default:
                        sendMessageln("No comand: \"" + msg + "\"");
                        break;
                }
            }
            sendMessageln("Bye!");
            socket.quit();
        } catch (IOException ioe) {
            System.out.println("Spectator err: " + ioe);
        }
    }
    public void sendMessage(String msg) {
        socket.sendMessage(msg);
        System.out.print(msg);
    }
    private void sendMessageln(String msg) {
        sendMessage(msg + "\n\r");
    }

    public Boolean getSpectating() {
        return spectating;
    }
}
