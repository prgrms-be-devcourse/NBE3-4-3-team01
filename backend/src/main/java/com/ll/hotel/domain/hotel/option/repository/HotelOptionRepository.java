package com.ll.hotel.domain.hotel.option.repository;

import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelOptionRepository extends JpaRepository<HotelOption, Long> {
    Set<HotelOption> findByNameIn(Collection<String> names);
}
