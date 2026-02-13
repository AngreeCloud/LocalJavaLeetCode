package org.example.judge;

import org.example.judge.core.SuggestionEngine;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para SuggestionEngine.
 *
 * Testa:
 * - Matching contextual simples
 * - Sugestão em diferentes blocos (if, for, while)
 * - Matching baseado em tokens
 * - Casos onde não há match
 * - Histórico de sugestões
 */
class SuggestionEngineTest {

    private SuggestionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SuggestionEngine();
    }

    @Test
    @DisplayName("Deve sugerir linha correta em contexto simples")
    void testSimpleContextMatching() {
        // TODO: Implementar
        String userCode = """
            public class Test {
                public static void main(String[] args) {
                    int sum = 0;
                    for (int i = 0; i < 10; i++) {
                        
                    }
                }
            }
            """;

        String officialSolution = """
            public class Test {
                public static void main(String[] args) {
                    int sum = 0;
                    for (int i = 0; i < 10; i++) {
                        sum += i;
                    }
                    System.out.println(sum);
                }
            }
            """;

        int cursorLine = 4; // Dentro do for loop

        String suggestion = engine.suggestNextLine(userCode, cursorLine, officialSolution);

        // TODO: Descomentar quando implementado
        // assertNotNull(suggestion);
        // assertTrue(suggestion.contains("sum += i"));
    }

    @Test
    @DisplayName("Deve encontrar match em bloco if")
    void testIfBlockMatching() {
        // TODO: Implementar
        String userCode = """
            if (x > 0) {
                
            }
            """;

        String officialSolution = """
            if (x > 0) {
                System.out.println("Positive");
            }
            """;

        int cursorLine = 1;

        String suggestion = engine.suggestNextLine(userCode, cursorLine, officialSolution);

        // TODO: Verificar sugestão
    }

    @Test
    @DisplayName("Deve retornar null quando não há match adequado")
    void testNoMatchFound() {
        // TODO: Implementar
        String userCode = "int x = 5;";
        String officialSolution = "String name = \"test\";";

        int cursorLine = 0;

        String suggestion = engine.suggestNextLine(userCode, cursorLine, officialSolution);

        // TODO: Descomentar quando implementado
        // assertNull(suggestion);
    }

    @Test
    @DisplayName("Deve considerar indentação no matching")
    void testIndentationMatching() {
        // TODO: Implementar
        // Verificar que linhas com indentação similar têm score maior
    }

    @Test
    @DisplayName("Deve considerar tokens em comum")
    void testTokenMatching() {
        // TODO: Implementar
        // Verificar que linhas com tokens similares têm score maior
    }

    @Test
    @DisplayName("Deve manter histórico de sugestões")
    void testSuggestionHistory() {
        // TODO: Implementar
        // Verificar que não sugere a mesma linha duas vezes
        engine.clearHistory();
        // TODO: fazer múltiplas sugestões
    }
}

