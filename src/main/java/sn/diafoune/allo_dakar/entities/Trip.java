package sn.diafoune.allo_dakar.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Trajet publié par un conducteur.
 *
 * Concurrence : availableSeats est protégé par verrouillage optimiste (@Version). Toute
 * décrémentation se fait dans une transaction avec retry borné en cas de conflit de version
 * (voir BookingServiceImpl). Une contrainte CHECK (available_seats >= 0) en base sert de filet
 * de sécurité supplémentaire — voir migration Flyway correspondante.
 */
@Getter
@Setter
@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(name = "idx_trips_search", columnList = "departure_city, destination_city, departure_date, status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Trip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "departure_city", nullable = false, length = 100)
    private String departureCity;

    @Column(name = "departure_address", length = 255)
    private String departureAddress;

    private Double departureLatitude;
    private Double departureLongitude;

    @Column(name = "destination_city", nullable = false, length = 100)
    private String destinationCity;

    @Column(name = "destination_address", length = 255)
    private String destinationAddress;

    private Double destinationLatitude;
    private Double destinationLongitude;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(length = 255)
    private String meetingPoint;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerSeat;

    @Column(nullable = false)
    private int availableSeats;

    @Column(nullable = false)
    private int totalSeats;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String rules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.DRAFT;

    private Instant publishedAt;

    /** Verrou optimiste utilisé lors de la réservation de places (voir BookingServiceImpl). */
    @Version
    private Long version;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingPoint> meetingPoints = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();
}
