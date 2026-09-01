package com.softwells.fanops.repository;

import com.softwells.fanops.model.ValoresEventoPenaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ValoresEventoPenaRepository extends JpaRepository<ValoresEventoPenaEntity, UUID> {

  Optional<ValoresEventoPenaEntity> findByPenaId(Long penaId);
}
