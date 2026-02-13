package org.example.judge;

import org.example.judge.core.OutputComparator;
import org.example.judge.model.ComparisonResult;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para OutputComparator.
 *
 * Testa:
 * - Comparação de outputs idênticos
 * - Comparação de outputs diferentes
 * - Linhas faltantes ou extra
 * - Cálculo de percentagem de similaridade
 * - Geração de relatório
 */
class OutputComparatorTest {

    private OutputComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new OutputComparator();
    }

    @Test
    @DisplayName("Deve identificar outputs idênticos")
    void testIdenticalOutputs() {
        // TODO: Implementar
        String expected = "Line 1\nLine 2\nLine 3";
        String user = "Line 1\nLine 2\nLine 3";

        ComparisonResult result = comparator.compare(user, expected);

        // TODO: Descomentar quando implementado
        // assertTrue(result.isMatch());
        // assertEquals(100.0, result.getSimilarityPercentage());
        // assertEquals(3, result.getMatchingLines());
    }

    @Test
    @DisplayName("Deve identificar diferenças entre outputs")
    void testDifferentOutputs() {
        // TODO: Implementar
        String expected = "Hello\nWorld\nTest";
        String user = "Hello\nJava\nTest";

        ComparisonResult result = comparator.compare(user, expected);

        // TODO: Descomentar quando implementado
        // assertFalse(result.isMatch());
        // assertEquals(2, result.getMatchingLines());
        // assertTrue(result.getSimilarityPercentage() < 100.0);
    }

    @Test
    @DisplayName("Deve identificar linhas faltantes")
    void testMissingLines() {
        // TODO: Implementar
        String expected = "Line 1\nLine 2\nLine 3\nLine 4";
        String user = "Line 1\nLine 2";

        ComparisonResult result = comparator.compare(user, expected);

        // TODO: Verificar que há linhas MISSING
    }

    @Test
    @DisplayName("Deve identificar linhas extra")
    void testExtraLines() {
        // TODO: Implementar
        String expected = "Line 1\nLine 2";
        String user = "Line 1\nLine 2\nLine 3\nLine 4";

        ComparisonResult result = comparator.compare(user, expected);

        // TODO: Verificar que há linhas EXTRA
    }

    @Test
    @DisplayName("Deve ignorar whitespace se configurado")
    void testIgnoreWhitespace() {
        // TODO: Implementar
        comparator.setIgnoreTrailingWhitespace(true);

        String expected = "Hello  ";
        String user = "Hello";

        ComparisonResult result = comparator.compare(user, expected);

        // TODO: Verificar que são considerados iguais
    }

    @Test
    @DisplayName("Deve gerar relatório formatado")
    void testGenerateReport() {
        // TODO: Implementar
        String expected = "Output 1\nOutput 2";
        String user = "Output 1\nDifferent";

        ComparisonResult result = comparator.compare(user, expected);
        String report = comparator.generateDiffReport(result);

        // TODO: Verificar que relatório contém informação relevante
        // assertNotNull(report);
        // assertTrue(report.contains("RESULTADO DA COMPARAÇÃO"));
    }
}

