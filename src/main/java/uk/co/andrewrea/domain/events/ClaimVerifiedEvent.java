package uk.co.andrewrea.domain.events;

import uk.co.andrewrea.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimVerifiedEvent {
    public static final String NAME = "claim-verified-event";

    public String id;
    public ClaimDto claim;
}
