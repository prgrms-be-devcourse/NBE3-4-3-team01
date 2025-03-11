package com.ll.hotel.global.mail

import com.ll.hotel.domain.booking.booking.dto.BookingResponseDetails
import com.ll.hotel.domain.member.member.dto.MemberDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class MailService {
    @Autowired
    private lateinit var mailSender: JavaMailSender

    @Async
    fun sendBookingConfirmedMail(booking: BookingResponseDetails) {
        val member: MemberDTO = booking.member
        val subject = "[서울호텔] ${member.memberName} 님의 예약이 확정되었습니다."
        val content = """
            ${member.memberName} 님의 예약 내역입니다.

            예약 번호: ${booking.bookNumber}
            호텔명: ${booking.hotel.hotelName}
            객실: ${booking.room.roomName}
            숙박 일정: ${booking.checkInDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))} ~ ${booking.checkOutDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))}
            결제 금액: ${booking.payment.amount} 원

            서울호텔을 이용해주셔서 감사합니다.
        """.trimIndent()

        sendSimpleMail(member.memberEmail, subject, content)
    }

    @Async
    fun sendBookingCancelledMail(booking: BookingResponseDetails) {
        val member: MemberDTO = booking.member
        val subject = "[서울호텔] ${member.memberName} 님의 예약이 취소되었습니다."
        val content = """
            ${member.memberName} 님의 예약이 ${booking.modifiedAt.format(DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분"))}에 취소되었습니다.

            예약 번호: ${booking.bookNumber}
            호텔명: ${booking.hotel.hotelName}
            객실: ${booking.room.roomName}
            숙박 일정: ${booking.checkInDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))} ~ ${booking.checkOutDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))}
            결제 금액: ${booking.payment.amount} 원

            서울호텔을 이용해주셔서 감사합니다.
        """.trimIndent()

        sendSimpleMail(member.memberEmail, subject, content)
    }

    private fun sendSimpleMail(to: String, subject: String, content: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = subject
        message.text = content
        mailSender.send(message)
    }
}