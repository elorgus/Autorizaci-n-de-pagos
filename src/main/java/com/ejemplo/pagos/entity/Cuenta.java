package com.ejemplo.pagos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas", uniqueConstraints = @UniqueConstraint(columnNames = "numero"))
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @Column(nullable = false, length = 150)
    private String titular;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "limite_diario", nullable = false, precision = 15, scale = 2)
    private BigDecimal limiteDiario = new BigDecimal("5000.00");

    @Column(nullable = false)
    private Boolean activa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public BigDecimal getLimiteDiario() { return limiteDiario; }
    public void setLimiteDiario(BigDecimal limiteDiario) { this.limiteDiario = limiteDiario; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
