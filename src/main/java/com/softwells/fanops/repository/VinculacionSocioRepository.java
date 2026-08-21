package com.softwells.fanops.repository;

import com.softwells.fanops.model.VinculacionSocioEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculacionSocioRepository
    extends JpaRepository<VinculacionSocioEntity, UUID> {

  Optional<VinculacionSocioEntity> findByTokenHash(String tokenHash);

  List<VinculacionSocioEntity> findByEmailIgnoreCaseAndFechaUsoIsNull(String email);
}
