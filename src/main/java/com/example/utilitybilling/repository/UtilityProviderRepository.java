package com.example.utilitybilling.repository;

import com.example.utilitybilling.model.UtilityProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UtilityProviderRepository extends JpaRepository<UtilityProvider, UUID> {
}