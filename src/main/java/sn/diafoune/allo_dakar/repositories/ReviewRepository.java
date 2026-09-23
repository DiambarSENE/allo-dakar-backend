package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.diafoune.allo_dakar.entities.Review;
import sn.diafoune.allo_dakar.entities.enums.ReviewStatus;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByTargetUserIdAndStatus(UUID targetUserId, ReviewStatus status, Pageable pageable);

    boolean existsByTripIdAndAuthorIdAndTargetUserId(UUID tripId, UUID authorId, UUID targetUserId);

    @Query("select avg(r.rating) from Review r where r.targetUser.id = :targetUserId and r.status = 'PUBLISHED'")
    Double computeAverageRating(@Param("targetUserId") UUID targetUserId);
}
