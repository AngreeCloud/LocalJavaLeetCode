package org.example.judge.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.judge.core.CodeExecutor;
import org.example.judge.core.FileManager;
import org.example.judge.core.OutputComparator;
import org.example.judge.core.SuggestionEngine;
import org.example.judge.model.ComparisonResult;
import org.example.judge.model.ExecutionResult;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.util.Duration;

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
    private SplitPane verticalSplitPane;
    private TextArea statementPreview;
    private TabManager tabManager;
    private TreeView<String> fileTreeView;
    private VBox leftPanel;
    private List<IntegratedTerminal> terminals;
    private TabPane terminalTabPane;
    private int terminalCounter;

    // Botões
    private Button uploadStatementBtn;
    private Button uploadSolutionBtn;
    private Button executeBtn;
    private Button suggestionBtn;
    private Button errorCountBtn;
    private Label liveValidationLabel;
    private Popup problemsPopup;
    private TextArea problemsArea;

    // Componentes de negócio
    private FileManager fileManager;
    private CodeExecutor codeExecutor;
    private OutputComparator outputComparator;
    private SuggestionEngine suggestionEngine;
    private org.example.judge.util.CodeAnalyzer codeAnalyzer;
    private PauseTransition validationDebounce;
    private ExecutorService validationExecutor;
    private volatile long validationSequence;
    private List<CompilerDiagnostic> latestDiagnostics;
    private String latestCompilerErrorsRaw;
    private boolean latestCompilationSuccess;
    private int currentActiveErrorCount;
    private boolean problemsExpanded;

    private static final String VALIDATION_INFO_STYLE = "comparison-info";
    private static final String VALIDATION_OK_STYLE = "comparison-success";
    private static final String VALIDATION_ERROR_STYLE = "comparison-error";
    private static final Pattern COMPILER_HEADER_PATTERN =
        Pattern.compile("^(.*\\\\.java):(\\\\d+):\\\\s*(error|warning):\\\\s*(.*)$");
    private static final int POPUP_WIDTH = 700;
    private static final int POPUP_HEIGHT = 240;

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
        this.codeAnalyzer = new org.example.judge.util.CodeAnalyzer();
        this.terminals = new ArrayList<>();
        this.terminalCounter = 0;
        this.latestDiagnostics = new ArrayList<>();
        this.latestCompilerErrorsRaw = "";
        this.latestCompilationSuccess = true;
        this.currentActiveErrorCount = 0;
        this.problemsExpanded = false;

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
        root.setCenter(verticalSplitPane);
        initializeLiveValidation();
    }

    /**
     * Inicializa a validação em tempo real dos editores.
     */
    private void initializeLiveValidation() {
        validationDebounce = new PauseTransition(Duration.millis(700));
        validationExecutor = Executors.newSingleThreadExecutor();

        validationDebounce.setOnFinished(e -> runLiveValidationAsync());
        tabManager.setEditorChangeListener((fileName, content) -> scheduleLiveValidation());
        tabManager.setActiveFileChangeListener(fileName -> refreshProblemsForActiveFile());

        refreshProblemsForActiveFile();
    }

    /**
     * Cria popup sobreposto para mostrar problemas sem empurrar a UI.
     */
    private void initializeProblemsPopup() {
        problemsArea = new TextArea();
        problemsArea.setEditable(false);
        problemsArea.setWrapText(true);
        problemsArea.setPrefWidth(POPUP_WIDTH - 24);
        problemsArea.setPrefHeight(POPUP_HEIGHT - 48);
        problemsArea.setPromptText("Detalhes dos problemas do ficheiro ativo aparecerão aqui...");
        problemsArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");

        Button closeBtn = new Button("Fechar");
        closeBtn.setOnAction(e -> {
            problemsPopup.hide();
            problemsExpanded = false;
            updateErrorCountButtonText();
        });

        HBox header = new HBox(closeBtn);
        header.setStyle("-fx-alignment: center-right;");

        VBox popupContent = new VBox(6, header, problemsArea);
        popupContent.setPrefSize(POPUP_WIDTH, POPUP_HEIGHT);
        popupContent.setStyle(
            "-fx-background-color: #1f2937;" +
            "-fx-border-color: #60a5fa;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 8;"
        );

        problemsPopup = new Popup();
        problemsPopup.setAutoHide(true);
        problemsPopup.setAutoFix(true);
        problemsPopup.getContent().add(popupContent);
        problemsPopup.setOnHidden(e -> {
            problemsExpanded = false;
            updateErrorCountButtonText();
        });
    }

    /**
     * Posiciona popup de problemas junto ao botão de erros.
     */
    private void showProblemsPopup() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return;
        }

        var anchor = errorCountBtn.localToScreen(0, errorCountBtn.getHeight() + 6);
        if (anchor == null) {
            return;
        }

        problemsPopup.show(root.getScene().getWindow(), anchor.getX() - 140, anchor.getY());
    }

    /**
     * Configura o SplitPane principal (enunciado | workspace).
     */
    private void setupSplitPane() {
        // Painel esquerdo: Enunciado + File Tree
        leftPanel = new VBox(10);
        leftPanel.setStyle("-fx-padding: 10;");
        
        statementPreview = new TextArea();
        statementPreview.setEditable(false);
        statementPreview.setWrapText(true);
        statementPreview.setPromptText("Enunciado aparecerá aqui...");
        statementPreview.setPrefHeight(400);
        
        // TreeView para ficheiros de solução
        Label treeLabel = new Label("📁 Ficheiros de Solução:");
        treeLabel.setStyle("-fx-font-weight: bold;");
        
        TreeItem<String> rootItem = new TreeItem<>("Solução");
        rootItem.setExpanded(true);
        fileTreeView = new TreeView<>(rootItem);
        fileTreeView.setShowRoot(false);
        fileTreeView.setPrefHeight(200);
        
        leftPanel.getChildren().addAll(statementPreview, treeLabel, fileTreeView);
        
        // Painel direito: Tabs de código
        tabManager = new TabManager();
        TabPane tabPane = tabManager.getTabPane();
        
        mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(leftPanel, tabPane);
        mainSplitPane.setDividerPositions(0.3);
        
        // Painel de terminais (parte inferior, estilo VS Code)
        terminalTabPane = new TabPane();
        terminalTabPane.setStyle("-fx-background-color: #1e1e1e;");
        terminalTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        
        // SplitPane vertical: workspace em cima, terminais em baixo
        verticalSplitPane = new SplitPane();
        verticalSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalSplitPane.getItems().addAll(mainSplitPane, terminalTabPane);
        verticalSplitPane.setDividerPositions(0.65);
    }

    /**
     * Cria um novo terminal e adiciona ao TabPane.
     *
     * @return O terminal criado
     */
    private IntegratedTerminal createNewTerminal() {
        terminalCounter++;
        
        IntegratedTerminal terminal = new IntegratedTerminal();
        terminals.add(terminal);
        
        javafx.scene.control.Tab tab = new javafx.scene.control.Tab("🖥️ Terminal " + terminalCounter);
        tab.setContent(terminal);
        
        // Quando fechar tab, remover da lista
        tab.setOnClosed(e -> terminals.remove(terminal));
        
        terminalTabPane.getTabs().add(tab);
        terminalTabPane.getSelectionModel().select(tab);
        
        return terminal;
    }

    /**
     * Obtém um terminal disponível (livre) ou cria um novo.
     *
     * @return Terminal disponível
     */
    private IntegratedTerminal getAvailableTerminal() {
        // Procurar terminal livre (não em execução)
        for (IntegratedTerminal terminal : terminals) {
            if (!terminal.isRunning()) {
                // Selecionar a tab deste terminal
                for (javafx.scene.control.Tab tab : terminalTabPane.getTabs()) {
                    if (tab.getContent() == terminal) {
                        terminalTabPane.getSelectionModel().select(tab);
                        break;
                    }
                }
                return terminal;
            }
        }
        
        // Todos ocupados ou nenhum terminal existe - criar novo
        return createNewTerminal();
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

        errorCountBtn = new Button("Erros: 0 ▶");
        errorCountBtn.setOnAction(e -> toggleProblemsPanel());
        errorCountBtn.setDisable(true);
        initializeProblemsPopup();

        liveValidationLabel = new Label("Validação: a preparar...");
        liveValidationLabel.getStyleClass().add(VALIDATION_INFO_STYLE);
        
        return new ToolBar(uploadStatementBtn, uploadSolutionBtn, executeBtn, 
              new Separator(), suggestionBtn, new Separator(), errorCountBtn, liveValidationLabel);
    }

    /**
     * Handler para upload de enunciado.
     */
    private void handleUploadStatement() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Enunciado");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Todos os Formatos Suportados", "*.txt", "*.pdf", "*.docx"),
            new FileChooser.ExtensionFilter("Ficheiros de Texto", "*.txt"),
            new FileChooser.ExtensionFilter("Ficheiros PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Ficheiros Word", "*.docx")
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
                e.printStackTrace();
            }
        }
    }

    /**
     * Handler para upload de solução oficial (múltiplos ficheiros).
     */
    private void handleUploadSolution() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Ficheiros de Solução");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Ficheiros Java", "*.java"),
            new FileChooser.ExtensionFilter("Todos os Formatos", "*.txt", "*.java")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(root.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            try {
                // Guardar ficheiros
                fileManager.saveSolutionFiles(files);
                
                // Obter lista de nomes
                List<String> fileNames = fileManager.getSolutionFileNames();
                
                // Criar tabs VAZIAS para cada ficheiro (utilizador escreve manualmente)
                tabManager.createSolutionFileTabs(fileNames);
                
                // NÃO carregar conteúdo - tabs devem começar vazias para estudo
                // Os ficheiros ficam guardados como referência para sugestões
                
                // Atualizar TreeView
                updateFileTree(fileNames);

                latestCompilationSuccess = true;
                latestDiagnostics.clear();
                latestCompilerErrorsRaw = "";
                refreshProblemsForActiveFile();
                
                showSuccess("Ficheiros de solução carregados: " + files.size());
            } catch (Exception e) {
                showError("Erro ao carregar solução: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Atualiza a TreeView com os ficheiros de solução.
     *
     * @param fileNames Lista de nomes de ficheiros
     */
    private void updateFileTree(List<String> fileNames) {
        TreeItem<String> rootItem = new TreeItem<>("📁 Solução");
        rootItem.setExpanded(true);
        
        for (String fileName : fileNames) {
            TreeItem<String> fileItem = new TreeItem<>("📄 " + fileName);
            rootItem.getChildren().add(fileItem);
        }
        
        fileTreeView.setRoot(rootItem);
        fileTreeView.setShowRoot(true);
    }

    /**
     * Handler para executar código do utilizador.
     */
    private void handleExecute() {
        try {
            // Verificar se há ficheiros de solução carregados (multi-file)
            if (fileManager.hasSolutionFiles()) {
                handleExecuteMultiFile();
            } else {
                // Fallback para código single-file
                handleExecuteSingleFile();
            }
        } catch (Exception e) {
            showError("Erro durante execução: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executa código single-file (antigo formato).
     */
    private void handleExecuteSingleFile() throws Exception {
        String userCode = tabManager.getUserCode();
        if (userCode.trim().isEmpty()) {
            showError("Código do utilizador está vazio!");
            return;
        }
        
        // Verificar se requer execução interativa
        boolean requiresInteractive = codeAnalyzer.requiresInteractiveExecution(userCode);
        
        fileManager.saveUserCode(userCode);
        
        ExecutionResult compileResult = codeExecutor.compile(fileManager.getUserCodePath());
        if (!compileResult.isSuccess()) {
            showError("Erro de compilação:\n" + compileResult.getErrors());
            return;
        }
        
        // Se requer execução interativa, executar em terminal
        if (requiresInteractive) {
            // Para single-file, executar UserCode no tmpDir
            executeSingleFileInteractive();
            return;
        }
        
        ExecutionResult execResult = codeExecutor.execute(fileManager.getUserCodeClassName());
        if (!execResult.isSuccess()) {
            showError("Erro de execução:\\n" + execResult.getErrors());
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
    }

    /**
     * Executa código single-file em modo interativo (terminal integrado).
     */
    private void executeSingleFileInteractive() {
        try {
            // Obter terminal disponível ou criar novo
            IntegratedTerminal terminal = getAvailableTerminal();
            terminal.clear();
            
            // Executar programa no terminal
            String className = fileManager.getUserCodeClassName();
            File tmpDir = fileManager.getTmpDir().toFile();
            terminal.executeProgram(className, tmpDir);
            
        } catch (Exception e) {
            showError("Erro ao abrir terminal interativo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executa código multi-file (novo formato para OOP).
     */
    private void handleExecuteMultiFile() throws Exception {
        // Obter todos os códigos editados e guardar
        Map<String, String> editedContents = tabManager.getAllSolutionFileContents();
        
        // Verificar se algum código requer execução interativa
        boolean requiresInteractive = false;
        for (String content : editedContents.values()) {
            if (codeAnalyzer.requiresInteractiveExecution(content)) {
                requiresInteractive = true;
                break;
            }
        }
        
        // Guardar todos os ficheiros editados no diretório de solução
        for (Map.Entry<String, String> entry : editedContents.entrySet()) {
            String fileName = entry.getKey();
            String content = entry.getValue();
            fileManager.saveSolutionFileContent(fileName, content);
        }
        
        // Compilar todos os ficheiros
        List<Path> solutionPaths = fileManager.getSolutionFilePaths();
        ExecutionResult compileResult = codeExecutor.compileMultiple(solutionPaths);
        
        if (!compileResult.isSuccess()) {
            showError("Erro de compilação:\\n" + compileResult.getErrors());
            return;
        }
        
        // Obter o nome do ficheiro da tab ativa (ficheiro que o utilizador quer executar)
        String activeFileName = tabManager.getActiveFileName();
        if (activeFileName == null) {
            showError("Nenhum ficheiro está selecionado. Clique numa tab de código para executar.");
            return;
        }
        
        // Obter o nome completo da classe do ficheiro ativo
        String mainClassName = fileManager.getClassNameFromFile(activeFileName);
        if (mainClassName == null) {
            showError("Erro ao determinar classe do ficheiro: " + activeFileName);
            return;
        }
        
        // Se requer execução interativa (input, ficheiros, sockets, etc.), executar em terminal
        if (requiresInteractive) {
            executeInteractive(mainClassName);
            return;
        }
        
        // Executar classe main usando solutionDir como classpath (modo automático)
        ExecutionResult execResult = codeExecutor.execute(mainClassName, fileManager.getSolutionDir());
        if (!execResult.isSuccess()) {
            showError("Erro de execução:\\n" + execResult.getErrors());
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
    }

    /**
     * Executa código em modo interativo (terminal integrado).
     *
     * @param mainClassName Nome completo da classe com main
     */
    private void executeInteractive(String mainClassName) {
        try {
            // Obter terminal disponível ou criar novo
            IntegratedTerminal terminal = getAvailableTerminal();
            terminal.clear();
            
            // Executar programa no terminal
            File solutionDir = fileManager.getSolutionDir().toFile();
            terminal.executeProgram(mainClassName, solutionDir);
            
        } catch (Exception e) {
            showError("Erro ao abrir terminal interativo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler para botão de sugestão (💡).
     */
    private void handleSuggestion() {
        try {
            String userCode = tabManager.getUserCode();
            
            // Se editor vazio, usar código vazio para começar do início
            if (userCode == null) {
                userCode = "";
            }
            
            int cursorLine = tabManager.getCurrentCursorLine();
            
            // Verificar se há ficheiros de solução carregados
            String officialSolution;
            if (fileManager.hasSolutionFiles()) {
                // Multi-file: ler ficheiro ativo
                String activeFileName = tabManager.getActiveFileName();
                if (activeFileName == null) {
                    showError("Nenhum ficheiro de solução ativo!");
                    return;
                }
                officialSolution = fileManager.readSolutionFile(activeFileName);
            } else {
                // Single-file: ler solução antiga
                officialSolution = fileManager.readSolution();
            }
            
            if (officialSolution == null || officialSolution.isEmpty()) {
                showError("Carregue primeiro a solução oficial!");
                return;
            }
            
            // Se editor está vazio, sugerir primeira linha
            String suggestion;
            if (userCode.trim().isEmpty()) {
                String[] lines = officialSolution.split("\n");
                suggestion = lines.length > 0 ? lines[0] : null;
            } else {
                suggestion = suggestionEngine.suggestNextLine(userCode, cursorLine, officialSolution);
            }
            
            if (suggestion == null || suggestion.trim().isEmpty()) {
                showError("Não foi possível encontrar uma sugestão apropriada.");
                return;
            }
            
            SuggestionPopup popup = new SuggestionPopup();
            popup.show(suggestion, root.getScene().getWindow(), 300, 300, () -> {
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
     * Liberta recursos de background da validação em tempo real.
     */
    public void shutdown() {
        if (validationDebounce != null) {
            validationDebounce.stop();
        }
        if (validationExecutor != null) {
            validationExecutor.shutdownNow();
        }
    }

    /**
     * Agenda validação após curto intervalo (debounce).
     */
    private void scheduleLiveValidation() {
        if (!fileManager.hasSolutionFiles()) {
            latestCompilationSuccess = true;
            latestDiagnostics.clear();
            latestCompilerErrorsRaw = "";
            refreshProblemsForActiveFile();
            return;
        }

        updateValidationStatus("Validação de sintaxe: a verificar...", VALIDATION_INFO_STYLE, null);
        validationDebounce.playFromStart();
    }

    /**
     * Executa validação em thread separada para não bloquear UI.
     */
    private void runLiveValidationAsync() {
        Map<String, String> editedContents = tabManager.getAllSolutionFileContents();
        if (editedContents.isEmpty()) {
            latestCompilationSuccess = true;
            latestDiagnostics.clear();
            latestCompilerErrorsRaw = "";
            refreshProblemsForActiveFile();
            return;
        }

        final long requestId = ++validationSequence;

        validationExecutor.submit(() -> {
            try {
                for (Map.Entry<String, String> entry : editedContents.entrySet()) {
                    fileManager.saveSolutionFileContent(entry.getKey(), entry.getValue());
                }

                ExecutionResult compileResult = codeExecutor.compileMultiple(fileManager.getSolutionFilePaths());
                if (requestId != validationSequence) {
                    return;
                }

                Platform.runLater(() -> {
                    if (requestId != validationSequence) {
                        return;
                    }

                    List<CompilerDiagnostic> compilerDiagnostics = parseCompilerDiagnostics(compileResult.getErrors());
                    List<CompilerDiagnostic> braceDiagnostics = detectBraceDiagnostics(editedContents);

                    if (compileResult.isSuccess()) {
                        latestCompilationSuccess = braceDiagnostics.isEmpty();
                        latestDiagnostics = sortDiagnostics(braceDiagnostics);
                        latestCompilerErrorsRaw = "";
                    } else {
                        latestCompilationSuccess = false;
                        latestDiagnostics = sortDiagnostics(mergeDiagnostics(compilerDiagnostics, braceDiagnostics));
                        if (latestDiagnostics.isEmpty()) {
                            latestDiagnostics = createFallbackDiagnostics(compileResult.getErrors());
                        }
                        latestCompilerErrorsRaw = compileResult.getErrors();
                    }

                    refreshProblemsForActiveFile();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    latestCompilationSuccess = false;
                    latestDiagnostics.clear();
                    latestCompilerErrorsRaw = e.getMessage();
                    refreshProblemsForActiveFile();
                    updateValidationStatus(
                        "Validação: erro ao analisar código",
                        VALIDATION_ERROR_STYLE,
                        e.getMessage()
                    );
                });
            }
        });
    }

    /**
     * Mostra/oculta painel de problemas abaixo da toolbar.
     */
    private void toggleProblemsPanel() {
        problemsExpanded = !problemsExpanded;
        if (problemsExpanded) {
            showProblemsPopup();
        } else {
            problemsPopup.hide();
        }
        updateErrorCountButtonText();
    }

    /**
     * Atualiza contador e detalhes dos erros do ficheiro ativo.
     */
    private void refreshProblemsForActiveFile() {
        if (!fileManager.hasSolutionFiles()) {
            currentActiveErrorCount = 0;
            errorCountBtn.setDisable(true);
            updateErrorCountButtonText();
            problemsArea.setText("Carregue ficheiros de solução para ativar diagnóstico em tempo real.");
            updateValidationStatus("Validação de sintaxe: carregue ficheiros para iniciar", VALIDATION_INFO_STYLE, null);
            return;
        }

        String activeFileName = tabManager.getActiveFileName();
        if (activeFileName == null || activeFileName.isBlank()) {
            currentActiveErrorCount = 0;
            errorCountBtn.setDisable(true);
            updateErrorCountButtonText();
            problemsArea.setText("Selecione uma tab de código para ver os problemas desse ficheiro.");
            updateValidationStatus("Validação de sintaxe: selecione um ficheiro de código", VALIDATION_INFO_STYLE, null);
            return;
        }

        errorCountBtn.setDisable(false);

        List<CompilerDiagnostic> activeDiagnostics = filterDiagnosticsForFile(latestDiagnostics, activeFileName);
        int totalErrorCount = countErrors(latestDiagnostics);
        currentActiveErrorCount = countErrors(activeDiagnostics);
        updateErrorCountButtonText();

        String details = buildProblemsPanelContent(activeFileName, activeDiagnostics, totalErrorCount);
        problemsArea.setText(details);

        if (latestCompilationSuccess) {
            updateValidationStatus("Validação de sintaxe: sem erros", VALIDATION_OK_STYLE, null);
            return;
        }

        if (currentActiveErrorCount > 0) {
            updateValidationStatus(
                "Validação de sintaxe: " + currentActiveErrorCount + " erro(s) em " + activeFileName,
                VALIDATION_ERROR_STYLE,
                buildDiagnosticsTooltip(activeDiagnostics, latestCompilerErrorsRaw)
            );
            return;
        }

        if (totalErrorCount > 0) {
            updateValidationStatus(
                "Validação de sintaxe: 0 erros em " + activeFileName + " (há erros noutros ficheiros)",
                VALIDATION_INFO_STYLE,
                buildDiagnosticsTooltip(latestDiagnostics, latestCompilerErrorsRaw)
            );
            return;
        }

        updateValidationStatus(
            "Validação de sintaxe: problema detetado",
            VALIDATION_ERROR_STYLE,
            formatFallbackCompilationPreview(latestCompilerErrorsRaw)
        );
    }

    /**
     * Atualiza texto do botão de erros com contador e estado expandido.
     */
    private void updateErrorCountButtonText() {
        String arrow = problemsExpanded ? "▼" : "▶";
        errorCountBtn.setText("Erros: " + currentActiveErrorCount + " " + arrow);
    }

    /**
     * Filtra diagnósticos para um ficheiro específico.
     */
    private List<CompilerDiagnostic> filterDiagnosticsForFile(List<CompilerDiagnostic> diagnostics, String fileName) {
        List<CompilerDiagnostic> filtered = new ArrayList<>();
        if (diagnostics == null || fileName == null) {
            return filtered;
        }

        for (CompilerDiagnostic diagnostic : diagnostics) {
            if (fileName.equalsIgnoreCase(diagnostic.fileName)) {
                filtered.add(diagnostic);
            }
        }

        return filtered;
    }

    /**
     * Conta diagnósticos com severidade de erro.
     */
    private int countErrors(List<CompilerDiagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (CompilerDiagnostic diagnostic : diagnostics) {
            if ("error".equalsIgnoreCase(diagnostic.severity)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gera texto detalhado do painel de problemas para o ficheiro ativo.
     */
    private String buildProblemsPanelContent(String activeFileName,
                                             List<CompilerDiagnostic> activeDiagnostics,
                                             int totalErrorCount) {
        if (latestCompilationSuccess) {
            return "Sem erros no ficheiro ativo: " + activeFileName;
        }

        if (activeDiagnostics == null || activeDiagnostics.isEmpty()) {
            if (totalErrorCount > 0) {
                return "Sem erros em " + activeFileName +
                    "\nExistem " + totalErrorCount + " erro(s) noutros ficheiros.";
            }
            return formatFallbackCompilationPreview(latestCompilerErrorsRaw);
        }

        StringBuilder content = new StringBuilder();
        content.append("Problemas em ").append(activeFileName)
            .append(" (")
            .append(countErrors(activeDiagnostics))
            .append(" erro(s))")
            .append("\n\n");

        int index = 1;
        for (CompilerDiagnostic diagnostic : activeDiagnostics) {
            content.append(index++)
                .append(") Linha ")
                .append(diagnostic.line > 0 ? diagnostic.line : "?");

            if (diagnostic.column > 0) {
                content.append(", Coluna ").append(diagnostic.column);
            }

            content.append(" [")
                .append(diagnostic.severity.toUpperCase())
                .append("]\n")
                .append("   ")
                .append(diagnostic.message)
                .append("\n");

            if (diagnostic.sourceLine != null && !diagnostic.sourceLine.isBlank()) {
                content.append("   ")
                    .append(diagnostic.sourceLine.trim())
                    .append("\n");
            }

            if (diagnostic.pointerLine != null && !diagnostic.pointerLine.isBlank()) {
                content.append("   ")
                    .append(diagnostic.pointerLine)
                    .append("\n");
            }

            content.append("\n");
        }

        return content.toString().trim();
    }

    /**
     * Atualiza o estado visual da validação contínua.
     */
    private void updateValidationStatus(String text, String styleClass, String tooltipText) {
        liveValidationLabel.setText(text);
        liveValidationLabel.getStyleClass().removeAll(
            VALIDATION_INFO_STYLE,
            VALIDATION_OK_STYLE,
            VALIDATION_ERROR_STYLE
        );
        liveValidationLabel.getStyleClass().add(styleClass);

        if (tooltipText == null || tooltipText.isBlank()) {
            liveValidationLabel.setTooltip(null);
        } else {
            liveValidationLabel.setTooltip(new Tooltip(tooltipText));
        }
    }

    /**
     * Cria conteúdo detalhado para tooltip com diagnósticos parseados.
     */
    private String buildDiagnosticsTooltip(List<CompilerDiagnostic> diagnostics, String fallbackErrors) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return formatFallbackCompilationPreview(fallbackErrors);
        }

        StringBuilder tooltip = new StringBuilder();
        int maxItems = Math.min(diagnostics.size(), 10);

        for (int i = 0; i < maxItems; i++) {
            CompilerDiagnostic diagnostic = diagnostics.get(i);
            tooltip.append(i + 1)
                .append(") ")
                .append(diagnostic.fileName)
                .append(":")
                .append(diagnostic.line);

            if (diagnostic.column > 0) {
                tooltip.append(":").append(diagnostic.column);
            }

            tooltip.append(" [")
                .append(diagnostic.severity.toUpperCase())
                .append("] ")
                .append(diagnostic.message)
                .append("\n");

            if (diagnostic.sourceLine != null && !diagnostic.sourceLine.isBlank()) {
                tooltip.append("    ")
                    .append(diagnostic.sourceLine.trim())
                    .append("\n");
            }

            if (diagnostic.pointerLine != null && !diagnostic.pointerLine.isBlank()) {
                tooltip.append("    ")
                    .append(diagnostic.pointerLine)
                    .append("\n");
            }

            tooltip.append("\n");
        }

        if (diagnostics.size() > maxItems) {
            tooltip.append("... e mais ")
                .append(diagnostics.size() - maxItems)
                .append(" diagnóstico(s).");
        }

        return tooltip.toString().trim();
    }

    /**
     * Parseia output do javac em diagnósticos estruturados.
     */
    private List<CompilerDiagnostic> parseCompilerDiagnostics(String errors) {
        List<CompilerDiagnostic> diagnostics = new ArrayList<>();
        if (errors == null || errors.isBlank()) {
            return diagnostics;
        }

        String[] lines = errors.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = COMPILER_HEADER_PATTERN.matcher(line.trim());

            if (!matcher.matches()) {
                continue;
            }

            String fullPath = matcher.group(1);
            int lineNumber;
            try {
                lineNumber = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException ex) {
                lineNumber = -1;
            }

            String severity = matcher.group(3).trim();
            String message = matcher.group(4).trim();

            String sourceLine = null;
            String pointerLine = null;
            int column = -1;

            if (i + 1 < lines.length) {
                String possibleSource = lines[i + 1];
                if (!possibleSource.trim().isEmpty()) {
                    sourceLine = possibleSource;
                }
            }

            if (i + 2 < lines.length) {
                String possiblePointer = lines[i + 2];
                if (possiblePointer.contains("^")) {
                    pointerLine = possiblePointer;
                    column = possiblePointer.indexOf('^') + 1;
                }
            }

            diagnostics.add(new CompilerDiagnostic(
                extractFileName(fullPath),
                lineNumber,
                column,
                severity,
                message,
                sourceLine,
                pointerLine
            ));
        }

        return diagnostics;
    }

    /**
     * Junta diagnósticos compilador + diagnósticos estruturais (chavetas).
     */
    private List<CompilerDiagnostic> mergeDiagnostics(List<CompilerDiagnostic> compilerDiagnostics,
                                                      List<CompilerDiagnostic> braceDiagnostics) {
        List<CompilerDiagnostic> merged = new ArrayList<>();
        if (compilerDiagnostics != null) {
            merged.addAll(compilerDiagnostics);
        }
        if (braceDiagnostics != null) {
            merged.addAll(braceDiagnostics);
        }
        return merged;
    }

    /**
     * Ordena diagnósticos para facilitar leitura (ficheiro -> linha -> coluna).
     */
    private List<CompilerDiagnostic> sortDiagnostics(List<CompilerDiagnostic> diagnostics) {
        List<CompilerDiagnostic> sorted = new ArrayList<>(diagnostics);
        sorted.sort(Comparator
            .comparing((CompilerDiagnostic d) -> d.fileName == null ? "" : d.fileName)
            .thenComparingInt(d -> d.line < 0 ? Integer.MAX_VALUE : d.line)
            .thenComparingInt(d -> d.column < 0 ? Integer.MAX_VALUE : d.column));
        return sorted;
    }

    /**
     * Cria diagnóstico de fallback quando o parser do javac não identifica localização.
     */
    private List<CompilerDiagnostic> createFallbackDiagnostics(String rawErrors) {
        List<CompilerDiagnostic> fallback = new ArrayList<>();
        String activeFileName = tabManager.getActiveFileName();
        String message = (rawErrors == null || rawErrors.isBlank())
            ? "Erro de sintaxe detetado"
            : firstMeaningfulLine(rawErrors);

        fallback.add(new CompilerDiagnostic(
            activeFileName == null ? "FicheiroAtivo" : activeFileName,
            -1,
            -1,
            "error",
            message,
            null,
            null
        ));

        return fallback;
    }

    /**
     * Obtém primeira linha útil de mensagem de erro.
     */
    private String firstMeaningfulLine(String text) {
        String[] lines = text.split("\\R");
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
        }
        return "Erro de sintaxe detetado";
    }

    /**
     * Deteção heurística de problemas de chavetas (abertura/fecho) por ficheiro.
     */
    private List<CompilerDiagnostic> detectBraceDiagnostics(Map<String, String> editedContents) {
        List<CompilerDiagnostic> diagnostics = new ArrayList<>();
        if (editedContents == null || editedContents.isEmpty()) {
            return diagnostics;
        }

        for (Map.Entry<String, String> entry : editedContents.entrySet()) {
            diagnostics.addAll(detectBraceDiagnosticsForFile(entry.getKey(), entry.getValue()));
        }

        return diagnostics;
    }

    /**
     * Deteção de chavetas de um ficheiro individual.
     */
    private List<CompilerDiagnostic> detectBraceDiagnosticsForFile(String fileName, String code) {
        List<CompilerDiagnostic> diagnostics = new ArrayList<>();
        if (code == null || code.isEmpty()) {
            return diagnostics;
        }

        Stack<BracePosition> stack = new Stack<>();
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;
        boolean escaping = false;

        int line = 1;
        int column = 0;
        String[] codeLines = code.split("\\R", -1);

        for (int i = 0; i < code.length(); i++) {
            char current = code.charAt(i);
            char next = i + 1 < code.length() ? code.charAt(i + 1) : '\0';
            column++;

            if (current == '\n') {
                line++;
                column = 0;
                inLineComment = false;
                continue;
            }

            if (inLineComment) {
                continue;
            }

            if (inBlockComment) {
                if (current == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                    column++;
                }
                continue;
            }

            if (inString) {
                if (!escaping && current == '"') {
                    inString = false;
                }
                escaping = (!escaping && current == '\\');
                continue;
            }

            if (inChar) {
                if (!escaping && current == '\'') {
                    inChar = false;
                }
                escaping = (!escaping && current == '\\');
                continue;
            }

            escaping = false;

            if (current == '/' && next == '/') {
                inLineComment = true;
                i++;
                column++;
                continue;
            }

            if (current == '/' && next == '*') {
                inBlockComment = true;
                i++;
                column++;
                continue;
            }

            if (current == '"') {
                inString = true;
                continue;
            }

            if (current == '\'') {
                inChar = true;
                continue;
            }

            if (current == '{') {
                stack.push(new BracePosition(line, column));
            } else if (current == '}') {
                if (stack.isEmpty()) {
                    diagnostics.add(new CompilerDiagnostic(
                        fileName,
                        line,
                        column,
                        "error",
                        "Chaveta de fecho '}' sem abertura correspondente. Possivelmente falta '{' antes deste bloco.",
                        safeLine(codeLines, line),
                        buildPointer(column)
                    ));
                } else {
                    stack.pop();
                }
            }
        }

        while (!stack.isEmpty()) {
            BracePosition open = stack.pop();
            diagnostics.add(new CompilerDiagnostic(
                fileName,
                open.line,
                open.column,
                "error",
                "Chaveta de abertura '{' sem fecho correspondente. Falta '}' no fim do bloco.",
                safeLine(codeLines, open.line),
                buildPointer(open.column)
            ));
        }

        return diagnostics;
    }

    private String safeLine(String[] codeLines, int lineNumber) {
        if (lineNumber <= 0 || codeLines == null || lineNumber > codeLines.length) {
            return null;
        }
        return codeLines[lineNumber - 1];
    }

    private String buildPointer(int column) {
        if (column <= 0) {
            return null;
        }
        StringBuilder pointer = new StringBuilder();
        for (int i = 1; i < column; i++) {
            pointer.append(' ');
        }
        pointer.append('^');
        return pointer.toString();
    }

    /**
     * Extrai nome de ficheiro de um caminho completo.
     */
    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "Desconhecido";
        }

        Path path = Path.of(filePath);
        Path fileName = path.getFileName();
        return fileName == null ? filePath : fileName.toString();
    }

    /**
     * Fallback para quando não for possível parsear diagnósticos.
     */
    private String formatFallbackCompilationPreview(String errors) {
        if (errors == null || errors.isBlank()) {
            return "Erro de compilação sem detalhes.";
        }

        String[] lines = errors.split("\\R");
        StringBuilder preview = new StringBuilder();
        int count = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            preview.append(trimmed).append("\n");
            count++;

            if (count >= 8) {
                preview.append("...\n");
                break;
            }
        }

        if (preview.length() == 0) {
            return "Erro de compilação sem detalhes.";
        }

        return preview.toString().trim();
    }

    private static class CompilerDiagnostic {
        private final String fileName;
        private final int line;
        private final int column;
        private final String severity;
        private final String message;
        private final String sourceLine;
        private final String pointerLine;

        private CompilerDiagnostic(String fileName,
                                   int line,
                                   int column,
                                   String severity,
                                   String message,
                                   String sourceLine,
                                   String pointerLine) {
            this.fileName = fileName;
            this.line = line;
            this.column = column;
            this.severity = severity;
            this.message = message;
            this.sourceLine = sourceLine;
            this.pointerLine = pointerLine;
        }
    }

    private static class BracePosition {
        private final int line;
        private final int column;

        private BracePosition(int line, int column) {
            this.line = line;
            this.column = column;
        }
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

