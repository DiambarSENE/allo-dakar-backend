package sn.diafoune.allo_dakar.commons.utils;

import java.security.SecureRandom;
import java.time.Year;

/**
 * Génère des références métier lisibles (BK-2026-XXXXXXXX, PAY-2026-XXXXXXXX) — voir §51.
 * L'unicité effective est garantie par la contrainte UNIQUE en base (uk_bookings_reference,
 * uk_payments_reference) ; en cas de collision improbable, le service doit régénérer et retenter.
 */
public final class ReferenceGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ReferenceGenerator() {
    }

    public static String bookingReference() {
        return generate("BK");
    }

    public static String paymentReference() {
        return generate("PAY");
    }

    private static String generate(String prefix) {
        StringBuilder suffix = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            suffix.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return "%s-%d-%s".formatted(prefix, Year.now().getValue(), suffix);
    }
}
