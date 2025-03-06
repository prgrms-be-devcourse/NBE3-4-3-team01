package com.ll.hotel.domain.booking.payment.controller;

import com.ll.hotel.domain.booking.payment.dto.UidResponse;
import com.ll.hotel.domain.booking.payment.service.PaymentService;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings/payments")
@RequiredArgsConstructor
@Tag(name = "PaymentController", description = "결제 관련 API")
public class PaymentController {
    private final PaymentService paymentService;
    private final Rq rq;

    @GetMapping("/uid")
    @Operation(summary = "결제 Uid 발급", description = "결제에 필요한 Uid, Key를 발급하는 api")
    public RsData<UidResponse> getUid() {
        Member actor = rq.getActor();

        return RsData.success(
                HttpStatus.OK,
                paymentService.generateMerchantUid()
        );
    }
}