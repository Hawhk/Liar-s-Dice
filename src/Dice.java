import java.util.Random;

public class Dice {
    public static final int MIN = 1;
    public static final int MAX = 6;
    private int value;


    public Dice() {
        throwDice();
    }

    public int getValue() {
        return this.value;
    }

    public void throwDice() {
        Random rand = new Random();
        this.value = MIN + rand.nextInt(MAX);
    }
}
