package com.ll.hotel.domain.hotel.option.entity;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "hotel_option")
public class HotelOption extends BaseOption {
    @ManyToMany(mappedBy = "hotelOptions")
    private Set<Hotel> hotels = new HashSet<>();

    @Builder
    public HotelOption(String name) {
        super(name);
        this.hotels = (hotels != null) ? hotels : new HashSet<>();
    }
}
