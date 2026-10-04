package travel.service;

import java.util.Random;
import java.util.Set;

/**
 * Pembuat nomor konfirmasi acak 6 digit (100000-999999) yang dijamin tidak bentrok
 * dengan nomor yang sedang dipakai. Final + constructor private karena murni utilitas static.
 */
public final class ConfirmationGenerator {

    private static final Random RANDOM = new Random();

    private ConfirmationGenerator() {
    }

    public static int generate(Set<Integer> taken) {
        return generate(taken, RANDOM);
    }

    /** Versi dengan Random yang bisa disuntik, supaya mudah diuji. */
    public static int generate(Set<Integer> taken, Random random) {
        int number;
        do {
            number = 100_000 + random.nextInt(900_000);
        } while (taken.contains(number));
        return number;
    }
}
