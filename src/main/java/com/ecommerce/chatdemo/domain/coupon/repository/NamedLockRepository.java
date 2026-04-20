package com.ecommerce.chatdemo.domain.coupon.repository;

import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NamedLockRepository extends JpaRepository<Coupon, Long> {

    @Query(value = "SELECT GET_LOCK(:lockName, :timeout)", nativeQuery = true)
    Integer getLock(
    @Param("lockName") String lockName,
    @Param("timeout") Integer timeout
    );

    @Query(value = "SELECT RELEASE_LOCK(:lockName)", nativeQuery = true)
    Integer releaseLock(
            @Param("lockName") String lockName
    );
}
