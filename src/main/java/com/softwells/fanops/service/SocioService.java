package com.softwells.fanops.service;

import com.softwells.fanops.controller.dto.CarnetDto;
import com.softwells.fanops.controller.dto.RegisterRequest;
import com.softwells.fanops.controller.dto.SocioDto;
import com.softwells.fanops.controller.dto.SocioStatsDto;
import com.softwells.fanops.enums.EstadoCuota;
import com.softwells.fanops.exception.EmailAlreadyExistsException;
import com.softwells.fanops.model.CuotaEntity;
import com.softwells.fanops.model.PenaEntity;
import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.model.SocioEntity;
import com.softwells.fanops.model.UsuarioEntity;
import com.softwells.fanops.repository.CuotaRepository;
import com.softwells.fanops.repository.RoleRepository;
import com.softwells.fanops.repository.SocioRepository;
import com.softwells.fanops.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.commons.text.WordUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class SocioService {

  // Constantes para los índices de las columnas del Excel
  private static final int COL_NOMBRE_COMPLETO = 0;
  private static final int COL_DNI = 1;
  private static final int COL_FECHA_NACIMIENTO = 2;
  private static final int COL_DIRECCION = 3;
  private static final int COL_POBLACION = 4;
  private static final int COL_PROVINCIA = 5;
  private static final int COL_CODIGO_POSTAL = 6;
  private static final int COL_TELEFONO = 7;
  private static final int COL_EMAIL = 8;
  private static final int COL_ABONADO_BETIS = 9;
  private static final int COL_ACCIONISTA_BETIS = 10;
  private static final int COL_DOMICILIACION = 12;

  private final SocioRepository socioRepository;
  private final CuotaRepository cuotaRepository;
  private final PasswordEncoder passwordEncoder;
  private final UsuarioRepository usuarioRepository;
  private final RoleRepository roleRepository;
  private final PenaService penaService;
  private final UsuarioService usuarioService;

  public SocioEntity crear(SocioEntity socio) {
    if (socioRepository.existsByDni(socio.getDni())) {
      throw new IllegalArgumentException("Ya existe un socio con ese DNI");
    }
    // El socio se da de alta en la peña de trabajo de quien lo está creando (admin de su peña,
    // o la peña que el superadmin tenga seleccionada en ese momento).
    socio.setPena(usuarioService.obtenerPenaDelUsuarioAutenticado());
    return socioRepository.save(socio);
  }

  public SocioEntity registrarSocio(RegisterRequest request) {
    // 1. Verifica que el email no esté ya en uso para un Usuario.
    if (usuarioRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
      throw new EmailAlreadyExistsException("El email ya está registrado.");
    }

    // 2. Crea el nuevo UsuarioEntity
    UsuarioEntity nuevoUsuario = new UsuarioEntity();
    nuevoUsuario.setEmail(request.getEmail());
    nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
    nuevoUsuario.setActivo(true);

    // Asignamos el rol directamente al Usuario
    RoleEntity userRole = roleRepository.findByName("ROLE_USER")
        .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_USER")));
    nuevoUsuario.setRoles(Set.of(userRole));

    // El auto-registro público no permite elegir peña todavía: se asigna la peña por defecto.
    PenaEntity penaPorDefecto = penaService.getDefaultPena();
    nuevoUsuario.setPena(penaPorDefecto);

    UsuarioEntity usuario = usuarioRepository.save(nuevoUsuario);

    // 3. Crea la nueva ficha de SocioEntity.
    SocioEntity nuevoSocio = new SocioEntity();
    nuevoSocio.setNombre(request.getNombre());
    nuevoSocio.setEmail(request.getEmail());
    nuevoSocio.setFechaAlta(LocalDate.now());
    nuevoSocio.setActivo(true);
    nuevoSocio.setAbonadoBetis(false);
    nuevoSocio.setAccionistaBetis(false);
    nuevoSocio.setExentoPago(false);
    nuevoSocio.setNumeroSocio(generarNumeroSocio());
    nuevoSocio.setUsuario(usuario);
    nuevoSocio.setPena(penaPorDefecto);
    return socioRepository.save(nuevoSocio);
  }

  @Transactional
  public SocioEntity actualizar(UUID id, SocioEntity socioData) {
    SocioEntity existente = obtenerPorId(id);

    // Actualizamos los datos del socio
    existente.setNumeroSocio(socioData.getNumeroSocio());
    existente.setNombre(socioData.getNombre());
    existente.setDni(socioData.getDni());
    existente.setFechaNacimiento(socioData.getFechaNacimiento());
    existente.setEmail(socioData.getEmail());
    existente.setTelefono(socioData.getTelefono());
    existente.setDireccion(socioData.getDireccion());
    existente.setPoblacion(socioData.getPoblacion());
    existente.setProvincia(socioData.getProvincia());
    existente.setCodigoPostal(socioData.getCodigoPostal());
    existente.setActivo(socioData.isActivo());
    existente.setAbonadoBetis(socioData.isAbonadoBetis());
    existente.setAccionistaBetis(socioData.isAccionistaBetis());
    existente.setExentoPago(socioData.isExentoPago());
    existente.setNumeroCuenta(socioData.getNumeroCuenta());
    existente.setFechaAlta(socioData.getFechaAlta());
    existente.setObservaciones(socioData.getObservaciones());

    // Los roles viven en la cuenta de usuario, así que solo se tocan si la ficha ya está
    // vinculada a una: las fichas del listado que nadie ha reclamado todavía no tienen cuenta.
    if (existente.getUsuario() != null && socioData.getUsuario() != null) {
      existente.getUsuario().setRoles(socioData.getUsuario().getRoles());
    }

    return socioRepository.save(existente);
  }


  @Transactional
  public SocioEntity actualizarMiSocio(UUID id, SocioEntity socioData) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assert authentication != null;
    String userEmail = authentication.getName();

    SocioEntity existente = socioRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Socio no encontrado con ID: " + id));

    // Verificación de seguridad: el socio debe pertenecer al usuario autenticado
    if (!existente.getUsuario().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("No tienes permiso para modificar este socio.");
    }

    // Actualizamos solo los campos permitidos
    existente.setFechaNacimiento(socioData.getFechaNacimiento());
    existente.setDni(socioData.getDni());
    existente.setDireccion(socioData.getDireccion());
    existente.setPoblacion(socioData.getPoblacion());
    existente.setProvincia(socioData.getProvincia());
    existente.setCodigoPostal(socioData.getCodigoPostal());
    existente.setTelefono(socioData.getTelefono());
    existente.setNumeroCuenta(socioData.getNumeroCuenta());

    return socioRepository.save(existente);
  }

  public void eliminar(UUID id) {
    if (!socioRepository.existsById(id)) {
      throw new EntityNotFoundException("Socio no encontrado");
    }
    socioRepository.deleteById(id);
  }

  public SocioEntity obtenerPorId(UUID id) {
    SocioEntity socio = socioRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Socio no encontrado"));

    // Lógica de autorización
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assert authentication != null;
    String currentUsername = authentication.getName();
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !socio.getEmail().equals(currentUsername)) {
      throw new AccessDeniedException("No tienes permiso para ver la información de este socio.");
    }
    return socio;
  }

  public List<SocioEntity> obtenerTodos() {
    Long penaId = usuarioService.obtenerPenaDelUsuarioAutenticado().getId();
    return socioRepository.findByPenaId(penaId);
  }

  public List<SocioEntity> obtenerSociosActivos() {
    Long penaId = usuarioService.obtenerPenaDelUsuarioAutenticado().getId();
    return socioRepository.findByActivoAndPenaId(true, penaId);
  }

  @Transactional(readOnly = true)
  public List<CuotaEntity> obtenerCuotasDeSocio(UUID socioId) {
    return cuotaRepository.findBySocioUid(socioId);
  }

  // En tu clase SocioService
  public SocioStatsDto obtenerEstadisticas(LocalDate fechaDesde) {
    // Obtenemos la peña de trabajo del usuario autenticado y acotamos todo a esa peña
    PenaEntity pena = usuarioService.obtenerPenaDelUsuarioAutenticado();
    Long penaId = pena.getId();

    long totalSocios = socioRepository.countByPenaId(penaId);
    long nuevosSocios = socioRepository.countByFechaAltaGreaterThanEqualAndPenaId(fechaDesde,
        penaId);

    LocalDate fechaCorteJovenes = LocalDate.now().minusYears(pena.getEdadMayoria());
    long totalSociosJovenes = socioRepository.countByFechaNacimientoAfterAndPenaId(
        fechaCorteJovenes, penaId);

    // Calculamos la fecha de corte para ser jubilado
    LocalDate fechaCorteJubilados = LocalDate.now().minusYears(pena.getEdadJubilacion());
    long totalSociosJubilados = socioRepository.countByFechaNacimientoBeforeOrEqualsAndPenaId(
        fechaCorteJubilados, penaId);

    List<EstadoCuota> estadosImpagados = List.of(EstadoCuota.RECHAZADA, EstadoCuota.VENCIDA);
    int totalImpagados = cuotaRepository.countDistinctSociosByEstadoIn(estadosImpagados, penaId);

    return new SocioStatsDto(totalSocios, nuevosSocios, totalSociosJovenes, pena.getEdadMayoria(),
        totalSociosJubilados, pena.getEdadJubilacion(), totalImpagados);
  }

  private Integer generarNumeroSocio() {
    // Busca el número de socio máximo actual y le suma 1.
    // Si no hay socios, empieza en 1.
    return socioRepository.findMaxNumeroSocio().orElse(0) + 1;
  }

  public void importarSocios(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      Workbook workbook = new XSSFWorkbook(inputStream);
      int sheetNum = workbook.getNumberOfSheets();

      List<SocioEntity> socios = new ArrayList<>();

      // Peña de trabajo de quien hace la importación: todos los socios importados se dan de
      // alta en esa peña.
      PenaEntity pena = usuarioService.obtenerPenaDelUsuarioAutenticado();

      // Obtenemos el número de socio máximo actual para empezar a incrementar desde ahí
      int numSocio = socioRepository.findMaxNumeroSocio().orElse(0) + 1;


      for (int sheetIndex = 0; sheetIndex < sheetNum; sheetIndex++) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        // Creamos un formateador que acepta múltiples patrones de fecha
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd MM yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd - MM - yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd /MM /yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            .toFormatter();

        // Saltamos la primera fila (cabecera)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
          Row row = sheet.getRow(i);
          if (row == null) {
            continue;
          }

          // Verificamos si la fila está vacía comprobando la columna del nombre.
          String nombreCompleto = getCellValueAsString(row.getCell(COL_NOMBRE_COMPLETO));
          if (nombreCompleto == null || nombreCompleto.isBlank()) {
            log.warn("Saltando fila {} porque el nombre está vacío.", row.getRowNum() + 1);
            continue; // Si el nombre está vacío, ignoramos la fila completa.
          }

          SocioEntity socio = new SocioEntity();
          String email = getCellValueAsString(row.getCell(COL_EMAIL));

          // Los socios importados se quedan a propósito sin cuenta de usuario: la cuenta la
          // crea la propia persona al registrarse con ese email, y la ficha se le vincula solo
          // cuando confirma el enlace que recibe por correo (ver VinculacionSocioService).
          // Antes se intentaba crear el usuario aquí, pero la condición estaba invertida (solo
          // entraba si el email venía en blanco) y acababa dando de alta usuarios con email
          // vacío, que rompen la columna única de "usuarios".

          // Asignamos los valores de las celdas al objeto SocioEntity usando los índices 0-based
          socio.setNombre(WordUtils.capitalizeFully(nombreCompleto));
          socio.setDni(getCellValueAsString(row.getCell(COL_DNI)));
          socio.setEmail(email);

          String fechaNacimientoStr = getCellValueAsString(row.getCell(COL_FECHA_NACIMIENTO));
          if (fechaNacimientoStr != null && !fechaNacimientoStr.isEmpty()) {
            try {
              socio.setFechaNacimiento(LocalDate.parse(fechaNacimientoStr, dateFormatter));
            } catch (Exception e) {
              log.warn("Error al formatear la fecha '{}' para el socio '{}'", fechaNacimientoStr,
                  socio.getNombre());
            }
          }

          socio.setDireccion(getCellValueAsString(row.getCell(COL_DIRECCION)));
          socio.setPoblacion(getCellValueAsString(row.getCell(COL_POBLACION)));
          socio.setProvincia(getCellValueAsString(row.getCell(COL_PROVINCIA)));
          socio.setCodigoPostal(getCellValueAsString(row.getCell(COL_CODIGO_POSTAL)));
          socio.setTelefono(getCellValueAsString(row.getCell(COL_TELEFONO)));

          // Extraemos el IBAN del campo de domiciliación
          String domiciliacion = getCellValueAsString(row.getCell(COL_DOMICILIACION));
          socio.setNumeroCuenta(domiciliacion);

          String esAbonado = getCellValueAsString(row.getCell(COL_ABONADO_BETIS));
          socio.setAbonadoBetis(
              "si".equalsIgnoreCase(esAbonado) || "sí".equalsIgnoreCase(esAbonado));

          String esAccionista = getCellValueAsString(row.getCell(COL_ACCIONISTA_BETIS));
          socio.setAccionistaBetis(
              "si".equalsIgnoreCase(esAccionista) || "sí".equalsIgnoreCase(esAccionista));

          socio.setActivo(true); // Por defecto, los nuevos socios están activos
          socio.setNumeroSocio(numSocio++);
          socio.setFechaAlta(LocalDate.now());
          socio.setPena(pena);

          socios.add(socio);
        }
      }
      socioRepository.saveAll(socios);
    } catch (Exception e) {
      log.error("Error al procesar el fichero Excel: {}", e.getMessage());
      throw new RuntimeException("Error al procesar el fichero Excel: " + e.getMessage());
    }
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) {
      return "";
    }
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      default -> "";
    };
  }

  public List<SocioEntity> sociosByUsuario(UUID uid) {
    return socioRepository.findByUsuarioUid(uid);
  }

  public List<SocioEntity> obtenerSocioAutenticado() {
    String userEmail = Objects.requireNonNull(
        SecurityContextHolder.getContext().getAuthentication()).getName();
    // Asumimos que un usuario tiene al menos una ficha de socio.
    // Esta lógica busca la primera que encuentra asociada a su email.
    return socioRepository.findByUsuarioEmail(userEmail);
  }

  @Transactional(readOnly = true)
  public CarnetDto obtenerDatosCarnetUsuarioAutenticado() {
    String userEmail = Objects.requireNonNull(
        SecurityContextHolder.getContext().getAuthentication()).getName();

    // 1. Obtener la información de la peña de trabajo del usuario autenticado
    PenaEntity penaInfo = usuarioService.obtenerPenaDelUsuarioAutenticado();

    // 2. Obtener todos los socios del usuario y mapearlos a DTOs
    List<SocioDto> sociosDto = socioRepository.findByUsuarioEmail(userEmail).stream()
        .map(SocioDto::fromEntity).collect(Collectors.toList());

    return new CarnetDto(penaInfo, sociosDto);
  }

  public SocioEntity crearSocioAsociado(SocioEntity nuevoSocioData) {
    // 1. Obtener el email del usuario autenticado desde el contexto de seguridad
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

    // 2. Buscar el usuario principal en la base de datos
    UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(userEmail)
        .orElseThrow(
            () -> new UsernameNotFoundException("Usuario no encontrado con email: " + userEmail));

    // 3. Crear y configurar la nueva entidad Socio
    SocioEntity nuevoSocio = new SocioEntity();
    nuevoSocio.setNumeroSocio(generarNumeroSocio());
    nuevoSocio.setNombre(nuevoSocioData.getNombre());
    nuevoSocio.setDni(nuevoSocioData.getDni());
    nuevoSocio.setFechaNacimiento(nuevoSocioData.getFechaNacimiento());
    nuevoSocio.setTelefono(nuevoSocioData.getTelefono());
    nuevoSocio.setDireccion(nuevoSocioData.getDireccion());
    nuevoSocio.setPoblacion(nuevoSocioData.getPoblacion());
    nuevoSocio.setProvincia(nuevoSocioData.getProvincia());
    nuevoSocio.setFechaAlta(LocalDate.now());
    nuevoSocio.setCodigoPostal(nuevoSocioData.getCodigoPostal());

    // Si no se proporciona un número de cuenta, hereda el del socio principal.
    if (nuevoSocioData.getNumeroCuenta() == null || nuevoSocioData.getNumeroCuenta().isBlank()) {
      SocioEntity socioPrincipal = usuario.getSocios().stream().findFirst()
          .orElseThrow(() -> new IllegalStateException(
              "El usuario no tiene un socio principal para heredar la cuenta."));
      nuevoSocio.setNumeroCuenta(socioPrincipal.getNumeroCuenta());
    } else {
      nuevoSocio.setNumeroCuenta(nuevoSocioData.getNumeroCuenta());
    }

    // 4. Asignar el usuario y su peña al nuevo socio
    nuevoSocio.setUsuario(usuario);
    nuevoSocio.setPena(usuario.getPena());
    nuevoSocio.setActivo(true); // Por defecto, el nuevo socio se crea como activo

    // 5. Guardar el nuevo socio en la base de datos
    return socioRepository.save(nuevoSocio);
  }

  public List<SocioEntity> obtenerSociosConImpagos() {
    List<EstadoCuota> estadosImpagados = List.of(EstadoCuota.RECHAZADA, EstadoCuota.VENCIDA);
    Long penaId = usuarioService.obtenerPenaDelUsuarioAutenticado().getId();
    return socioRepository.findSociosConCuotasEnEstadosAndPenaId(estadosImpagados, penaId);
  }
}
