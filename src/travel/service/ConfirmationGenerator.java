package travel.service;

import java.util.Random;
import java.util.Set;

public final class ConfirmationGenerator {

    private static final Random RANDOM = new Random();

    private ConfirmationGenerator() {
    }

    public static int generate(Set<Integer> taken) {
        return generate(taken, RANDOM);
    }

    public static int generate(Set<Integer> taken, Random random) {
        int number;
        do {
            number = 100_000 + random.nextInt(900_000);
        } while (taken.contains(number));
        return number;
    }
}
