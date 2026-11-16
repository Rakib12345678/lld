package com.rockydrive.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Shared representation of a user, used wherever services need to embed basic user information
 * (e.g. as the actor on an event, or the "shared with" party) without pulling in the full auth
 * domain model.
 *
 * <p>Data contract - additive changes only once consumed by 2+ services: never rename or remove
 * existing fields.
 *
 * @param userId      unique id of the user
 * @param email       user's email address
 * @param displayName user-facing display name
 * @param createdAt   time the user account was created
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDTO(
        @JsonProperty("userId") String userId,
        @JsonProperty("email") String email,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("createdAt") Instant createdAt
) {

    @JsonCreator
    public UserDTO {
    }
}
