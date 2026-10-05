package com.ejemplo.pagos.repository;

import com.ejemplo.pagos.entity.IntentoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntentoPagoRepository extends JpaRepository<IntentoPago, Long> {
    List<IntentoPago> findByPagoIdOrderByTimestampAsc(Long pagoId);
}
