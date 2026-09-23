package sn.diafoune.allo_dakar.repositories;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Critères de recherche pour GET /api/v1/trips/search. Ne retourne jamais les trajets
 * CANCELLED/EXPIRED/DRAFT, et n'expose que ceux réellement disponibles (places > 0 par défaut).
 */
public final class TripSpecifications {

    private TripSpecifications() {
    }

    public static Specification<Trip> search(
            String departureCity,
            String destinationCity,
            LocalDate departureDate,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minAvailableSeats,
            UUID driverId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("status").in(TripStatus.PUBLISHED, TripStatus.FULL));

            if (departureCity != null && !departureCity.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("departureCity")), departureCity.toLowerCase()));
            }
            if (destinationCity != null && !destinationCity.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("destinationCity")), destinationCity.toLowerCase()));
            }
            if (departureDate != null) {
                predicates.add(cb.equal(root.get("departureDate"), departureDate));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerSeat"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerSeat"), maxPrice));
            }
            int seatsThreshold = (minAvailableSeats == null || minAvailableSeats < 1) ? 1 : minAvailableSeats;
            predicates.add(cb.greaterThanOrEqualTo(root.get("availableSeats"), seatsThreshold));

            if (driverId != null) {
                // driverId ici est l'ID du compte utilisateur (User), pas celui du DriverProfile —
                // cohérent avec TripResponse.driverId (voir TripMapper) : la convention dans tout
                // le reste de l'API est que tout "driverId"/"driverUserId" exposé à l'extérieur
                // identifie l'utilisateur, jamais le profil interne.
                predicates.add(cb.equal(root.get("driver").get("user").get("id"), driverId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
