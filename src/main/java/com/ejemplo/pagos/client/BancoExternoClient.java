package com.ejemplo.pagos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
public class BancoExternoClient {

    private static final Logger log = LoggerFactory.getLogger(BancoExternoClient.class);
    private final Random random = new Random();

    public record RespuestaBanco(boolean aprobado, String codigo, String mensaje) {}

    /**
     * Simula una llamada al banco externo.
     * - 70% aprobado
     * - 15% rechazado
     * - 15% timeout / error de red (para probar reintentos)
     */
    public RespuestaBanco autorizar(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        try {
            // Latencia variable: 0.5s a 2s
            long latencia = 500 + random.nextInt(1500);
            Thread.sleep(latencia);

            int dado = random.nextInt(100);

            if (dado < 70) {
                log.info("Banco externo APROBÓ pago {} -> {} por {}", cuentaOrigen, cuentaDestino, monto);
                return new RespuestaBanco(true, "00", "Autorizado por banco externo");
            } else if (dado < 85) {
                log.warn("Banco externo RECHAZÓ pago {} -> {} por {}", cuentaOrigen, cuentaDestino, monto);
                return new RespuestaBanco(false, "51", "Fondos insuficientes en banco externo");
            } else {
                log.error("Banco externo TIMEOUT/ERROR en pago {} -> {} por {}", cuentaOrigen, cuentaDestino, monto);
                throw new RuntimeException("Timeout del banco externo (simulado)");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido", e);
        }
    }
}
