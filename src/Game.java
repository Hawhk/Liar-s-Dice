import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.net.ServerSocket;

public class Game {
    private int turn;
    private Player[] players;
    private int turnStarter;
    private Prediction currPrediction;
    private int nrOfPlayers;
    private static int nrOfDice;
    private static boolean wilds = true;
    public static ServerSocket server;
    private ArrayList<Spectator> spectaters = new ArrayList<>();

    public Game(int nrOfDice, int players, int aiPlayers) throws IOException {
        this.turn = 0;
        this.nrOfPlayers = players + aiPlayers;
        this.players = new Player[nrOfPlayers];
        Random rand = new Random();
        int index;
        for (int i = 0; i < players; i++) {
            index = rand.nextInt(this.nrOfPlayers);
            while (this.players[index] != null) {
                index = rand.nextInt(this.nrOfPlayers);
            }
            String name = "Player " + (i + 1);
            sendMessageln("Waiting for " + name);
            this.players[index] = new Player(nrOfDice, name, this);
            this.players[index].start();
        }
        System.out.println("Waiting for players to give name");
        for (int i = 0; i < this.nrOfPlayers; i++) {
            while(this.players[i] != null && this.players[i].isAlive());
        }
        index = 0;
        for (int i = 0; i < aiPlayers; i++) {
            while (this.players[index] != null) {
                index++;
            }
            this.players[index++] = new AiPlayer("Ai " + (i + 1), nrOfDice, this);
        }
        
        this.turnStarter = rand.nextInt(this.nrOfPlayers);
        this.currPrediction = new Prediction(0, 0);
        Game.nrOfDice = getNrOfDice(this.nrOfPlayers);
    }

    public int getNrOfPlayers() {
        return nrOfPlayers;
    }

    private int getNrOfDice(int index) {
        int num = 0;
        if (index == this.nrOfPlayers) {
            for (Player player : players) {
                num += player.getNrOfDice();
            }
        } else if (index >= 0 && index < this.nrOfPlayers) {
            num = this.players[index].getNrOfDice();
        } else {
            return this.getNrOfDice(index-1);
        }
        return num;
    }

    public static int getNrOfDice() {
        return nrOfDice;
    }

    public boolean doTurn () throws Exception {
        if(nrOfPlayers == 1) {
            sendMessageln("!!Winner!! " + players[0].name + " !!Winner!!");
            Thread.sleep(1000);
            if (isAPlayer(players[0])) {
                players[0].socket.quit();
            } 
            int size = spectaters.size();
            for (int i = 0; i < size; i++) {
                if (spectaters.get(i).isAlive()) {
                    spectaters.get(i).socket.quit();
                }
            }
            return false;
        } else if (this.currPrediction.face == 0) {
            startNewTurn();
        }
        for (int i = turnStarter; i < turnStarter + nrOfPlayers; i++) {
            int index = i % nrOfPlayers;
            Prediction playerPrediction = players[index].play(currPrediction);
            
            if (playerPrediction == null) {
                bluff(index);
                break;
            } else {
                currPrediction = playerPrediction;
                if (currPrediction.face == 1) {
                    Game.wilds = false;
                    sendMessageln("Once are no longer wilds!");
                }
            }
        }
        return true;
    }

    private void bluff(int index) {
        int[] total = countDice();
        int prevIndex = (this.nrOfPlayers + index - 1)%this.nrOfPlayers;
        Player prev = players[prevIndex];
        int removeIndex = -1;
        if (total[currPrediction.face - 1] + getNrOfWilds() < currPrediction.number) {
            if(didBluff(index, prev, total)) {
                removeIndex = prevIndex;
                turnStarter = (this.nrOfPlayers + index - 2)%(this.nrOfPlayers-1);
            }
        } else {
            if(didNotBluff(index, prev, total)) {
                removeIndex = index;
                if(turnStarter == nrOfPlayers - 1) {
                    turnStarter--;
                }
            }
        }
        if (removeIndex >= 0) {
            removePlayer(removeIndex);
        }
        nrOfDice = getNrOfDice(nrOfPlayers);
        currPrediction = new Prediction(0, 0);
    }

