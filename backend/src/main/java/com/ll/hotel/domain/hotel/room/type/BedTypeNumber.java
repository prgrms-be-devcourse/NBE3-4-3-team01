package com.ll.hotel.domain.hotel.room.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import java.util.Map;

@Embeddable
public record BedTypeNumber(
        @JsonProperty("SINGLE")
        int bed_single,

        @JsonProperty("DOUBLE")
        int bed_double,

        @JsonProperty("QUEEN")
        int bed_queen,

        @JsonProperty("KING")
        int bed_king,

        @JsonProperty("TWIN")
        int bed_twin,

        @JsonProperty("TRIPLE")
        int bed_triple
) {
    public static BedTypeNumber fromJson(Map<String, Integer> bedTypeNumber) {
        return new BedTypeNumber(
                bedTypeNumber.getOrDefault("SINGLE", 0),
                bedTypeNumber.getOrDefault("DOUBLE", 0),
                bedTypeNumber.getOrDefault("QUEEN", 0),
                bedTypeNumber.getOrDefault("KING", 0),
                bedTypeNumber.getOrDefault("TWIN", 0),
                bedTypeNumber.getOrDefault("TRIPLE", 0)
        );
    }
}