import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestingCode {
    public static void main(String[] args) {
//        Random rand = new Random();
//        randomNum = minimum + rand.nextInt((maximum - minimum) + 1);
        int rand = ThreadLocalRandom.current().nextInt(0,9);
        System.out.println(rand);
    }
}
