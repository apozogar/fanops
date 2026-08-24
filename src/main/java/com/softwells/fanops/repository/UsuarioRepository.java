package com.softwells.fanops.repository;

import com.softwells.fanops.model.UsuarioEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
  @Query(
      "SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.socios s LEFT JOIN FETCH s.pena WHERE lower(u.email) = lower(:email)")
  Optional<UsuarioEntity> findByEmailIgnoreCase(@Param("email") String email);

  /**
   * Sella el último acceso de la cuenta. Va como update directo y no leyendo la entidad porque se
   * ejecuta en cada login y no necesita traerse el usuario con todas sus fichas de socio.
   */
  @Modifying
  @Query("UPDATE UsuarioEntity u SET u.ultimoAcceso = :momento WHERE lower(u.email) = lower(:email)")
  int registrarAcceso(@Param("email") String email, @Param("momento") LocalDateTime momento);
}
