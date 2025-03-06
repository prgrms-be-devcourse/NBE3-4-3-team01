package com.ll.hotel.domain.hotel.option.service;

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository;
import com.ll.hotel.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelOptionService {
    private final HotelOptionRepository hotelOptionRepository;

    @Transactional
    public HotelOption add(OptionRequest optionRequest) {
        HotelOption hotelOption = HotelOption
                .builder()
                .name(optionRequest.name())
                .build();

        return hotelOptionRepository.save(hotelOption);
    }

    public List<HotelOption> findAll() {
        return hotelOptionRepository.findAll();
    }

    public HotelOption findById(Long id) {
        return hotelOptionRepository.findById(id)
                .orElseThrow(ErrorCode.HOTEL_OPTION_NOT_FOUND::throwServiceException);
    }

    @Transactional
    public void modify(HotelOption hotelOption, OptionRequest optionRequest) {
        hotelOption.setName(optionRequest.name());
    }

    public void flush() {
        hotelOptionRepository.flush();
    }

    public void delete(HotelOption hotelOption) {

        if (!hotelOption.getHotels().isEmpty()) {
            ErrorCode.OPTION_IN_USE.throwServiceException();
        }

        hotelOptionRepository.delete(hotelOption);
    }
}
