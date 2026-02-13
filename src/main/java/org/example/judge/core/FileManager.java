package org.example.judge.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * FileManager - Gestor de todos os ficheiros do sistema.
 *
 * Responsabilidades:
 * - Inicializar diretório temporário (/tmp/judge-{timestamp}/)
 * - Guardar enunciados, soluções e código do utilizador
 * - Ler ficheiros
 * - Cleanup de ficheiros temporários
 * - Fornecer caminhos para outros componentes
 *
 * Estrutura do diretório temporário:
 * /tmp/judge-{timestamp}/
 *   ├── statement.txt
 *   ├── solution.txt
 *   ├── UserCode.java
 *   ├── UserCode.class
 *   └── user-output.txt
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class FileManager {

    private Path tmpDir;
    private Path statementPath;
    private Path solutionPath;
    private Path solutionDir;  // Diretório para múltiplos ficheiros
    private Path userCodePath;
    private Path userOutputPath;
    private Map<String, Path> solutionFiles;  // Nome -> Path dos ficheiros de solução

    private static final String USER_CODE_CLASS_NAME = "UserCode";

    /**
     * Construtor - Inicializa o diretório temporário.
     *
     * @throws IOException Se houver erro ao criar diretório
     */
    public FileManager() throws IOException {
        initializeTempDirectory();
    }

    /**
     * Inicializa o diretório temporário com timestamp único.
     *
     * @throws IOException Se houver erro ao criar diretório
     */
    private void initializeTempDirectory() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "judge-" + timestamp);
        Files.createDirectories(tmpDir);
        
        statementPath = tmpDir.resolve("statement.txt");
        solutionPath = tmpDir.resolve("solution.txt");
        solutionDir = tmpDir.resolve("solution");
        Files.createDirectories(solutionDir);
        userCodePath = tmpDir.resolve("UserCode.java");
        userOutputPath = tmpDir.resolve("user-output.txt");
        solutionFiles = new HashMap<>();
    }

    /**
     * Lê e retorna o conteúdo de um ficheiro de enunciado.
     * Suporta formatos: TXT, PDF, DOCX
     *
     * @param file Ficheiro a ler
     * @return Conteúdo do ficheiro
     * @throws IOException Se houver erro na leitura
     */
    public String readStatement(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".pdf")) {
            return readPdfFile(file);
        } else if (fileName.endsWith(".docx")) {
            return readDocxFile(file);
        } else {
            // TXT ou outros formatos de texto
            return Files.readString(file.toPath());
        }
    }

    /**
     * Lê conteúdo de um ficheiro PDF.
     *
     * @param file Ficheiro PDF
     * @return Texto extraído do PDF
     * @throws IOException Se houver erro na leitura
     */
    private String readPdfFile(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Lê conteúdo de um ficheiro DOCX.
     *
     * @param file Ficheiro DOCX
     * @return Texto extraído do DOCX
     * @throws IOException Se houver erro na leitura
     */
    private String readDocxFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString();
        }
    }

    /**
     * Guarda o enunciado no diretório temporário.
     *
     * @param file Ficheiro de origem
     * @throws IOException Se houver erro ao copiar
     */
    public void saveStatement(File file) throws IOException {
        Files.copy(file.toPath(), statementPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Guarda a solução oficial no diretório temporário.
     *
     * @param file Ficheiro de origem
     * @throws IOException Se houver erro ao copiar
     */
    public void saveSolution(File file) throws IOException {
        Files.copy(file.toPath(), solutionPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Guarda múltiplos ficheiros de solução.
     *
     * @param files Lista de ficheiros de solução
     * @throws IOException Se houver erro ao copiar
     */
    public void saveSolutionFiles(List<File> files) throws IOException {
        solutionFiles.clear();
        for (File file : files) {
            String fileName = file.getName();
            Path destPath = solutionDir.resolve(fileName);
            Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            solutionFiles.put(fileName, destPath);
        }
    }

    /**
     * Lê o conteúdo de um ficheiro de solução específico.
     *
     * @param fileName Nome do ficheiro
     * @return Conteúdo do ficheiro
     * @throws IOException Se houver erro na leitura
     */
    public String readSolutionFile(String fileName) throws IOException {
        Path filePath = solutionFiles.get(fileName);
        if (filePath != null && Files.exists(filePath)) {
            return Files.readString(filePath);
        }
        return "";
    }

    /**
     * Guarda o conteúdo editado de um ficheiro de solução.
     *
     * @param fileName Nome do ficheiro
     * @param content Conteúdo atualizado
     * @throws IOException Se houver erro ao escrever
     */
    public void saveSolutionFileContent(String fileName, String content) throws IOException {
        Path filePath = solutionFiles.get(fileName);
        if (filePath != null) {
            Files.writeString(filePath, content);
        }
    }

    /**
     * Retorna lista de nomes dos ficheiros de solução.
     *
     * @return Lista de nomes de ficheiros
     */
    public List<String> getSolutionFileNames() {
        return solutionFiles.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Verifica se há ficheiros de solução carregados.
     *
     * @return true se há ficheiros
     */
    public boolean hasSolutionFiles() {
        return !solutionFiles.isEmpty();
    }

    /**
     * Retorna os paths de todos os ficheiros de solução.
     *
     * @return Lista de paths
     */
    public List<Path> getSolutionFilePaths() {
        return solutionFiles.values().stream()
                .collect(Collectors.toList());
    }

    /**
     * Encontra o ficheiro que contém o método main.
     *
     * @return Nome completo qualificado da classe (package.ClassName) ou null
     */
    public String findMainClassName() {
        for (Map.Entry<String, Path> entry : solutionFiles.entrySet()) {
            try {
                String content = Files.readString(entry.getValue());
                if (content.contains("public static void main")) {
                    String fileName = entry.getKey();
                    String className = fileName.substring(0, fileName.lastIndexOf('.'));
                    
                    // Extrair package se existir
                    String packageName = extractPackageName(content);
                    
                    if (packageName != null && !packageName.isEmpty()) {
                        return packageName + "." + className;
                    }
                    return className;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Extrai o nome do package de um código Java.
     *
     * @param content Conteúdo do ficheiro Java
     * @return Nome do package ou null se não existir
     */
    private String extractPackageName(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
                // Extrair: "package ficha5_pw.ex1;" -> "ficha5_pw.ex1"
                return trimmed.substring(8, trimmed.length() - 1).trim();
            }
        }
        return null;
    }

    /**
     * Obtém o nome completo qualificado da classe de um ficheiro específico.
     *
     * @param fileName Nome do ficheiro (ex: "ServidorEco.java")
     * @return Nome completo qualificado (package.ClassName) ou null
     */
    public String getClassNameFromFile(String fileName) {
        Path filePath = solutionFiles.get(fileName);
        if (filePath == null) {
            return null;
        }

        try {
            String content = Files.readString(filePath);
            String className = fileName.substring(0, fileName.lastIndexOf('.'));
            
            // Extrair package se existir
            String packageName = extractPackageName(content);
            
            if (packageName != null && !packageName.isEmpty()) {
                return packageName + "." + className;
            }
            return className;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Guarda o código do utilizador em UserCode.java.
     *
     * @param code Código Java
     * @throws IOException Se houver erro ao escrever
     */
    public void saveUserCode(String code) throws IOException {
        Files.writeString(userCodePath, code);
    }

    /**
     * Lê a solução oficial.
     *
     * @return Conteúdo da solução (output esperado)
     * @throws IOException Se houver erro na leitura
     */
    public String readSolution() throws IOException {
        if (!Files.exists(solutionPath)) return "";
        return Files.readString(solutionPath);
    }

    /**
     * Guarda o output do utilizador.
     *
     * @param output Output capturado
     * @throws IOException Se houver erro ao escrever
     */
    public void saveUserOutput(String output) throws IOException {
        Files.writeString(userOutputPath, output);
    }

    /**
     * Lê o output do utilizador.
     *
     * @return Output guardado
     * @throws IOException Se houver erro na leitura
     */
    public String readUserOutput() throws IOException {
        if (!Files.exists(userOutputPath)) return "";
        return Files.readString(userOutputPath);
    }

    /**
     * Retorna o caminho do ficheiro UserCode.java.
     *
     * @return Path do código do utilizador
     */
    public Path getUserCodePath() {
        return userCodePath;
    }

    /**
     * Retorna o diretório temporário.
     *
     * @return Path do diretório temporário
     */
    public Path getTmpDir() {
        return tmpDir;
    }

    /**
     * Retorna o diretório de solução (para compilação multi-file).
     *
     * @return Path do diretório de solução
     */
    public Path getSolutionDir() {
        return solutionDir;
    }

    /**
     * Retorna o nome da classe do utilizador.
     *
     * @return Nome da classe (sem .java)
     */
    public String getUserCodeClassName() {
        return USER_CODE_CLASS_NAME;
    }

    /**
     * Lê conteúdo de qualquer ficheiro.
     *
     * @param path Caminho do ficheiro
     * @return Conteúdo do ficheiro
     * @throws IOException Se houver erro na leitura
     */
    public String readFileContent(Path path) throws IOException {
        return Files.readString(path);
    }

    /**
     * Remove todos os ficheiros temporários.
     */
    public void cleanup() {
        if (tmpDir != null && Files.exists(tmpDir)) {
            try {
                Files.walk(tmpDir)
                     .sorted(java.util.Comparator.reverseOrder())
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             System.err.println("Erro ao remover: " + path);
                         }
                     });
            } catch (IOException e) {
                System.err.println("Erro ao fazer cleanup: " + e.getMessage());
            }
        }
    }
}

