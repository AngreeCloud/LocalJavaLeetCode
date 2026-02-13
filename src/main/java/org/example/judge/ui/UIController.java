package org.example.judge.ui;

import java.io.File;

import org.example.judge.core.CodeExecutor;
import org.example.judge.core.FileManager;
import org.example.judge.core.OutputComparator;
import org.example.judge.core.SuggestionEngine;
import org.example.judge.model.ComparisonResult;
import org.example.judge.model.ExecutionResult;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

/**
 * UIController - Controlador principal da interface gráfica.
 *
 * Responsabilidades:
 * - Criar e gerir todos os componentes visuais
 * - Configurar SplitPane (enunciado | área de trabalho)
 * - Gerir eventos de botões (upload, executar, sugestão)
 * - Coordenar interação entre componentes (FileManager, CodeExecutor, etc.)
 * - Mostrar mensagens e erros ao utilizador
 *
 * Componentes principais:
 * - SplitPane (enunciado + workspace)
 * - TabPane com 4 tabs fixas
 * - Botões de ação
 * - Área de preview do enunciado
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class UIController {

    // Componentes de UI
    private BorderPane root;
    private SplitPane mainSplitPane;
    private TextArea statementPreview;
    private TabManager tabManager;

    // Botões
    private Button uploadStatementBtn;
    private Button uploadSolutionBtn;
    private Button executeBtn;
    private Button suggestionBtn;

    // Componentes de negócio
    private FileManager fileManager;
    private CodeExecutor codeExecutor;
    private OutputComparator outputComparator;
    private SuggestionEngine suggestionEngine;

    /**
     * Construtor - Inicializa o controlador.
     *
     * @param fileManager Gestor de ficheiros
     */
    public UIController(FileManager fileManager) {
        this.fileManager = fileManager;
        this.codeExecutor = new CodeExecutor(fileManager);
        this.outputComparator = new OutputComparator();
        this.suggestionEngine = new SuggestionEngine();

        initialize();
    }

    /**
     * Inicializa todos os componentes da UI.
     */
    private void initialize() {
        root = new BorderPane();
        
        ToolBar toolbar = createToolbar();
        root.setTop(toolbar);
        
        setupSplitPane();
        root.setCenter(mainSplitPane);
    }

    /**
     * Configura o SplitPane principal (enunciado | workspace).
     */
    private void setupSplitPane() {
        statementPreview = new TextArea();
        statementPreview.setEditable(false);
        statementPreview.setWrapText(true);
        statementPreview.setPromptText("Enunciado aparecerá aqui...");
        
        tabManager = new TabManager();
        TabPane tabPane = tabManager.getTabPane();
        
        mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(statementPreview, tabPane);
        mainSplitPane.setDividerPositions(0.3);
    }

    /**
     * Cria a toolbar com botões de ação.
     *
     * @return ToolBar configurada
     */
    private ToolBar createToolbar() {
        uploadStatementBtn = new Button("Upload Enunciado");
        uploadStatementBtn.setOnAction(e -> handleUploadStatement());
        
        uploadSolutionBtn = new Button("Upload Solução");
        uploadSolutionBtn.setOnAction(e -> handleUploadSolution());
        
        executeBtn = new Button("Executar Código");
        executeBtn.setOnAction(e -> handleExecute());
        
        suggestionBtn = new Button("💡 Sugestão");
        suggestionBtn.setOnAction(e -> handleSuggestion());
        
        return new ToolBar(uploadStatementBtn, uploadSolutionBtn, executeBtn, 
                          new Separator(), suggestionBtn);
    }

    /**
     * Handler para upload de enunciado.
     */
    private void handleUploadStatement() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Enunciado");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Ficheiros de Texto", "*.txt")
        );
        
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                String content = fileManager.readStatement(file);
                fileManager.saveStatement(file);
                displayStatement(content);
                showSuccess("Enunciado carregado com sucesso!");
            } catch (Exception e) {
                showError("Erro ao carregar enunciado: " + e.getMessage());
            }
        }
    }

    /**
     * Handler para upload de solução oficial.
     */
    private void handleUploadSolution() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Solução Oficial");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Ficheiros de Texto", "*.txt")
        );
        
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                fileManager.saveSolution(file);
                String content = fileManager.readSolution();
                tabManager.setExpectedOutput(content);
                showSuccess("Solução oficial carregada!");
            } catch (Exception e) {
                showError("Erro ao carregar solução: " + e.getMessage());
            }
        }
    }

    /**
     * Handler para executar código do utilizador.
     */
    private void handleExecute() {
        try {
            String userCode = tabManager.getUserCode();
            if (userCode.trim().isEmpty()) {
                showError("Código do utilizador está vazio!");
                return;
            }
            
            fileManager.saveUserCode(userCode);
            
            ExecutionResult compileResult = codeExecutor.compile(fileManager.getUserCodePath());
            if (!compileResult.isSuccess()) {
                showError("Erro de compilação:\n" + compileResult.getErrors());
                return;
            }
            
            ExecutionResult execResult = codeExecutor.execute(fileManager.getUserCodeClassName());
            if (!execResult.isSuccess()) {
                showError("Erro de execução:\n" + execResult.getErrors());
                return;
            }
            
            String userOutput = execResult.getOutput();
            displayUserOutput(userOutput);
            
            String expectedOutput = fileManager.readSolution();
            ComparisonResult comparison = outputComparator.compare(userOutput, expectedOutput);
            displayComparison(comparison);
            
            if (comparison.isMatch()) {
                showSuccess("Output correto! 🎉");
            } else {
                showError("Output diferente. Verifique a tab de comparação.");
            }
            
        } catch (Exception e) {
            showError("Erro durante execução: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler para botão de sugestão (💡).
     */
    private void handleSuggestion() {
        try {
            String userCode = tabManager.getUserCode();
            int cursorLine = tabManager.getCurrentCursorLine();
            String officialSolution = fileManager.readSolution();
            
            if (officialSolution == null || officialSolution.isEmpty()) {
                showError("Carregue primeiro a solução oficial!");
                return;
            }
            
            String suggestion = suggestionEngine.suggestNextLine(userCode, cursorLine, officialSolution);
            
            if (suggestion == null) {
                showError("Não foi possível encontrar uma sugestão apropriada.");
                return;
            }
            
            SuggestionPopup popup = new SuggestionPopup();
            popup.show(suggestion, 300, 300, () -> {
                tabManager.insertSuggestion(suggestion);
            });
            
        } catch (Exception e) {
            showError("Erro ao gerar sugestão: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mostra o conteúdo do enunciado no preview.
     *
     * @param content Conteúdo do enunciado
     */
    public void displayStatement(String content) {
        statementPreview.setText(content);
    }

    /**
     * Mostra o output do utilizador na tab correspondente.
     *
     * @param output Output capturado
     */
    public void displayUserOutput(String output) {
        tabManager.setUserOutput(output);
    }

    /**
     * Mostra o resultado da comparação na tab correspondente.
     *
     * @param result Resultado da comparação
     */
    public void displayComparison(ComparisonResult result) {
        tabManager.setComparisonResult(result);
    }

    /**
     * Mostra mensagem de erro ao utilizador.
     *
     * @param message Mensagem de erro
     */
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra mensagem de sucesso ao utilizador.
     *
     * @param message Mensagem de sucesso
     */
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Retorna o root node da UI.
     *
     * @return BorderPane root
     */
    public BorderPane getRoot() {
        return root;
    }
}

