public class App {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Args are not correct! <NumberOfDice> <NumberOfPlayers> <NumberOfAi>");
            System.exit(1);
        }

        int numberOfDice = Integer.parseInt(args[0]);
        int players = Integer.parseInt(args[1]);
        int ais = Integer.parseInt(args[2]);
        Game.startServer(4000);
        Game game= new Game(numberOfDice, players, ais);
        while (game.doTurn());
    }
}
