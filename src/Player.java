import java.io.IOException;
import java.util.Scanner;


public class Player extends Thread {
    protected int nrOfDice;
    protected String name;
    protected Dice[] dice;
    protected Scanner input = new Scanner(System.in);
    protected GameSocket socket;
    protected Game game;

    public Player(String name, int nrOfDice, Game game) {
        setValues(name, nrOfDice, game);
    }

    public Player(int nrOfDice, String name, Game game) throws IOException {
        socket = new GameSocket(game);
        setValues(name, nrOfDice, game);
    }

    private void setValues(String name, int nrOfDice, Game game) {
        this.name = name;
        this.nrOfDice = nrOfDice;
        this.dice = new Dice[this.nrOfDice];
        this.game = game;
        for (int i = 0; i < this.nrOfDice; i++) {
            this.dice[i] = new Dice();
        }
    }

    public void throwDice() {
        for (Dice dice_ : dice) {
            dice_.throwDice();
        }
    }

    public Prediction play(Prediction prediction) throws IOException {
        if (prediction.face > 0) {
            game.sendMessageln("  Current Guess is: " + prediction.number + " of " + prediction.face + "");
        } else if (prediction.face == 0 && prediction.number > 0) {
            game.sendMessageln("there should be no predictions with zeros!");
        }
        game.sendMessageln(name.toUpperCase() + " its your turn!");

        Integer face = getFace(prediction.face);
        if (face == null) return null;

        Integer number;
        if (face > prediction.face && prediction.face != 0) {
            number = prediction.number;
        } else {
            number = getNumber(prediction.number);
        }
        sendMessagelnLog("You guessed " + number + " of " + face + "!");
        Prediction myPrediction = new Prediction(face, number);
        return myPrediction;
    }

    private Integer getFace(int face) throws IOException {
        Integer myFace = null;
        if (face >= 0) {
            sendMessageLog("What face do you want to use? ");
            while(!socket.hasInt()) {
                String str = socket.next();
                if ((str.toLowerCase().equals("bluff") || str.toLowerCase().equals("b")) && face != 0) {
                    return null;
                }
                if (str.toLowerCase().equals("show") || str.toLowerCase().equals("s")) {
                    showDice();

                    sendMessageLog("What face do you want to use? ");
                } else {
                    sendMessagelnLog("Please input a number");
                    sendMessageLog("What face do you want to use? ");
                }   
            }
            myFace = socket.nextInt();
        } else {
            System.err.println("Unreacheble in getFace");
        }

        if (myFace < Dice.MIN || myFace > Dice.MAX) {
            sendMessagelnLog("The number must be between " + ((face - 1 > 0) ? face - 1 : Dice.MIN - 1)  + " and " + (Dice.MAX + 1));
            return getFace(face);
        } else if (myFace < face) {
            sendMessagelnLog("The number must be greater or equal to: " + face);
            return getFace(face);
        }

        return myFace;
    }

    private Integer getNumber(int nr) throws IOException {

        Integer myNr = null;
        if (nr >= 0) {
            sendMessageLog("What number do you want to use? ");
            while(!socket.hasInt()) {
                socket.next();
                sendMessagelnLog("Please input a number");
                sendMessageLog("What number do you want to use? ");
            }
            myNr = socket.nextInt();
        } else {
            System.err.println("Unreacheble in getNumber");
        }

        if (myNr <= nr) {
            sendMessagelnLog("The number must be greater or equal to: " + (nr + 1));
            return getNumber(nr);
        }

        return myNr;
    }

    public int[] countDice() {
        int[] count = {0, 0, 0, 0, 0, 0};
        for (Dice dice_ : dice) {
            count[dice_.getValue() - 1]++;
        }
        return count;
    }  

    public boolean removeDice() {
        int length = this.dice.length - 1;
        this.dice = new Dice[length];
        for (int i = 0; i < length; i++) {
            this.dice[i] = new Dice();
        }
        this.nrOfDice--;
        if (nrOfDice == 0) {
            return true;
        }
        return false;
    }

    public void showDice() {
        for (Dice dice_ : dice) {
            sendMessageLog(dice_.getValue() + " ");
        }
        sendMessagelnLog("");
    }

    public int getNrOfWilds() {
        int count = 0;
        if (!Game.getWilds()) {
            return count;
        }
        for (Dice dice_ : dice) {
            if (dice_.getValue() == 1) {
                count++;
            }
        }
        return count;
    }

    public int getNrOfDice() {
        return nrOfDice;
    }

    public String getUserName() {
        return name;
    }

    public void sendMessage(String msg) {
        socket.sendMessage(msg);
    }

    private void sendMessagelnLog(String msg) {
        System.out.println(msg);
        socket.sendMessage(msg + "\n\r");
    }

    private void sendMessageLog(String msg) {
        System.out.print(msg);
        socket.sendMessage(msg);
    }

    public void run() {
        sendMessage("Hello " + name + ", welcome to Liar's Dice!\n\r");
        sendMessage("Please enter a name: ");
        try {
            socket.hasNext();
        } 
        catch (IOException ioe) {
            System.out.println("err: " + ioe);
        }
        String tmp = socket.next();
        if (!tmp.equals("\n\r") && !tmp.equals("\n") && !tmp.equals("")) {
            name = tmp.toUpperCase();
        }

        sendMessagelnLog("You are now known as \"" + name.toUpperCase() + "\"");
        game.sendMessageln(name + " have joined!");
    }
}
