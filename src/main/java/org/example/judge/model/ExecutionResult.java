package org.example.judge.model;

/**
 * ExecutionResult - Modelo que representa o resultado de uma compilação ou execução.
 *
 * Utilizado por:
 * - CodeExecutor (retorna este objeto)
 * - UIController (processa o resultado)
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class ExecutionResult {

    private boolean success;
    private String output;
    private String errors;
    private int exitCode;
    private long executionTimeMs;

    /**
     * Construtor completo.
     *
     * @param success Se foi bem-sucedido
     * @param output Output do programa (stdout)
     * @param errors Erros (stderr ou exceções)
     * @param exitCode Código de saída
     * @param executionTimeMs Tempo de execução em milissegundos
     */
    public ExecutionResult(boolean success, String output, String errors,
                          int exitCode, long executionTimeMs) {
        this.success = success;
        this.output = output;
        this.errors = errors;
        this.exitCode = exitCode;
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Construtor simplificado para falhas.
     *
     * @param success false
     * @param errors Mensagem de erro
     */
    public ExecutionResult(boolean success, String errors) {
        this(success, "", errors, -1, 0);
    }

    // Getters e Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Verifica se há erros.
     *
     * @return true se há erros
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Retorna output formatado para exibição.
     *
     * @return String formatada
     */
    public String getFormattedOutput() {
        StringBuilder sb = new StringBuilder();

        if (success) {
            sb.append("✓ Execução bem-sucedida\n");
            sb.append("Tempo: ").append(executionTimeMs).append(" ms\n");
            sb.append("\n--- OUTPUT ---\n");
            sb.append(output);
        } else {
            sb.append("✗ Execução falhou\n");
            sb.append("Exit code: ").append(exitCode).append("\n");
            sb.append("\n--- ERROS ---\n");
            sb.append(errors);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "ExecutionResult{" +
                "success=" + success +
                ", exitCode=" + exitCode +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}

