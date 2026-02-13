package org.example.judge.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.example.judge.model.CodeContext.BlockType;

/**
 * CodeAnalyzer - Analisador estático de código Java.
 *
 * Responsabilidades:
 * - Extrair tokens (palavras-chave, identificadores, operadores)
 * - Identificar tipo de bloco (class, method, if, for, while, try, etc.)
 * - Calcular nível de indentação
 * - Extrair palavras-chave Java
 * - Normalizar whitespace
 * - Identificar se está dentro de método
 * - Obter assinatura de método
 *
 * Utilizado por:
 * - SuggestionEngine (análise contextual)
 * - Outros componentes que precisem analisar código
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class CodeAnalyzer {

    // Palavras-chave Java
    private static final Set<String> JAVA_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch",
        "char", "class", "const", "continue", "default", "do", "double",
        "else", "enum", "extends", "final", "finally", "float", "for",
        "goto", "if", "implements", "import", "instanceof", "int", "interface",
        "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized",
        "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
    );

    // Padrões regex
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\w+");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Construtor.
     */
    public CodeAnalyzer() {
    }

    /**
     * Extrai todos os tokens de um código.
     *
     * Tokens incluem:
     * - Palavras-chave (if, for, int, etc.)
     * - Identificadores (nomes de variáveis, métodos, classes)
     * - Operadores (opcional)
     *
     * @param code Código a analisar
     * @return Lista de tokens
     */
    public List<String> extractTokens(String code) {
        List<String> tokens = new ArrayList<>();
        var matcher = TOKEN_PATTERN.matcher(code);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    /**
     * Identifica o tipo de bloco onde uma linha está localizada.
     *
     * @param code Código completo
     * @param lineNumber Número da linha (0-based)
     * @return BlockType (CLASS, METHOD, IF, FOR, WHILE, TRY, etc.)
     */
    public BlockType identifyBlock(String code, int lineNumber) {
        String[] lines = code.split("\n");
        if (lineNumber < 0 || lineNumber >= lines.length) {
            return BlockType.UNKNOWN;
        }

        int braceLevel = 0;
        for (int i = lineNumber; i >= 0; i--) {
            String line = lines[i].trim();
            
            // Contar chaves para contexto
            for (char c : line.toCharArray()) {
                if (c == '}') braceLevel++;
                if (c == '{') braceLevel--;
            }
            
            // Se encontrou abertura de bloco
            if (braceLevel < 0) {
                if (line.contains("for") && line.contains("(")) return BlockType.FOR_LOOP;
                if (line.contains("while") && line.contains("(")) return BlockType.WHILE_LOOP;
                if (line.contains("if") && line.contains("(")) return BlockType.IF_BLOCK;
                if (line.contains("try")) return BlockType.TRY_BLOCK;
                if (line.matches(".*(public|private|protected).*\\(.*")) return BlockType.METHOD;
                if (line.contains("class")) return BlockType.CLASS;
            }
        }
        
        return BlockType.UNKNOWN;
    }

    /**
     * Calcula o nível de indentação de uma linha.
     *
     * @param line Linha a analisar
     * @return Nível de indentação (0, 1, 2, ...)
     */
    public int calculateIndentationLevel(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') spaces++;
            else if (c == '\t') spaces += 4;
            else break;
        }
        return spaces / 4;
    }

    /**
     * Extrai palavras-chave Java presentes no código.
     *
     * @param code Código a analisar
     * @return Lista de keywords encontradas
     */
    public List<String> extractKeywords(String code) {
        List<String> tokens = extractTokens(code);
        List<String> keywords = new ArrayList<>();

        for (String token : tokens) {
            if (JAVA_KEYWORDS.contains(token)) {
                keywords.add(token);
            }
        }

        return keywords;
    }

    /**
     * Normaliza whitespace (remove espaços extra, tabs, etc.).
     *
     * @param code Código a normalizar
     * @return Código normalizado
     */
    public String normalizeWhitespace(String code) {
        return WHITESPACE_PATTERN.matcher(code).replaceAll(" ").trim();
    }

    /**
     * Verifica se uma linha está dentro de um método.
     *
     * @param code Código completo
     * @param lineNumber Número da linha (0-based)
     * @return true se está dentro de um método
     */
    public boolean isInsideMethod(String code, int lineNumber) {
        BlockType blockType = identifyBlock(code, lineNumber);
        return blockType == BlockType.METHOD ||
               blockType == BlockType.FOR_LOOP ||
               blockType == BlockType.WHILE_LOOP ||
               blockType == BlockType.IF_BLOCK;
    }

    /**
     * Obtém a assinatura do método onde a linha está.
     *
     * @param code Código completo
     * @param lineNumber Número da linha (0-based)
     * @return Assinatura do método ou null se não estiver em método
     */
    public String getMethodSignature(String code, int lineNumber) {
        String[] lines = code.split("\n");

        if (lineNumber < 0 || lineNumber >= lines.length) {
            return null;
        }

        // Procurar para trás por assinatura de método
        for (int i = lineNumber; i >= 0; i--) {
            String line = lines[i].trim();

            // Padrão: (public|private|protected)? (static)? tipo nome(...)
            if (line.matches(".*(public|private|protected|static).*\\w+\\s+\\w+\\s*\\(.*\\).*")) {
                return line;
            }
        }

        return null;
    }

    /**
     * Extrai linhas anteriores a uma posição.
     *
     * @param code Código completo
     * @param lineNumber Linha de referência (0-based)
     * @param count Quantidade de linhas anteriores
     * @return Lista de linhas anteriores
     */
    public List<String> getPreviousLines(String code, int lineNumber, int count) {
        String[] lines = code.split("\n");
        List<String> previousLines = new ArrayList<>();

        int startLine = Math.max(0, lineNumber - count);
        int endLine = Math.min(lineNumber, lines.length);

        for (int i = startLine; i < endLine; i++) {
            previousLines.add(lines[i]);
        }

        return previousLines;
    }

    /**
     * Identifica se uma linha contém um operador específico.
     *
     * @param line Linha a verificar
     * @param operator Operador (=, ==, +=, etc.)
     * @return true se contém o operador
     */
    public boolean containsOperator(String line, String operator) {
        return line.contains(operator);
    }

    /**
     * Extrai nomes de variáveis de uma linha.
     *
     * @param line Linha a analisar
     * @return Lista de identificadores que parecem variáveis
     */
    public List<String> extractVariables(String line) {
        List<String> variables = new ArrayList<>();
        List<String> tokens = extractTokens(line);
        
        for (String token : tokens) {
            if (!JAVA_KEYWORDS.contains(token) && 
                !Character.isUpperCase(token.charAt(0)) &&
                !token.matches("\\d+")) {
                variables.add(token);
            }
        }
        
        return variables;
    }

    /**
     * Verifica se o código Java parece sintaticamente válido (básico).
     *
     * @param code Código a validar
     * @return true se parece válido
     */
    public boolean seemsValid(String code) {
        if (code == null || code.trim().isEmpty()) return false;
        if (!code.contains("class")) return false;
        
        int braces = 0, brackets = 0, parens = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
            if (c == '[') brackets++;
            if (c == ']') brackets--;
            if (c == '(') parens++;
            if (c == ')') parens--;
        }
        
        return braces == 0 && brackets == 0 && parens == 0;
    }

    /**
     * Conta nível de profundidade de blocos na linha.
     *
     * @param code Código completo
     * @param lineNumber Linha a verificar
     * @return Profundidade (quantidade de blocos aninhados)
     */
    public int calculateBlockDepth(String code, int lineNumber) {
        String[] lines = code.split("\n");
        if (lineNumber < 0 || lineNumber >= lines.length) return 0;
        
        int depth = 0;
        for (int i = 0; i <= lineNumber; i++) {
            for (char c : lines[i].toCharArray()) {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        
        return Math.max(0, depth);
    }

    /**
     * Verifica se o código solicita input do utilizador.
     * 
     * Deteta:
     * - Scanner com System.in
     * - BufferedReader com InputStreamReader(System.in)
     * - System.console()
     * - Outros padrões de input interativo
     *
     * @param code Código a verificar
     * @return true se solicita input do utilizador
     */
    public boolean requiresUserInput(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        // Padrões que indicam input do utilizador
        return code.contains("Scanner") && code.contains("System.in") ||
               code.contains("BufferedReader") && code.contains("System.in") ||
               code.contains("InputStreamReader(System.in)") ||
               code.contains("System.console()") ||
               code.contains(".nextInt()") ||
               code.contains(".nextLine()") ||
               code.contains(".nextDouble()") ||
               code.contains(".next()") ||
               code.contains(".readLine()") && code.contains("System.in");
    }

    /**
     * Verifica se o código requer execução interativa (terminal).
     * 
     * Deteta código que:
     * - Solicita input do utilizador
     * - Trabalha com ficheiros (FileReader, FileWriter, etc.)
     * - Usa Sockets (TCP/IP)
     * - Usa Serialização (ObjectInputStream, ObjectOutputStream)
     * - Qualquer I/O que precise de recursos externos
     *
     * @param code Código a verificar
     * @return true se requer execução em terminal interativo
     */
    public boolean requiresInteractiveExecution(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        // Input do utilizador
        if (requiresUserInput(code)) {
            return true;
        }
        
        // Trabalho com ficheiros de texto
        if (code.contains("FileReader") || 
            code.contains("FileWriter") || 
            code.contains("BufferedWriter") ||
            code.contains("PrintWriter") && code.contains("FileOutputStream") ||
            code.contains("Files.write") ||
            code.contains("Files.read")) {
            return true;
        }
        
        // Trabalho com ficheiros binários
        if (code.contains("FileInputStream") || 
            code.contains("FileOutputStream") ||
            code.contains("DataInputStream") ||
            code.contains("DataOutputStream") ||
            code.contains("RandomAccessFile")) {
            return true;
        }
        
        // Serialização
        if (code.contains("ObjectInputStream") || 
            code.contains("ObjectOutputStream") ||
            code.contains("Serializable")) {
            return true;
        }
        
        // Networking (TCP/IP, UDP)
        if (code.contains("Socket") || 
            code.contains("ServerSocket") ||
            code.contains("DatagramSocket") ||
            code.contains("InetAddress")) {
            return true;
        }
        
        return false;
    }
}

