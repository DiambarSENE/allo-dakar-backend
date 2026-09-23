package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.web.dtos.review.CreateReviewRequest;
import sn.diafoune.allo_dakar.web.dtos.review.ReviewResponse;

import java.util.UUID;

public interface ReviewService {

    /** Règle §48-5 : Trip.status doit être COMPLETED et l'auteur doit avoir réellement participé. */
    ReviewResponse create(UUID authorId, CreateReviewRequest request);

    Page<ReviewResponse> listForUser(UUID targetUserId, Pageable pageable);

    ReviewResponse update(UUID authorId, UUID reviewId, String comment, Integer rating);

    void delete(UUID authorId, UUID reviewId);

    void moderate(UUID reviewId, UUID moderatorId, boolean hide);
}
