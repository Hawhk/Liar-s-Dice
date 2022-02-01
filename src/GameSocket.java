import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class GameSocket {
    private Socket socket;
    private BufferedReader readerChannel;
    private BufferedWriter writerChannel;
    private String line;
    private Integer nInt;

    public GameSocket(Game game) throws IOException {
        socket = Game.server.accept();
        readerChannel = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writerChannel = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        line = null;
        nInt = null;
    }

    public void sendMessage(String msg) {
        try {
            writerChannel.write(msg);
            writerChannel.flush();
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    public String next() {
        String temp = line;
        line = null;
        return temp;
    }

    public int nextInt() {
        Integer temp = nInt;
        nInt = null;
        return temp; 
    }

    public boolean hasInt () throws IOException {
        line = getData();
        boolean result = true;
        try {
            nInt = Integer.parseInt(line);
        }
        catch (NumberFormatException nfe) {
            result = false;
        }
        System.out.println(line);
        return result;
    }

    public boolean hasNext() throws IOException {
        line = getData();
        boolean result = true;
        if (line == null) {
            result = false;
        }

        return result;
    }

    public void quit() throws IOException {
        socket.close();
    }

    private String getData() throws IOException {
        String str = readerChannel.readLine();
        return removeBackstep(str);
    }

    private String removeBackstep(String str) throws IOException {
        StringBuilder sb = new StringBuilder(str);
        int index = str.indexOf('\b', 0);
        while (index != -1) {
            sb.deleteCharAt(index);
            if (index != 0) {
                sb.deleteCharAt(index - 1);
            }
            str = sb.toString();
            index = str.indexOf('\b', 0);
        }
        return str;
    }
}