    private void removePlayer(int removeIndex) {
        sendMessageln(players[removeIndex].name.toUpperCase() + " have lost all its dice!");
        Player[] tmp = new Player[nrOfPlayers - 1];
        for (int i = 0; i < nrOfPlayers - 1; i++) {
            if (i == removeIndex && isAPlayer(players[i])) {
                spectaters.add(new Spectator(players[i].socket, players[i].name));
                spectaters.get(spectaters.size() - 1).start();
                System.out.println(players[i].name + " was made a spectaters!");
                players[i].sendMessage("You (" + players[i].name + ") are now a spectator!\n\r");
                
                tmp[i] = players[i + 1];
            } else if (i >= removeIndex) {
                tmp[i] = players[i + 1];
            } else {
                tmp[i] = players[i];
            }
        }
        players = tmp;
        nrOfPlayers--;
    }

    private boolean didBluff(int index, Player prev, int[] total) {
        int wilds = getNrOfWilds();
        boolean noDice = prev.removeDice();
        turnStarter = index;
        String nameCurr = players[index].getUserName();
        String namePrev = prev.getUserName();
        String format = "%S called bluff and %S did bluff. %S guessed: %d of %d but there is only %d (+%d wild) for a total of %d of %d!";
        String result = String.format(
            format,
            nameCurr,
            namePrev,
            namePrev,
            this.currPrediction.number,
            this.currPrediction.face,
            total[currPrediction.face -1],
            wilds,
            wilds + total[currPrediction.face -1],
            currPrediction.face
        );
        sendMessageln(result);
        return noDice;
    }

    private boolean didNotBluff(int index, Player prev, int[] total) {
        int wilds = getNrOfWilds();
        boolean noDice = players[index].removeDice();
        turnStarter = (nrOfPlayers + index -1) % nrOfPlayers;
        String nameCurr = players[index].getUserName();
        String namePrev = prev.getUserName();
        String format = "%S called bluff and %S didn't bluff. %S guessed: %d of %d and there is %d (+%d wild) for a total of %d of %d!";
        String result = String.format(
            format, 
            nameCurr,
            namePrev,
            namePrev,
            this.currPrediction.number,
            this.currPrediction.face,
            total[currPrediction.face -1],
            wilds,
            wilds + total[currPrediction.face -1],
            currPrediction.face
        );
        sendMessageln(result);
        return noDice;
    }

    private void startNewTurn() {
        for (Player player : players) {
            player.throwDice();
        }
        Game.wilds = true;
        this.turn++;
        sendMessageln("Round: " + turn);
        sendMessageln(players[turnStarter].getUserName().toUpperCase() + " are first to guess!");
    }
    
    public int[] countDice () {
        int[] total = {0, 0, 0, 0, 0, 0};
        for (Player player : players) {
            int[] tmp = player.countDice();
            for (int i = 0; i < Dice.MAX; i++) {
                total[i] += tmp[i];
            }
        }
        return total;
    }

    static boolean getWilds() {
        return wilds;
    }

    public int getNrOfWilds() {
        int count = 0;
        if (!Game.getWilds()) {
            return count;
        }
        for (Player player : players) {
            count += player.getNrOfWilds();
        }
        return count;
    }

    public static void startServer(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public void sendMessage(String msg) {
        for (int i = 0; i < players.length ; i++) {
            if (players[i] != null && isAPlayer(players[i]) && !players[i].isAlive()) {
                players[i].sendMessage(msg);
            }
        }
        for (int i = 0; i < spectaters.size(); i++) {
            if (spectaters.get(i).isAlive()) {
                spectaters.get(i).sendMessage(msg);
            } else if (spectaters.get(i).getSpectating()) {
                spectaters.remove(i);
            }
        }
        System.out.print(msg);
    }

    public void sendMessageln(String msg) {
        sendMessage(msg + "\n\r");
    }

    private Boolean isAPlayer(Player player) {
        return player.getClass().getSimpleName().equals("Player");
    }
}
