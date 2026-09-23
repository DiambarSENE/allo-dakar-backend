package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Dispute;
import sn.diafoune.allo_dakar.entities.DisputeAction;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeActionResponse;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeResponse;

@Component
public class DisputeMapper {

    public DisputeResponse toResponse(Dispute dispute) {
        return new DisputeResponse(
                dispute.getId(),
                dispute.getRaisedBy().getId(),
                dispute.getRaisedBy().getFirstName() + " " + dispute.getRaisedBy().getLastName(),
                dispute.getBooking() != null ? dispute.getBooking().getId() : null,
                dispute.getPayment() != null ? dispute.getPayment().getId() : null,
                dispute.getType(),
                dispute.getStatus(),
                dispute.getDescription(),
                dispute.getResolution(),
                dispute.getResolvedAt(),
                dispute.getCreatedAt()
        );
    }

    public DisputeActionResponse toActionResponse(DisputeAction action) {
        return new DisputeActionResponse(
                action.getId(),
                action.getPerformedBy().getFirstName() + " " + action.getPerformedBy().getLastName(),
                action.getAction(),
                action.getNote(),
                action.getCreatedAt()
        );
    }
}
