package org.example.judge.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.*;

/**
 * IntegratedTerminal - Terminal integrado na aplicação.
 *
 * Permite executar processos Java com I/O interativo sem abrir janela externa.
 * 
 * Funcionalidades:
 * - Exibe output do processo em tempo real
 * - Permite input do utilizador através de TextField
 * - Suporta programas que leem/escrevem ficheiros
 * - Suporta Sockets e Serialização
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class IntegratedTerminal extends BorderPane {

    private TextArea outputArea;
    private TextField inputField;
    private Process process;
    private PrintWriter processInput;
    private Thread outputReaderThread;
    private Thread errorReaderThread;
    private boolean isRunning;

    /**
     * Construtor.
     */
    public IntegratedTerminal() {
        createUI();
    }

    /**
     * Cria a interface do terminal.
     */
    private void createUI() {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10));

        // Área de output (terminal)
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                          "-fx-font-size: 14px; " +
                          "-fx-background-color: #000000; " +
                          "-fx-text-fill: #00ff00; " +
                          "-fx-control-inner-background: #000000;");
        outputArea.setPrefHeight(500);

        ScrollPane scrollPane = new ScrollPane(outputArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Campo de input
        inputField = new TextField();
        inputField.setPromptText("Digite aqui e pressione Enter para enviar input...");
        inputField.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                          "-fx-font-size: 14px; " +
                          "-fx-background-color: #0c0c0c; " +
                          "-fx-text-fill: #00ff00; " +
                          "-fx-prompt-text-fill: #555555;");
        inputField.setOnAction(e -> handleInput());

        container.getChildren().addAll(scrollPane, inputField);
        setCenter(container);
    }

    /**
     * Executa um programa Java no terminal integrado.
     *
     * @param className Nome completo da classe
     * @param workingDirectory Diretório de trabalho
     */
    public void executeProgram(String className, File workingDirectory) {
        if (isRunning) {
            appendOutput("\n[Erro: Já existe um programa em execução]\n");
            return;
        }

        try {
            appendOutput("========================================\n");
            appendOutput("Iniciando programa: " + className + "\n");
            appendOutput("Diretório: " + workingDirectory.getAbsolutePath() + "\n");
            appendOutput("Comando: java -cp . " + className + "\n");
            appendOutput("========================================\n\n");

            // Criar processo
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", className);
            pb.directory(workingDirectory);
            pb.redirectErrorStream(false);

            process = pb.start();
            isRunning = true;

            // Criar writer para enviar input ao processo
            processInput = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);

            // Thread para ler output (stdout) - ler em tempo real, não linha por linha
            outputReaderThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    char[] buffer = new char[256];
                    int charsRead;
                    while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                        String output = new String(buffer, 0, charsRead);
                        Platform.runLater(() -> appendOutput(output));
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        Platform.runLater(() -> appendOutput("\n[Erro ao ler output: " + e.getMessage() + "]\n"));
                    }
                }
            });
            outputReaderThread.setDaemon(true);
            outputReaderThread.start();

            // Thread para ler erros (stderr) - ler em tempo real
            errorReaderThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    char[] buffer = new char[256];
                    int charsRead;
                    while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                        String output = new String(buffer, 0, charsRead);
                        Platform.runLater(() -> appendOutput("[ERRO] " + output));
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        Platform.runLater(() -> appendOutput("\n[Erro ao ler stderr: " + e.getMessage() + "]\n"));
                    }
                }
            });
            errorReaderThread.setDaemon(true);
            errorReaderThread.start();

            // Thread para aguardar término do processo
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    isRunning = false;
                    Platform.runLater(() -> {
                        appendOutput("\n========================================\n");
                        appendOutput("Programa terminado com código: " + exitCode + "\n");
                        appendOutput("========================================\n");
                        inputField.setDisable(true);
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> appendOutput("\n[Processo interrompido]\n"));
                }
            }).start();

        } catch (IOException e) {
            appendOutput("\n========================================\n");
            appendOutput("[ERRO AO INICIAR PROCESSO]\n");
            appendOutput("Mensagem: " + e.getMessage() + "\n");
            appendOutput("Classe: " + className + "\n");
            appendOutput("Diretório: " + workingDirectory.getAbsolutePath() + "\n");
            appendOutput("\nVerifique se:\n");
            appendOutput("1. A classe foi compilada corretamente\n");
            appendOutput("2. O nome da classe está correto (com package)\n");
            appendOutput("3. O ficheiro .class existe no diretório\n");
            appendOutput("========================================\n");
            isRunning = false;
            e.printStackTrace();
        }
    }

    /**
     * Envia input do utilizador para o processo.
     */
    private void handleInput() {
        if (!isRunning || processInput == null) {
            return;
        }

        String input = inputField.getText();
        inputField.clear();

        // Mostrar input no terminal
        appendOutput(input + "\n");

        // Enviar para o processo
        processInput.println(input);
    }

    /**
     * Adiciona texto ao terminal.
     *
     * @param text Texto a adicionar
     */
    private void appendOutput(String text) {
        outputArea.appendText(text);
        // Auto-scroll para o final
        outputArea.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Limpa o terminal.
     */
    public void clear() {
        outputArea.clear();
    }

    /**
     * Para o processo em execução.
     */
    public void stopProcess() {
        if (process != null && process.isAlive()) {
            process.destroy();
            appendOutput("\n[Processo terminado pelo utilizador]\n");
            isRunning = false;
        }
    }

    /**
     * Verifica se há processo em execução.
     *
     * @return true se está a executar
     */
    public boolean isRunning() {
        return isRunning;
    }
}
