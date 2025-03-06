package com.ll.hotel.domain.hotel.option.repository;

import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomOptionRepository extends JpaRepository<RoomOption, Long> {
    Set<RoomOption> findByNameIn(Collection<String> names);
}
