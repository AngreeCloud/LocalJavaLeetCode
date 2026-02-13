package org.example.judge.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.judge.core.CodeExecutor;
import org.example.judge.core.FileManager;
import org.example.judge.core.OutputComparator;
import org.example.judge.core.SuggestionEngine;
import org.example.judge.model.ComparisonResult;
import org.example.judge.model.ExecutionResult;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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

    // Componentes de negócio
    private FileManager fileManager;
    private CodeExecutor codeExecutor;
    private OutputComparator outputComparator;
    private SuggestionEngine suggestionEngine;
    private org.example.judge.util.CodeAnalyzer codeAnalyzer;

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
        
        return new ToolBar(uploadStatementBtn, uploadSolutionBtn, executeBtn, 
                          new Separator(), suggestionBtn);
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
     * Retorna o root node da UI.
     *
     * @return BorderPane root
     */
    public BorderPane getRoot() {
        return root;
    }
}

