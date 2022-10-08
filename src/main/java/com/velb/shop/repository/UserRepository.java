package com.velb.shop.repository;

import com.velb.shop.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u " +
            "where u.id = :id")
    Optional<User> findByIdFetchBasket(Long id);

    Optional<User> findByEmail(String email);
}
