package org.example.judge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ComparisonResult - Modelo que representa o resultado da comparação de outputs.
 *
 * Utilizado por:
 * - OutputComparator (cria e retorna este objeto)
 * - UIController (mostra o resultado)
 * - TabManager (formata para exibição)
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class ComparisonResult {

    private boolean isMatch;
    private List<DiffLine> differences;
    private int totalLines;
    private int matchingLines;
    private double similarityPercentage;

    /**
     * Construtor.
     *
     * @param isMatch Se outputs são idênticos
     * @param differences Lista de diferenças
     * @param totalLines Total de linhas comparadas
     * @param matchingLines Linhas que coincidem
     */
    public ComparisonResult(boolean isMatch, List<DiffLine> differences,
                           int totalLines, int matchingLines) {
        this.isMatch = isMatch;
        this.differences = differences;
        this.totalLines = totalLines;
        this.matchingLines = matchingLines;
        this.similarityPercentage = calculateSimilarity();
    }

    /**
     * Calcula percentagem de similaridade.
     *
     * @return Percentagem (0-100)
     */
    private double calculateSimilarity() {
        if (totalLines == 0) return 100.0;
        return (matchingLines * 100.0) / totalLines;
    }

    /**
     * Gera relatório formatado de comparação.
     *
     * @return String com relatório
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("=====================================\n");
        sb.append("     RESULTADO DA COMPARAÇÃO\n");
        sb.append("=====================================\n\n");

        if (isMatch) {
            sb.append("✓ OUTPUTS IDÊNTICOS!\n");
            sb.append("Todas as linhas coincidem.\n");
        } else {
            sb.append("✗ OUTPUTS DIFERENTES\n\n");
            sb.append(String.format("Similaridade: %.1f%%\n", similarityPercentage));
            sb.append(String.format("Linhas corretas: %d/%d\n\n", matchingLines, totalLines));

            sb.append("Diferenças encontradas:\n");
            sb.append("-------------------------------------\n");

            for (DiffLine diff : differences) {
                if (diff.type != DiffType.MATCH) {
                    sb.append(diff.format()).append("\n");
                }
            }
        }

        sb.append("=====================================\n");

        return sb.toString();
    }

    // Getters

    public boolean isMatch() {
        return isMatch;
    }

    public List<DiffLine> getDifferences() {
        return differences;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getMatchingLines() {
        return matchingLines;
    }

    public double getSimilarityPercentage() {
        return similarityPercentage;
    }

    /**
     * DiffLine - Representa uma diferença entre linhas.
     */
    public static class DiffLine {
        private int lineNumber;
        private String expectedLine;
        private String userLine;
        private DiffType type;

        public DiffLine(int lineNumber, String expectedLine, String userLine, DiffType type) {
            this.lineNumber = lineNumber;
            this.expectedLine = expectedLine;
            this.userLine = userLine;
            this.type = type;
        }

        /**
         * Formata a diferença para exibição.
         *
         * @return String formatada
         */
        public String format() {
            switch (type) {
                case MATCH:
                    return String.format("Linha %d: ✓ OK", lineNumber);

                case DIFFERENT:
                    return String.format("Linha %d: DIFERENTE\n  Esperado: \"%s\"\n  Recebido: \"%s\"",
                            lineNumber, expectedLine, userLine);

                case MISSING:
                    return String.format("Linha %d: FALTANTE\n  Esperado: \"%s\"\n  Recebido: (nada)",
                            lineNumber, expectedLine);

                case EXTRA:
                    return String.format("Linha %d: EXTRA\n  Esperado: (nada)\n  Recebido: \"%s\"",
                            lineNumber, userLine);

                default:
                    return String.format("Linha %d: UNKNOWN", lineNumber);
            }
        }

        // Getters

        public int getLineNumber() {
            return lineNumber;
        }

        public String getExpectedLine() {
            return expectedLine;
        }

        public String getUserLine() {
            return userLine;
        }

        public DiffType getType() {
            return type;
        }
    }

    /**
     * DiffType - Tipo de diferença.
     */
    public enum DiffType {
        MATCH,      // Linhas idênticas
        DIFFERENT,  // Linhas diferentes
        MISSING,    // Linha existe em expected mas não em user
        EXTRA       // Linha existe em user mas não em expected
    }
}

