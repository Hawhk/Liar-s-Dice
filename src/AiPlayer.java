import java.util.Random;
import java.lang.Thread;

public class AiPlayer extends Player {
    public AiPlayer(String name, int nrOfDice, Game game) {
        super(name, nrOfDice, game);
    }

    public Prediction play(Prediction prediction) {
        // TODO: Implement better ai  
        int numberOfWilds = getNrOfWilds();
        if (prediction.face > 0) {
            game.sendMessageln("  Current Guess is: " + prediction.number + " of " + prediction.face + "");
        }
        game.sendMessageln(name.toUpperCase() + " its your turn!");
        try
        {
            Thread.sleep(4000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        Prediction aiPrediction;
        Random rand = new Random();
        int[] myDice = countDice();

        if (prediction.face > 0) {
            boolean bool = false;
            int chanses = Math.round(((float)Game.getNrOfDice() - (float)nrOfDice) / (float)Dice.MAX);
            for (int i = prediction.face; i < Dice.MAX; i++) {
                if (myDice[i] + Math.round(chanses / 2) + numberOfWilds >= prediction.number) {
                    bool = true;
                    break;
                }
            }
            if (bool) {
                if(prediction.number == 0) {
                    game.sendMessageln("should not be accesseble " + prediction.face);
                }
                aiPrediction = new Prediction(prediction.face + 1, prediction.number);
                return aiPrediction;
            } else if(myDice[prediction.face - 1] + chanses + Math.round(rand.nextInt(chanses + 1) - chanses/2 + numberOfWilds) < prediction.number) {
                return null;
            }
        }
        int number;
        int face;
        if (rand.nextInt(5) > 1) {
            int count = 0;
            do {
                face = prediction.face + rand.nextInt(Dice.MAX - prediction.face + 1);
                if (face == 0) {
                    face = 1;
                }
                count++;
            } while ((myDice[face - 1] + numberOfWilds == 0 || myDice[face - 1] + numberOfWilds < prediction.number) && count != 10);
            if ( face == prediction.face || prediction.face == 0) {
                int plusMinus;
                do {
                    plusMinus = Math.round(rand.nextInt(3) - 1);
                    count++;
                } while (myDice[face - 1] + plusMinus + numberOfWilds <= prediction.number && count < 10);
                
                if ( count < 10 || myDice[face - 1] + plusMinus + numberOfWilds > prediction.number ) { //hittadde ett antal som är 
                    if (myDice[face - 1] + plusMinus > prediction.number) {
                        number = myDice[face - 1] + plusMinus;
                    } else {
                        number = myDice[face - 1] + plusMinus;
                        while (number <= prediction.number && numberOfWilds != 0) {
                            number++;
                            numberOfWilds--;
                        }
                    }
                } else {
                    number = prediction.number + 1;
                }
                
            } else {
                number = prediction.number;
            }
             
        } else {
            face = prediction.face + rand.nextInt(Dice.MAX - prediction.face + 1);
            if (face == 0) {
                face = 1 + rand.nextInt(Dice.MAX);
            }

            if (face == prediction.face || prediction.face == 0) {
                number = prediction.number + 1;
            } else {
                number = prediction.number;
            }
        }
        aiPrediction = new Prediction(face, number);
        return aiPrediction;
    }
}
