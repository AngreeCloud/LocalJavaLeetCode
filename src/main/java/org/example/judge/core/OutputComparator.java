package org.example.judge.core;

import java.util.ArrayList;
import java.util.List;

import org.example.judge.model.ComparisonResult;
import org.example.judge.model.ComparisonResult.DiffLine;
import org.example.judge.model.ComparisonResult.DiffType;

/**
 * OutputComparator - Comparador de outputs linha a linha.
 *
 * Responsabilidades:
 * - Comparar output do utilizador com solução oficial
 * - Identificar linhas diferentes, faltantes ou extra
 * - Calcular percentagem de similaridade
 * - Gerar relatório formatado de diferenças
 * - Suportar opções de comparação (ignorar whitespace, case-sensitive)
 *
 * Algoritmo:
 * 1. Dividir ambos outputs em linhas
 * 2. Comparar linha a linha
 * 3. Identificar diferenças
 * 4. Gerar relatório com cores
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class OutputComparator {

    private boolean ignoreTrailingWhitespace = true;
    private boolean caseSensitive = true;

    /**
     * Construtor padrão.
     */
    public OutputComparator() {
    }

    /**
     * Compara dois outputs e retorna resultado detalhado.
     *
     * @param userOutput Output do utilizador
     * @param expectedOutput Output esperado (solução oficial)
     * @return ComparisonResult com diferenças e estatísticas
     */
    public ComparisonResult compare(String userOutput, String expectedOutput) {
        if (userOutput == null) userOutput = "";
        if (expectedOutput == null) expectedOutput = "";
        
        String[] userLines = userOutput.split("\n");
        String[] expectedLines = expectedOutput.split("\n");
        
        List<DiffLine> differences = lineDiff(userLines, expectedLines);
        
        int matchingLines = 0;
        for (DiffLine diff : differences) {
            if (diff.getType() == DiffType.MATCH) {
                matchingLines++;
            }
        }
        
        int totalLines = Math.max(userLines.length, expectedLines.length);
        boolean match = differences.stream().allMatch(d -> d.getType() == DiffType.MATCH);
        
        return new ComparisonResult(match, differences, totalLines, matchingLines);
    }

    /**
     * Compara arrays de linhas e identifica diferenças.
     *
     * @param userLines Linhas do utilizador
     * @param expectedLines Linhas esperadas
     * @return Lista de DiffLine
     */
    private List<DiffLine> lineDiff(String[] userLines, String[] expectedLines) {
        List<DiffLine> diffs = new ArrayList<>();
        int maxLines = Math.max(userLines.length, expectedLines.length);
        
        for (int i = 0; i < maxLines; i++) {
            String userLine = i < userLines.length ? userLines[i] : null;
            String expectedLine = i < expectedLines.length ? expectedLines[i] : null;
            
            if (userLine == null) {
                diffs.add(new DiffLine(i + 1, null, expectedLine, DiffType.MISSING));
            } else if (expectedLine == null) {
                diffs.add(new DiffLine(i + 1, userLine, null, DiffType.EXTRA));
            } else if (linesEqual(userLine, expectedLine)) {
                diffs.add(new DiffLine(i + 1, userLine, expectedLine, DiffType.MATCH));
            } else {
                diffs.add(new DiffLine(i + 1, userLine, expectedLine, DiffType.DIFFERENT));
            }
        }
        
        return diffs;
    }

    /**
     * Verifica se outputs são idênticos.
     *
     * @param result Resultado da comparação
     * @return true se são idênticos
     */
    public boolean isMatch(ComparisonResult result) {
        return result.isMatch();
    }

    /**
     * Gera relatório formatado de diferenças.
     *
     * @param result Resultado da comparação
     * @return String formatada com relatório
     */
    public String generateDiffReport(ComparisonResult result) {
        return result.generateReport();
    }

    /**
     * Destaca diferenças entre duas linhas (char a char).
     *
     * @param line1 Primeira linha
     * @param line2 Segunda linha
     * @return String formatada com diferenças destacadas
     */
    public String highlightDifferences(String line1, String line2) {
        if (line1.equals(line2)) {
            return "Linhas idênticas";
        }

        StringBuilder result = new StringBuilder();
        int maxLen = Math.max(line1.length(), line2.length());

        for (int i = 0; i < maxLen; i++) {
            char c1 = i < line1.length() ? line1.charAt(i) : ' ';
            char c2 = i < line2.length() ? line2.charAt(i) : ' ';

            if (c1 != c2) {
                result.append('[').append(c1).append('≠').append(c2).append(']');
            } else {
                result.append(c1);
            }
        }

        return result.toString();
    }

    /**
     * Normaliza uma linha (remove whitespace extra, etc.).
     *
     * @param line Linha a normalizar
     * @return Linha normalizada
     */
    private String normalizeLine(String line) {
        if (line == null) {
            return "";
        }

        if (ignoreTrailingWhitespace) {
            line = line.trim();
        }

        if (!caseSensitive) {
            line = line.toLowerCase();
        }

        return line;
    }

    /**
     * Compara duas linhas considerando opções.
     *
     * @param line1 Primeira linha
     * @param line2 Segunda linha
     * @return true se são iguais
     */
    private boolean linesEqual(String line1, String line2) {
        return normalizeLine(line1).equals(normalizeLine(line2));
    }

    /**
     * Calcula percentagem de similaridade.
     *
     * @param matchingLines Linhas que coincidem
     * @param totalLines Total de linhas
     * @return Percentagem (0-100)
     */
    private double calculateSimilarity(int matchingLines, int totalLines) {
        if (totalLines == 0) {
            return 100.0;
        }
        return (matchingLines * 100.0) / totalLines;
    }

    // Getters e Setters

    public void setIgnoreTrailingWhitespace(boolean ignore) {
        this.ignoreTrailingWhitespace = ignore;
    }

    public void setCaseSensitive(boolean sensitive) {
        this.caseSensitive = sensitive;
    }
}

