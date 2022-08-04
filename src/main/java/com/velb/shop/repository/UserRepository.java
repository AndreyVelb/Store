package com.velb.shop.repository;

import com.velb.shop.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u " +
            "left join fetch u.basket " +
            "where u.id = :id")
    Optional<User> findByIdFetchBasket(Long id);

    Optional<User> findByEmail(String email);
}
