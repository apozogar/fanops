package com.softwells.fanops.repository;

import com.softwells.fanops.enums.EstadoCuota;
import com.softwells.fanops.model.SocioEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface SocioRepository extends JpaRepository<SocioEntity, UUID> {

  boolean existsByDni(String dni);

  List<SocioEntity> findByUsuarioUid(UUID usuarioUid);

  List<SocioEntity> findByUsuarioEmail(String email);

  // numeroSocio es único a nivel global (columna con constraint unique), así que la numeración
  // sigue siendo por toda la aplicación y no por peña.
  @Query("SELECT MAX(CAST(s.numeroSocio as INTEGER)) FROM SocioEntity s")
  Optional<Integer> findMaxNumeroSocio();

  // --- El resto de consultas van acotadas a una peña concreta (multi-peña real) ---

  List<SocioEntity> findByPenaId(Long penaId);

  long countByPenaId(Long penaId);

  List<SocioEntity> findByActivoAndPenaId(boolean activo, Long penaId);

  long countByFechaAltaGreaterThanEqualAndPenaId(LocalDate fechaDesde, Long penaId);

  long countByFechaNacimientoAfterAndPenaId(LocalDate fecha, Long penaId);

  @Query("SELECT COUNT(s) FROM SocioEntity s "
      + "WHERE s.pena.id = :penaId AND s.fechaNacimiento <= :fecha")
  long countByFechaNacimientoBeforeOrEqualsAndPenaId(@Param("fecha") LocalDate fecha,
      @Param("penaId") Long penaId);

  @Query("SELECT DISTINCT s FROM SocioEntity s JOIN s.cuotas c "
      + "WHERE c.estado IN :estados AND s.pena.id = :penaId")
  List<SocioEntity> findSociosConCuotasEnEstadosAndPenaId(
      @Param("estados") List<EstadoCuota> estados, @Param("penaId") Long penaId);
}
