package org.example.judge.core;

import org.example.judge.model.ExecutionResult;
import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.*;

/**
 * CodeExecutor - Compilador e executor de código Java.
 *
 * Responsabilidades:
 * - Compilar código Java usando javac via ProcessBuilder
 * - Executar código compilado usando java via ProcessBuilder
 * - Capturar stdout e stderr
 * - Implementar timeout para evitar loops infinitos
 * - Retornar resultados estruturados (ExecutionResult)
 *
 * Fluxo:
 * 1. compile(sourceFile) → compila UserCode.java
 * 2. Se sucesso → execute(className) → executa UserCode
 * 3. Captura output e erros
 * 4. Retorna ExecutionResult
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class CodeExecutor {

    private final FileManager fileManager;
    private static final int TIMEOUT_SECONDS = 5;

    /**
     * Construtor.
     *
     * @param fileManager Gestor de ficheiros
     */
    public CodeExecutor(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * Compila o código Java do utilizador.
     *
     * @param sourceFile Caminho do ficheiro .java
     * @return ExecutionResult com resultado da compilação
     */
    public ExecutionResult compile(Path sourceFile) {
        long startTime = System.currentTimeMillis();

        try {
            ProcessBuilder pb = createCompileProcess(sourceFile);
            Process process = pb.start();

            // Capturar stderr (erros de compilação)
            String errors = captureErrors(process);

            // Aguardar conclusão
            int exitCode = process.waitFor();
            long executionTime = System.currentTimeMillis() - startTime;

            boolean success = (exitCode == 0);

            return new ExecutionResult(success, "", errors, exitCode, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new ExecutionResult(false, "", "Erro ao compilar: " + e.getMessage(),
                                     -1, executionTime);
        }
    }

    /**
     * Executa o código Java compilado.
     *
     * @param className Nome da classe (sem .class)
     * @return ExecutionResult com output e erros
     */
    public ExecutionResult execute(String className) {
        long startTime = System.currentTimeMillis();
        
        try {
            ProcessBuilder pb = createExecutionProcess(className);
            Process process = pb.start();
            
            // Aguardar com timeout
            boolean finished = waitWithTimeout(process, TIMEOUT_SECONDS);
            
            if (!finished) {
                process.destroy();
                long executionTime = System.currentTimeMillis() - startTime;
                return new ExecutionResult(false, "", 
                    "Timeout: execução excedeu " + TIMEOUT_SECONDS + " segundos",
                    -1, executionTime);
            }
            
            String output = captureOutput(process);
            String errors = captureErrors(process);
            int exitCode = process.exitValue();
            long executionTime = System.currentTimeMillis() - startTime;
            
            boolean success = (exitCode == 0);
            
            return new ExecutionResult(success, output, errors, exitCode, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new ExecutionResult(false, "", "Erro ao executar: " + e.getMessage(),
                                     -1, executionTime);
        }
    }

    /**
     * Cria ProcessBuilder para compilação.
     *
     * @param sourceFile Ficheiro a compilar
     * @return ProcessBuilder configurado
     */
    private ProcessBuilder createCompileProcess(Path sourceFile) {
        ProcessBuilder pb = new ProcessBuilder("javac", sourceFile.toString());
        pb.directory(fileManager.getTmpDir().toFile());
        pb.redirectErrorStream(false);
        return pb;
    }

    /**
     * Cria ProcessBuilder para execução.
     *
     * @param className Nome da classe a executar
     * @return ProcessBuilder configurado
     */
    private ProcessBuilder createExecutionProcess(String className) {
        ProcessBuilder pb = new ProcessBuilder("java", "-cp",
            fileManager.getTmpDir().toString(), className);
        pb.directory(fileManager.getTmpDir().toFile());
        return pb;
    }

    /**
     * Captura output (stdout) de um processo.
     *
     * @param process Processo em execução
     * @return String com output
     * @throws IOException Se houver erro na leitura
     */
    private String captureOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Captura erros (stderr) de um processo.
     *
     * @param process Processo em execução
     * @return String com erros
     * @throws IOException Se houver erro na leitura
     */
    private String captureErrors(Process process) throws IOException {
        StringBuilder errors = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errors.append(line).append("\n");
            }
        }
        return errors.toString();
    }

    /**
     * Executa processo com timeout.
     *
     * @param process Processo a executar
     * @param timeoutSeconds Timeout em segundos
     * @return true se terminou dentro do timeout
     * @throws InterruptedException Se for interrompido
     */
    private boolean waitWithTimeout(Process process, int timeoutSeconds)
            throws InterruptedException {
        return process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Compila e executa código em sequência.
     *
     * @return ExecutionResult com resultado final
     */
    public ExecutionResult compileAndExecute() {
        // Compilar
        ExecutionResult compileResult = compile(fileManager.getUserCodePath());

        if (!compileResult.isSuccess()) {
            return compileResult; // Retornar erro de compilação
        }

        // Executar
        return execute(fileManager.getUserCodeClassName());
    }
}

