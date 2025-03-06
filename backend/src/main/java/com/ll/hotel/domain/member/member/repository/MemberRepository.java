package com.ll.hotel.domain.member.member.repository;

import com.ll.hotel.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberEmail(String memberEmail);
    Optional<Member> findByMemberName(String nickname);
    boolean existsByMemberEmail(String memberEmail);
}