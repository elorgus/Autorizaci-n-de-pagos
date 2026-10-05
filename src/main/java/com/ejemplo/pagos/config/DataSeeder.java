package com.ejemplo.pagos.config;

import com.ejemplo.pagos.entity.Cuenta;
import com.ejemplo.pagos.repository.CuentaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CuentaRepository cuentaRepo;

    public DataSeeder(CuentaRepository cuentaRepo) {
        this.cuentaRepo = cuentaRepo;
    }

    @Override
    public void run(String... args) {
        if (cuentaRepo.count() > 0) return;

        crear("ACC-001", "Ana Torres", "10000.00", "5000.00", true);
        crear("ACC-002", "Luis Pérez", "2500.00", "2000.00", true);
        crear("ACC-003", "María Gómez", "800.00", "1000.00", true);
        crear("ACC-004", "Carlos Ruiz", "0.00", "500.00", false);
    }

    private void crear(String numero, String titular, String saldo, String limite, boolean activa) {
        Cuenta c = new Cuenta();
        c.setNumero(numero);
        c.setTitular(titular);
        c.setSaldo(new BigDecimal(saldo));
        c.setLimiteDiario(new BigDecimal(limite));
        c.setActiva(activa);
        cuentaRepo.save(c);
    }
}
