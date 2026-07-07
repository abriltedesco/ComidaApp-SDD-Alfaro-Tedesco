package com.comidapp.domain.entities;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.comidapp.domain.enums.DiaSemana;

/**
 * Tests unitarios para la entidad Local — valida lógica estaAbierto/estaAbiertoEn (FR-033).
 */
@DisplayName("Local - Tests unitarios")
class LocalTest {

    private Local local;

    @BeforeEach
    void setUp() {
        local = new Local();
        local.setId(1L);
        local.setNombre("Test Local");
        local.setDireccion("Calle Test 123");

        // Horario: Lunes a Viernes 11:00-23:00
        for (DiaSemana dia : new DiaSemana[]{
                DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
                DiaSemana.JUEVES, DiaSemana.VIERNES}) {
            HorarioLocal h = new HorarioLocal();
            h.setDiaSemana(dia);
            h.setHoraApertura(LocalTime.of(11, 0));
            h.setHoraCierre(LocalTime.of(23, 0));
            local.getHorarios().add(h);
        }

        // Sábado y Domingo 12:00-22:00
        for (DiaSemana dia : new DiaSemana[]{DiaSemana.SABADO, DiaSemana.DOMINGO}) {
            HorarioLocal h = new HorarioLocal();
            h.setDiaSemana(dia);
            h.setHoraApertura(LocalTime.of(12, 0));
            h.setHoraCierre(LocalTime.of(22, 0));
            local.getHorarios().add(h);
        }
    }

    @Test
    @DisplayName("Local abierto un Lunes a las 15:00")
    void abiertoLunes15() {
        // Lunes 15:00
        LocalDateTime lunes15 = LocalDateTime.of(2026, 6, 22, 15, 0); // 22/06/2026 = Lunes
        assertTrue(local.estaAbiertoEn(lunes15));
    }

    @Test
    @DisplayName("Local cerrado un Lunes a las 08:00")
    void cerradoLunes8() {
        LocalDateTime lunes8 = LocalDateTime.of(2026, 6, 22, 8, 0);
        assertFalse(local.estaAbiertoEn(lunes8));
    }

    @Test
    @DisplayName("Local cerrado un Lunes a las 23:30")
    void cerradoLunes2330() {
        LocalDateTime lunes2330 = LocalDateTime.of(2026, 6, 22, 23, 30);
        assertFalse(local.estaAbiertoEn(lunes2330));
    }

    @Test
    @DisplayName("Local abierto exactamente a hora de apertura")
    void abiertoExactoApertura() {
        LocalDateTime lunes11 = LocalDateTime.of(2026, 6, 22, 11, 0);
        assertTrue(local.estaAbiertoEn(lunes11));
    }

    @Test
    @DisplayName("Local abierto exactamente a hora de cierre")
    void abiertoExactoCierre() {
        LocalDateTime lunes23 = LocalDateTime.of(2026, 6, 22, 23, 0);
        assertTrue(local.estaAbiertoEn(lunes23));
    }

    @Test
    @DisplayName("Local abierto Sábado 14:00")
    void abiertoSabado14() {
        LocalDateTime sabado14 = LocalDateTime.of(2026, 6, 27, 14, 0); // Sábado
        assertTrue(local.estaAbiertoEn(sabado14));
    }

    @Test
    @DisplayName("Local cerrado Domingo 11:00 (abre 12:00)")
    void cerradoDomingo11() {
        LocalDateTime domingo11 = LocalDateTime.of(2026, 6, 28, 11, 0); // Domingo
        assertFalse(local.estaAbiertoEn(domingo11));
    }

    @Test
    @DisplayName("Local sin horarios está cerrado")
    void localSinHorariosCerrado() {
        Local localVacio = new Local();
        localVacio.setNombre("Local sin horarios");
        assertFalse(localVacio.estaAbiertoEn(LocalDateTime.now()));
    }

    @Test
    @DisplayName("DiaSemana.fromDayOfWeek convierte correctamente")
    void diaSemanaConversion() {
        assertEquals(DiaSemana.LUNES, DiaSemana.fromDayOfWeek(DayOfWeek.MONDAY));
        assertEquals(DiaSemana.DOMINGO, DiaSemana.fromDayOfWeek(DayOfWeek.SUNDAY));
        assertEquals(DiaSemana.MIERCOLES, DiaSemana.fromDayOfWeek(DayOfWeek.WEDNESDAY));
    }
}
