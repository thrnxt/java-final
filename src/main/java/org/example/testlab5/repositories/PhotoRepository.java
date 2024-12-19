package org.example.testlab5.repositories;

import org.example.testlab5.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo,Long> {
}
