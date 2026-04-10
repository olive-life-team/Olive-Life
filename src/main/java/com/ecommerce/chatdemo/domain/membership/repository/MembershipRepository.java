package com.ecommerce.chatdemo.domain.membership.repository;

import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findFirstByOrderByMinSpentAmountAsc();
}