package org.example.judge.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.judge.model.CodeContext;
import org.example.judge.util.CodeAnalyzer;

/**
 * SuggestionEngine - Motor de sugestões inteligente baseado em análise contextual.
 *
 * COMPONENTE MAIS CRÍTICO DO SISTEMA
 *
 * Responsabilidades:
 * - Analisar contexto do código do utilizador (posição do cursor)
 * - Encontrar linha equivalente na solução oficial (SEM usar números de linha)
 * - Usar heurísticas de matching contextual
 * - Sugerir a PRÓXIMA linha de código
 *
 * Algoritmo de Matching Contextual:
 *
 * 1. ANÁLISE DO CÓDIGO DO UTILIZADOR:
 *    - Extrair 2-3 linhas anteriores ao cursor
 *    - Identificar tokens (palavras-chave, identificadores, operadores)
 *    - Identificar tipo de bloco (if, for, while, método, class)
 *    - Calcular nível de indentação
 *    - Criar perfil contextual (CodeContext)
 *
 * 2. VARREDURA DA SOLUÇÃO OFICIAL:
 *    - Para cada linha da solução:
 *      a) Criar CodeContext para essa linha
 *      b) Calcular score de similaridade:
 *         - +10 pontos por cada token em comum
 *         - +20 pontos se tipo de bloco é igual
 *         - +15 pontos se indentação é similar (±1)
 *         - +5 pontos por cada keyword em comum
 *         - +30 pontos se linhas anteriores têm >50% tokens em comum
 *      c) Guardar (linha, score)
 *
 * 3. SELEÇÃO DA MELHOR CORRESPONDÊNCIA:
 *    - Ordenar linhas por score (descendente)
 *    - Selecionar linha com maior score
 *    - Retornar a PRÓXIMA linha (a sugestão)
 *
 * Exemplo:
 * Código do utilizador (cursor na linha 5):
 *   int sum = 0;
 *   for (int i = 0; i < 10; i++) {
 *     |← CURSOR
 *
 * Análise:
 *   - tokens: [int, sum, for, i, <, 10]
 *   - blockType: FOR_LOOP
 *   - indentLevel: 2
 *
 * Solução oficial linha 5: "sum += i;" → SCORE: 95 (contexto idêntico)
 * Sugestão: "sum += i;"
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class SuggestionEngine {

    private final CodeAnalyzer codeAnalyzer;
    private Set<Integer> suggestedLines; // Histórico de linhas já sugeridas

    /**
     * Construtor.
     */
    public SuggestionEngine() {
        this.codeAnalyzer = new CodeAnalyzer();
        this.suggestedLines = new HashSet<>();
    }

    /**
     * Sugere a próxima linha baseada em análise contextual.
     *
     * MÉTODO PRINCIPAL DO MOTOR DE SUGESTÕES
     *
     * @param userCode Código completo do utilizador
     * @param cursorLine Linha atual do cursor (0-based)
     * @param officialSolution Solução oficial completa
     * @return Sugestão de linha ou null se não encontrar
     */
    public String suggestNextLine(String userCode, int cursorLine, String officialSolution) {
        if (userCode == null || officialSolution == null) return null;
        
        CodeContext userContext = analyzeUserContext(userCode, cursorLine);
        if (userContext == null) return null;
        
        List<ScoredLine> scoredLines = scoreOfficialLines(userContext, officialSolution);
        
        int bestMatchIndex = findBestMatch(scoredLines);
        if (bestMatchIndex == -1) return null;
        
        String suggestion = getNextLine(officialSolution, bestMatchIndex);
        
        if (suggestion != null) {
            suggestedLines.add(bestMatchIndex);
        }
        
        return suggestion;
    }

    /**
     * Sugere um snippet de várias linhas no bloco atual (ignora linhas vazias e ';').
     *
     * @param userCode Código até ao cursor ou código completo
     * @param cursorLine Linha do cursor (0-based)
     * @param officialSolution Solução oficial
     * @param maxLines Número máximo de linhas úteis a sugerir
     * @return Snippet sugerido ou null se não houver sugestão útil
     */
    public String suggestNextSnippet(String userCode, int cursorLine, String officialSolution, int maxLines) {
        if (userCode == null || officialSolution == null || maxLines <= 0) {
            return null;
        }

        CodeContext userContext = analyzeUserContext(userCode, cursorLine);
        if (userContext == null) {
            return null;
        }

        List<ScoredLine> scoredLines = scoreOfficialLines(userContext, officialSolution);
        int bestMatchIndex = findBestMatch(scoredLines);
        if (bestMatchIndex == -1) {
            return null;
        }

        String snippet = getNextSnippetInSameBlock(officialSolution, bestMatchIndex, maxLines);
        if (snippet != null) {
            suggestedLines.add(bestMatchIndex);
        }

        return snippet;
    }

    /**
     * Analisa o contexto do código do utilizador na posição do cursor.
     *
     * @param userCode Código completo do utilizador
     * @param cursorLine Linha do cursor (0-based)
     * @return CodeContext com informação contextual
     */
    private CodeContext analyzeUserContext(String userCode, int cursorLine) {
        String[] lines = userCode.split("\n");
        if (cursorLine < 0 || cursorLine >= lines.length) return null;
        
        String currentLine = lines[cursorLine];
        List<String> previousLines = codeAnalyzer.getPreviousLines(userCode, cursorLine, 3);
        List<String> tokens = codeAnalyzer.extractTokens(currentLine);
        CodeContext.BlockType blockType = codeAnalyzer.identifyBlock(userCode, cursorLine);
        int indentLevel = codeAnalyzer.calculateIndentationLevel(currentLine);
        List<String> keywords = codeAnalyzer.extractKeywords(currentLine);
        String methodSig = codeAnalyzer.getMethodSignature(userCode, cursorLine);
        
        return new CodeContext(cursorLine, currentLine, previousLines, 
                               new HashSet<>(tokens), blockType, indentLevel, 
                               methodSig, new HashSet<>(keywords));
    }

    /**
     * Varre a solução oficial e calcula scores para cada linha.
     *
     * @param userContext Contexto do utilizador
     * @param officialSolution Solução oficial completa
     * @return Lista de (lineIndex, score) ordenada por score
     */
    private List<ScoredLine> scoreOfficialLines(CodeContext userContext, String officialSolution) {
        List<ScoredLine> scoredLines = new ArrayList<>();
        String[] lines = officialSolution.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            CodeContext officialContext = createContextForLine(officialSolution, i);
            int score = calculateContextScore(userContext, officialContext);
            scoredLines.add(new ScoredLine(i, score));
        }
        
        scoredLines.sort((a, b) -> Integer.compare(b.score, a.score));
        return scoredLines;
    }

    /**
     * Calcula score de similaridade contextual entre dois contextos.
     *
     * Pontuação:
     * - +10 por cada token em comum
     * - +20 se blockType é igual
     * - +15 se indentLevel é similar (±1)
     * - +5 por cada keyword em comum
     * - +30 se linhas anteriores têm >50% tokens em comum
     *
     * @param userCtx Contexto do utilizador
     * @param officialCtx Contexto da solução oficial
     * @return Score de similaridade (quanto maior, mais similar)
     */
    private int calculateContextScore(CodeContext userCtx, CodeContext officialCtx) {
        int score = 0;

        Set<String> userTokenSet = new HashSet<>(userCtx.getTokens());
        Set<String> officialTokenSet = new HashSet<>(officialCtx.getTokens());
        Set<String> commonTokens = intersection(userTokenSet, officialTokenSet);
        score += commonTokens.size() * 10;

        if (userCtx.getBlockType() == officialCtx.getBlockType()) {
            score += 20;
        }

        if (Math.abs(userCtx.getIndentationLevel() - officialCtx.getIndentationLevel()) <= 1) {
            score += 15;
        }

        Set<String> userKeywordSet = new HashSet<>(userCtx.getKeywords());
        Set<String> officialKeywordSet = new HashSet<>(officialCtx.getKeywords());
        Set<String> commonKeywords = intersection(userKeywordSet, officialKeywordSet);
        score += commonKeywords.size() * 5;

        double prevLinesSimilarity = calculatePreviousLinesSimilarity(userCtx, officialCtx);
        if (prevLinesSimilarity > 0.5) {
            score += 30;
        }

        return score;
    }

    /**
     * Cria um CodeContext para uma linha específica da solução oficial.
     */
    private CodeContext createContextForLine(String code, int lineNumber) {
        String[] lines = code.split("\n");
        if (lineNumber < 0 || lineNumber >= lines.length) return null;
        
        String currentLine = lines[lineNumber];
        List<String> previousLines = codeAnalyzer.getPreviousLines(code, lineNumber, 3);
        List<String> tokens = codeAnalyzer.extractTokens(currentLine);
        CodeContext.BlockType blockType = codeAnalyzer.identifyBlock(code, lineNumber);
        int indentLevel = codeAnalyzer.calculateIndentationLevel(currentLine);
        List<String> keywords = codeAnalyzer.extractKeywords(currentLine);
        String methodSig = codeAnalyzer.getMethodSignature(code, lineNumber);
        
        return new CodeContext(lineNumber, currentLine, previousLines, 
                               new HashSet<>(tokens), blockType, indentLevel, 
                               methodSig, new HashSet<>(keywords));
    }

    /**
     * Calcula similaridade entre linhas anteriores de dois contextos.
     *
     * @param userCtx Contexto do utilizador
     * @param officialCtx Contexto da solução
     * @return Valor entre 0 e 1 (percentagem de tokens em comum)
     */
    private double calculatePreviousLinesSimilarity(CodeContext userCtx, CodeContext officialCtx) {
        // Extrair tokens de todas as linhas anteriores
        String userPrevCode = String.join("\n", userCtx.getPreviousLines());
        String officialPrevCode = String.join("\n", officialCtx.getPreviousLines());

        List<String> userTokens = codeAnalyzer.extractTokens(userPrevCode);
        List<String> officialTokens = codeAnalyzer.extractTokens(officialPrevCode);

        Set<String> userSet = new HashSet<>(userTokens);
        Set<String> officialSet = new HashSet<>(officialTokens);

        // Calcular interseção
        Set<String> intersection = intersection(userSet, officialSet);

        // Calcular união
        Set<String> union = new HashSet<>(userSet);
        union.addAll(officialSet);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Encontra a linha com melhor match (maior score).
     *
     * @param scoredLines Lista de linhas com scores
     * @return Índice da linha com maior score ou -1
     */
    private int findBestMatch(List<ScoredLine> scoredLines) {
        // Filtrar linhas já sugeridas e com score muito baixo
        for (ScoredLine scored : scoredLines) {
            if (!suggestedLines.contains(scored.lineIndex) && scored.score >= 20) {
                return scored.lineIndex;
            }
        }

        return -1; // Não encontrou match adequado
    }

    /**
     * Obtém a próxima linha após o match encontrado.
     *
     * @param solution Solução oficial completa
     * @param matchedLineIndex Índice da linha matched
     * @return Próxima linha ou null se não existir
     */
    private String getNextLine(String solution, int matchedLineIndex) {
        String[] lines = solution.split("\n");

        int nextIndex = matchedLineIndex + 1;

        if (nextIndex < lines.length) {
            return lines[nextIndex].trim();
        }

        return null; // Não há próxima linha
    }

    /**
     * Obtém snippet de linhas seguintes no mesmo bloco lógico.
     */
    private String getNextSnippetInSameBlock(String solution, int matchedLineIndex, int maxLines) {
        String[] lines = solution.split("\n", -1);
        int start = matchedLineIndex + 1;
        if (start >= lines.length) {
            return null;
        }

        int[] depthBeforeLine = buildBraceDepthBeforeLine(lines);
        int firstMeaningful = -1;
        for (int i = start; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty() || ";".equals(trimmed)) {
                continue;
            }
            firstMeaningful = i;
            break;
        }

        if (firstMeaningful == -1) {
            return null;
        }

        int baseDepth = depthBeforeLine[firstMeaningful];
        StringBuilder snippet = new StringBuilder();
        int usefulLineCount = 0;

        for (int i = firstMeaningful; i < lines.length; i++) {
            String current = lines[i];
            String trimmed = current.trim();
            int depth = depthBeforeLine[i];

            if (i > firstMeaningful && depth < baseDepth) {
                break;
            }

            if (i > firstMeaningful && depth == baseDepth && trimmed.startsWith("}")) {
                break;
            }

            if (trimmed.isEmpty() || ";".equals(trimmed)) {
                continue;
            }

            if (snippet.length() > 0) {
                snippet.append("\n");
            }
            snippet.append(current);
            usefulLineCount++;

            if (usefulLineCount >= maxLines) {
                break;
            }
        }

        if (snippet.isEmpty()) {
            return null;
        }

        return snippet.toString();
    }

    /**
     * Calcula profundidade de chavetas antes de cada linha.
     */
    private int[] buildBraceDepthBeforeLine(String[] lines) {
        int[] depth = new int[lines.length];
        int currentDepth = 0;

        for (int i = 0; i < lines.length; i++) {
            depth[i] = Math.max(0, currentDepth);
            String line = lines[i];

            for (int j = 0; j < line.length(); j++) {
                char ch = line.charAt(j);
                if (ch == '{') {
                    currentDepth++;
                } else if (ch == '}') {
                    currentDepth = Math.max(0, currentDepth - 1);
                }
            }
        }

        return depth;
    }

    /**
     * Calcula interseção entre dois sets.
     *
     * @param set1 Primeiro set
     * @param set2 Segundo set
     * @return Set com elementos em comum
     */
    private <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    /**
     * Limpa o histórico de sugestões.
     */
    public void clearHistory() {
        suggestedLines.clear();
    }

    /**
     * Classe interna para guardar linha + score.
     */
    private static class ScoredLine {
        int lineIndex;
        int score;

        ScoredLine(int lineIndex, int score) {
            this.lineIndex = lineIndex;
            this.score = score;
        }
    }
}

