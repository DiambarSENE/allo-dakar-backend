package sn.diafoune.allo_dakar.web.dtos.common;

/**
 * Enveloppe de réponse générique. Utiliser ApiResponse<Void> ou omettre "data" pour les
 * réponses sans contenu (ex. 204). Pour les listes paginées, voir PagedResponse.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> of(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }
}
