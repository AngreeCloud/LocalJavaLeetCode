package org.example.judge.model;

import java.util.*;

/**
 * CodeContext - Modelo que representa o contexto de uma posição no código.
 *
 * Utilizado por:
 * - SuggestionEngine (análise contextual)
 * - CodeAnalyzer (cria este objeto)
 *
 * Contém informação sobre:
 * - Linha atual e linhas anteriores
 * - Tokens extraídos
 * - Tipo de bloco (if, for, while, method, etc.)
 * - Nível de indentação
 * - Palavras-chave presentes
 * - Assinatura de método (se aplicável)
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class CodeContext {

    private int lineNumber;
    private String currentLine;
    private List<String> previousLines;
    private Set<String> tokens;
    private BlockType blockType;
    private int indentationLevel;
    private String methodSignature;
    private Set<String> keywords;

    /**
     * Construtor completo.
     */
    public CodeContext(int lineNumber, String currentLine, List<String> previousLines,
                      Set<String> tokens, BlockType blockType, int indentationLevel,
                      String methodSignature, Set<String> keywords) {
        this.lineNumber = lineNumber;
        this.currentLine = currentLine;
        this.previousLines = previousLines != null ? previousLines : new ArrayList<>();
        this.tokens = tokens != null ? tokens : new HashSet<>();
        this.blockType = blockType != null ? blockType : BlockType.UNKNOWN;
        this.indentationLevel = indentationLevel;
        this.methodSignature = methodSignature;
        this.keywords = keywords != null ? keywords : new HashSet<>();
    }

    /**
     * Construtor simplificado.
     */
    public CodeContext() {
        this(0, "", new ArrayList<>(), new HashSet<>(), BlockType.UNKNOWN,
             0, null, new HashSet<>());
    }

    /**
     * Verifica se este contexto é similar a outro.
     *
     * @param other Outro contexto
     * @return true se são similares
     */
    public boolean isSimilarTo(CodeContext other) {
        // Similaridade baseada em:
        // - Tipo de bloco
        // - Nível de indentação (±1)
        // - Tokens em comum (>50%)

        if (this.blockType != other.blockType) {
            return false;
        }

        if (Math.abs(this.indentationLevel - other.indentationLevel) > 1) {
            return false;
        }

        // Calcular tokens em comum
        Set<String> commonTokens = new HashSet<>(this.tokens);
        commonTokens.retainAll(other.tokens);

        double similarity = (double) commonTokens.size() /
                           Math.max(this.tokens.size(), other.tokens.size());

        return similarity > 0.5;
    }

    // Getters e Setters

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(String currentLine) {
        this.currentLine = currentLine;
    }

    public List<String> getPreviousLines() {
        return previousLines;
    }

    public void setPreviousLines(List<String> previousLines) {
        this.previousLines = previousLines;
    }

    public Set<String> getTokens() {
        return tokens;
    }

    public void setTokens(Set<String> tokens) {
        this.tokens = tokens;
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public void setIndentationLevel(int indentationLevel) {
        this.indentationLevel = indentationLevel;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "CodeContext{" +
                "lineNumber=" + lineNumber +
                ", blockType=" + blockType +
                ", indentLevel=" + indentationLevel +
                ", tokens=" + tokens.size() +
                ", keywords=" + keywords +
                '}';
    }

    /**
     * BlockType - Tipo de bloco de código.
     */
    public enum BlockType {
        UNKNOWN,        // Desconhecido
        CLASS,          // Dentro de declaração de classe
        METHOD,         // Dentro de método
        IF_BLOCK,       // Dentro de if
        ELSE_BLOCK,     // Dentro de else
        FOR_LOOP,       // Dentro de for
        WHILE_LOOP,     // Dentro de while
        DO_WHILE_LOOP,  // Dentro de do-while
        TRY_BLOCK,      // Dentro de try
        CATCH_BLOCK,    // Dentro de catch
        FINALLY_BLOCK,  // Dentro de finally
        SWITCH_CASE,    // Dentro de switch
        LAMBDA          // Dentro de expressão lambda
    }
}

