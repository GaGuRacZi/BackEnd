package com.gaguraczi.paw.domain.auth.repository;

import com.gaguraczi.paw.domain.auth.entity.OAuth;
import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    Optional<OAuth> findByProviderIdAndProviderType(String providerId, SocialType providerType);

    Optional<OAuth> findByEmailAndProviderType(String email, SocialType providerType);

    boolean existsByEmailAndProviderType(String email, SocialType providerType);

    boolean existsByUserAndProviderType(User user, SocialType providerType);

    boolean existsByProviderIdAndProviderType(String providerId, SocialType providerType);

    Optional<OAuth> findByUserAndProviderType(User user, SocialType providerType);

    java.util.List<OAuth> findAllByUser(User user);
}
