package com.nedap.workshop.syntaxis.repository;

import com.nedap.workshop.syntaxis.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
