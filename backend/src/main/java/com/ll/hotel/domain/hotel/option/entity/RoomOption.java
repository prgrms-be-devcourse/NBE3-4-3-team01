package com.ll.hotel.domain.hotel.option.entity;

import com.ll.hotel.domain.hotel.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "room_option")
public class RoomOption extends BaseOption {
    @ManyToMany(mappedBy = "roomOptions")
    private Set<Room> rooms = new HashSet<>();

    @Builder
    public RoomOption(String name) {
        super(name);
        this.rooms = (rooms != null) ? rooms : new HashSet<>();
    }
}
