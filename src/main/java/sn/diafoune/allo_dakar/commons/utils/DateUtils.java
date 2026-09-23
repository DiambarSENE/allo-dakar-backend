package sn.diafoune.allo_dakar.commons.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Le contexte métier initial est le Sénégal (fuseau Africa/Dakar, UTC toute l'année — pas de
 * changement d'heure). Les entités stockent des Instant (UTC) ; ce helper convertit les
 * couples date/heure locales saisis par l'utilisateur (ex. Trip.departureDate/departureTime)
 * vers un Instant non ambigu pour les comparaisons (expiration, tri chronologique).
 */
public final class DateUtils {

    public static final ZoneId SENEGAL_ZONE = ZoneId.of("Africa/Dakar");

    private DateUtils() {
    }

    public static Instant toInstant(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(SENEGAL_ZONE).toInstant();
    }

    public static boolean isPast(LocalDate date, LocalTime time) {
        return toInstant(date, time).isBefore(Instant.now());
    }
}
