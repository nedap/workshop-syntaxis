package com.nedap.workshop.syntaxis.repository;

import com.nedap.workshop.syntaxis.domain.Client;
import com.nedap.workshop.syntaxis.domain.Composition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompositionRepository extends JpaRepository<Composition, Long> {
    List<Composition> findAllByClient(Client client);
}
