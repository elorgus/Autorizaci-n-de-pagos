package com.ejemplo.pagos.repository;

import com.ejemplo.pagos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByIdempotencyKey(String idempotencyKey);
    List<Pago> findAllByOrderByCreadoEnDesc();
}
