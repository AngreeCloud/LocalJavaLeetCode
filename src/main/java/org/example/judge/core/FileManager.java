package org.example.judge.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private Path userCodePath;
    private Path userOutputPath;

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
        userCodePath = tmpDir.resolve("UserCode.java");
        userOutputPath = tmpDir.resolve("user-output.txt");
    }

    /**
     * Lê e retorna o conteúdo de um ficheiro de enunciado.
     *
     * @param file Ficheiro a ler
     * @return Conteúdo do ficheiro
     * @throws IOException Se houver erro na leitura
     */
    public String readStatement(File file) throws IOException {
        return Files.readString(file.toPath());
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

