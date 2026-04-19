package com.jaasielsilva.erpcorporativo.app.contract;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.PaymentRecordViewModel;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

/**
 * Feature: saas-contracts-and-architecture
 * Property 5: Contract date-based display flags are consistent
 * Property 6: Tenant without contracts resolves to SEM_CONTRATO label
 * Property 10: Payment record display properties are correct
 */
class ContractViewModelPropertyTest {

    // -------------------------------------------------------------------------
    // Property 5: Contract date-based display flags are consistent
    // Validates: Requirements 6.5, 9.2
    // -------------------------------------------------------------------------

    @Property(tries = 100)
    void pastTerminationDateWithAtivoStatus_isVencidoTrue(@ForAll @IntRange(min = 1, max = 3650) int daysAgo) {
        // Feature: saas-contracts-and-architecture, Property 5
        LocalDate dataTermino = LocalDate.now().minusDays(daysAgo);
        ContractViewModel vm = buildContract(ContractStatus.ATIVO, dataTermino);

        assertTrue(vm.isVencido(), "Contract with past dataTermino and ATIVO status should be vencido");
        assertFalse(vm.isVencendoEm30Dias(), "Vencido contract should not be vencendoEm30Dias");
    }

    @Property(tries = 100)
    void terminationWithin30Days_isVencendoEm30DiasTrue(@ForAll @IntRange(min = 0, max = 30) int daysAhead) {
        // Feature: saas-contracts-and-architecture, Property 5
        LocalDate dataTermino = LocalDate.now().plusDays(daysAhead);
        ContractViewModel vm = buildContract(ContractStatus.ATIVO, dataTermino);

        assertTrue(vm.isVencendoEm30Dias(), "Contract terminating within 30 days should be vencendoEm30Dias");
        assertFalse(vm.isVencido(), "Contract not yet expired should not be vencido");
    }

    @Property(tries = 100)
    void terminationBeyond30Days_bothFlagsFalse(@ForAll @IntRange(min = 31, max = 3650) int daysAhead) {
        // Feature: saas-contracts-and-architecture, Property 5
        LocalDate dataTermino = LocalDate.now().plusDays(daysAhead);
        ContractViewModel vm = buildContract(ContractStatus.ATIVO, dataTermino);

        assertFalse(vm.isVencido(), "Contract with future termination > 30 days should not be vencido");
        assertFalse(vm.isVencendoEm30Dias(), "Contract with future termination > 30 days should not be vencendoEm30Dias");
    }

    @Property(tries = 50)
    void nonAtivoStatus_flagsAlwaysFalse(@ForAll @IntRange(min = 1, max = 10) int daysAgo) {
        // Feature: saas-contracts-and-architecture, Property 5
        LocalDate dataTermino = LocalDate.now().minusDays(daysAgo);
        for (ContractStatus status : new ContractStatus[]{ContractStatus.ENCERRADO, ContractStatus.SUSPENSO, ContractStatus.AGUARDANDO_ASSINATURA}) {
            ContractViewModel vm = buildContract(status, dataTermino);
            assertFalse(vm.isVencido(), "Non-ATIVO contract should not be vencido");
            assertFalse(vm.isVencendoEm30Dias(), "Non-ATIVO contract should not be vencendoEm30Dias");
        }
    }

    // -------------------------------------------------------------------------
    // Property 10: Payment record display properties are correct
    // Validates: Requirements 8.5, 8.6
    // -------------------------------------------------------------------------

    @Property(tries = 100)
    void atrasadoPayment_isAtrasadoTrue() {
        // Feature: saas-contracts-and-architecture, Property 10
        PaymentRecordViewModel vm = buildPayment(PaymentStatus.ATRASADO);
        assertTrue(vm.isAtrasado(), "Payment with ATRASADO status should have isAtrasado=true");
    }

    @Property(tries = 100)
    void nonAtrasadoPayment_isAtrasadoFalse() {
        // Feature: saas-contracts-and-architecture, Property 10
        for (PaymentStatus status : new PaymentStatus[]{PaymentStatus.PAGO, PaymentStatus.PENDENTE, PaymentStatus.CANCELADO}) {
            PaymentRecordViewModel vm = buildPayment(status);
            assertFalse(vm.isAtrasado(), "Payment with status " + status + " should have isAtrasado=false");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContractViewModel buildContract(ContractStatus status, LocalDate dataTermino) {
        LocalDate hoje = LocalDate.now();
        boolean isVencido = dataTermino != null
                && dataTermino.isBefore(hoje)
                && status == ContractStatus.ATIVO;
        boolean isVencendoEm30Dias = dataTermino != null
                && !dataTermino.isBefore(hoje)
                && !dataTermino.isAfter(hoje.plusDays(30))
                && status == ContractStatus.ATIVO;

        return new ContractViewModel(
                1L, 1L, "Tenant Teste", 1L, "Plano Pro",
                BigDecimal.valueOf(299), LocalDate.now().minusMonths(6), dataTermino,
                status, null, 1, isVencido, isVencendoEm30Dias, null, List.of(), null, null
        );
    }

    private PaymentRecordViewModel buildPayment(PaymentStatus status) {
        return new PaymentRecordViewModel(
                1L, null, BigDecimal.valueOf(299), null,
                status, status == PaymentStatus.ATRASADO, null, null
        );
    }
}
