package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.mailbox.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByPrimaryEmail(String primaryEmail);
}
