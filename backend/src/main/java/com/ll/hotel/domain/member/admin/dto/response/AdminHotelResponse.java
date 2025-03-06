package com.ll.hotel.domain.member.admin.dto.response;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminHotelResponse {
    public record ApprovalResult(
            String name,
            HotelStatus status
    ) {
        public static ApprovalResult from(Hotel hotel) {
            return new ApprovalResult(
                    hotel.getHotelName(),
                    hotel.getHotelStatus()
            );
        }
    }

    public record Summary(
            Long hotelId,
            String name,
            String streetAddress,
            String ownerName,
            HotelStatus status
    ) {
        public static Summary from(Hotel hotel) {
            Business business = hotel.getBusiness();
            Member owner = business.getMember();
            return new Summary(
                    hotel.getId(),
                    hotel.getHotelName(),
                    hotel.getStreetAddress(),
                    owner.getMemberName(),
                    hotel.getHotelStatus()
            );
        }
    }

    public record Detail(
            String hotelName,
            String streetAddress,
            Integer zipCode,
            Integer hotelGrade,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            HotelStatus hotelStatus,
            
            String hotelEmail,
            String hotelPhoneNumber,

            Long ownerId,
            String ownerName,
            String businessRegistrationNumber,
            LocalDate startDate,

            Double averageRating,
            Long totalReviewCount,

            Set<String> hotelOptions
    ) {
        public static Detail from(Hotel hotel) {
            Business business = hotel.getBusiness();
            return new Detail(
                    hotel.getHotelName(),
                    hotel.getStreetAddress(),
                    hotel.getZipCode(),
                    hotel.getHotelGrade(),
                    hotel.getCheckInTime(),
                    hotel.getCheckOutTime(),
                    hotel.getHotelStatus(),

                    hotel.getHotelEmail(),
                    hotel.getHotelPhoneNumber(),

                    business.getId(),
                    business.getOwnerName(),
                    business.getBusinessRegistrationNumber(),
                    business.getStartDate(),

                    hotel.getAverageRating(),
                    hotel.getTotalReviewCount(),
                    hotel.getHotelOptions() != null
                            ? hotel.getHotelOptions().stream()
                            .map(HotelOption::getName)
                            .collect(Collectors.toSet())
                            : new HashSet<>()
            );
        }
    }
}
