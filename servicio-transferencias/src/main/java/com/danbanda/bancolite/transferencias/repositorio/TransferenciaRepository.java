package com.danbanda.bancolite.transferencias.repositorio;

import com.danbanda.bancolite.transferencias.modelo.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferenciaRepository extends JpaRepository<Transferencia, String> {

    Optional<Transferencia> findByClaveIdempotencia(String claveIdempotencia);

    // movimientos donde la cuenta fue origen o destino, los 20 mas recientes
    List<Transferencia> findTop20ByCuentaOrigenOrCuentaDestinoOrderByFechaCreacionDesc(
            String cuentaOrigen, String cuentaDestino);
}
