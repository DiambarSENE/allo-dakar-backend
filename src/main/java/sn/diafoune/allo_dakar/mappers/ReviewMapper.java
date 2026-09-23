package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Review;
import sn.diafoune.allo_dakar.web.dtos.review.ReviewResponse;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        String authorFullName = review.getAuthor().getFirstName() + " " + review.getAuthor().getLastName();
        return new ReviewResponse(
                review.getId(),
                review.getTrip().getId(),
                review.getAuthor().getId(),
                authorFullName,
                review.getTargetUser().getId(),
                review.getRating(),
                review.getComment(),
                review.getStatus()
        );
    }
}
