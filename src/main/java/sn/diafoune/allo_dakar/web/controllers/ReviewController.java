package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.ReviewService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.review.CreateReviewRequest;
import sn.diafoune.allo_dakar.web.dtos.review.ReviewResponse;
import sn.diafoune.allo_dakar.web.dtos.review.UpdateReviewRequest;

import java.util.UUID;

/**
 * Chemins délibérément non uniformes (POST/PUT/DELETE sous /reviews, GET sous /users/{id}/reviews)
 * pour coller exactement à la liste d'endpoints du cahier des charges (§49).
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/v1/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Avis publié", response));
    }

    @GetMapping("/api/v1/users/{id}/reviews")
    public PagedResponse<ReviewResponse> listForUser(@PathVariable UUID id,
                                                       @RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Avis récupérés",
                reviewService.listForUser(id, PaginationUtils.of(page, size, null, null)));
    }

    @PutMapping("/api/v1/reviews/{id}")
    public ApiResponse<ReviewResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest request) {
        return ApiResponse.of("Avis mis à jour",
                reviewService.update(SecurityUtils.currentUserId(), id, request.comment(), request.rating()));
    }

    @DeleteMapping("/api/v1/reviews/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        reviewService.delete(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok("Avis supprimé");
    }
}
