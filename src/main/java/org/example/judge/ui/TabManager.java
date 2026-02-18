package org.example.judge.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.example.judge.model.ComparisonResult;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

/**
 * TabManager - Gestor das 4 tabs fixas da aplicação.
 *
 * Responsabilidades:
 * - Criar as 4 tabs fixas e não removíveis
 * - Gerir o conteúdo de cada tab
 * - Fornecer acesso ao editor de código
 * - Formatar e mostrar outputs e comparações
 *
 * Tabs:
 * 1. "Código do Utilizador" - Editor editável (TextArea ou CodeArea)
 * 2. "Output Esperado" - Solução oficial (read-only)
 * 3. "Output do Utilizador" - Resultado da execução (read-only)
 * 4. "Resultado da Comparação" - Diff detalhado (read-only)
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class TabManager {

    private TabPane tabPane;

    // Tabs fixas
    private Tab userOutputTab;
    private Tab comparisonTab;

    // Conteúdo das tabs fixas
    private TextArea userOutputArea;
    private TextArea comparisonArea;

    // Tabs dinâmicas para ficheiros de solução (nome -> Tab)
    private Map<String, Tab> solutionFileTabs;
    // Editores para cada ficheiro de solução (nome -> TextArea)
    private Map<String, TextArea> solutionFileEditors;
    // Tab ativa atual
    private String activeFileName;
    private TextArea activeEditor;
    private BiConsumer<String, String> editorChangeListener;
    private Consumer<String> activeFileChangeListener;

    /**
     * Construtor - Cria as tabs fixas.
     */
    public TabManager() {
        solutionFileTabs = new HashMap<>();
        solutionFileEditors = new HashMap<>();
        createFixedTabs();
    }

    /**
     * Cria as tabs fixas (outputs e comparação).
     */
    private void createFixedTabs() {
        tabPane = new TabPane();
        
        userOutputTab = createUserOutputTab();
        comparisonTab = createComparisonTab();
        
        tabPane.getTabs().addAll(userOutputTab, comparisonTab);
        
        // Listener para detectar mudança de tab ativa
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && solutionFileTabs.containsValue(newTab)) {
                // Encontrar o nome do ficheiro correspondente
                for (Map.Entry<String, Tab> entry : solutionFileTabs.entrySet()) {
                    if (entry.getValue() == newTab) {
                        activeFileName = entry.getKey();
                        activeEditor = solutionFileEditors.get(activeFileName);
                        if (activeFileChangeListener != null) {
                            activeFileChangeListener.accept(activeFileName);
                        }
                        break;
                    }
                }
            } else {
                activeFileName = null;
                activeEditor = null;
                if (activeFileChangeListener != null) {
                    activeFileChangeListener.accept(null);
                }
            }
        });
    }

    /**
     * Cria tabs para ficheiros de solução.
     *
     * @param fileNames Lista de nomes de ficheiros
     */
    public void createSolutionFileTabs(java.util.List<String> fileNames) {
        // Remover tabs antigas de solução
        solutionFileTabs.values().forEach(tab -> tabPane.getTabs().remove(tab));
        solutionFileTabs.clear();
        solutionFileEditors.clear();
        
        // Criar tabs para cada ficheiro
        for (String fileName : fileNames) {
            TextArea editor = new TextArea();
            editor.setPromptText("Código de " + fileName + "...");
            editor.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");
            editor.setWrapText(false);
            editor.textProperty().addListener((obs, oldValue, newValue) -> {
                if (editorChangeListener != null) {
                    editorChangeListener.accept(fileName, newValue);
                }
            });
            
            Tab tab = new Tab("📝 " + fileName, editor);
            tab.setClosable(false);
            
            solutionFileTabs.put(fileName, tab);
            solutionFileEditors.put(fileName, editor);
            
            // Adicionar tab antes das tabs fixas
            tabPane.getTabs().add(tabPane.getTabs().size() - 2, tab);
        }
        
        // Ativar primeira tab se houver
        if (!fileNames.isEmpty()) {
            String firstName = fileNames.get(0);
            activeFileName = firstName;
            activeEditor = solutionFileEditors.get(firstName);
            tabPane.getSelectionModel().select(solutionFileTabs.get(firstName));
            if (activeFileChangeListener != null) {
                activeFileChangeListener.accept(activeFileName);
            }
        }
    }

    /**
     * Define o conteúdo de um ficheiro de solução específico.
     *
     * @param fileName Nome do ficheiro
     * @param content Conteúdo do ficheiro
     */
    public void setSolutionFileContent(String fileName, String content) {
        TextArea editor = solutionFileEditors.get(fileName);
        if (editor != null) {
            editor.setText(content);
        }
    }

    /**
     * Cria tab de output do utilizador (read-only).
     *
     * @return Tab configurada
     */
    private Tab createUserOutputTab() {
        userOutputArea = new TextArea();
        userOutputArea.setEditable(false);
        userOutputArea.setPromptText("Output do seu código aparecerá aqui...");
        userOutputArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px; -fx-background-color: #f9f9f9;");
        userOutputArea.setWrapText(true);
        
        Tab tab = new Tab("💻 Output do Utilizador", userOutputArea);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Cria tab de comparação (read-only, formatada).
     *
     * @return Tab configurada
     */
    private Tab createComparisonTab() {
        comparisonArea = new TextArea();
        comparisonArea.setEditable(false);
        comparisonArea.setPromptText("Resultado da comparação aparecerá aqui...");
        comparisonArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px; -fx-background-color: #fffef0;");
        comparisonArea.setWrapText(true);
        
        Tab tab = new Tab("🔍 Resultado da Comparação", comparisonArea);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Retorna o TabPane principal.
     *
     * @return TabPane
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Retorna o editor de código ativo.
     *
     * @return TextArea do editor ativo
     */
    public TextArea getActiveEditor() {
        return activeEditor;
    }

    /**
     * Define callback para alterações de texto no editor.
     *
     * @param listener Callback com (nomeDoFicheiro, conteúdoAtual)
     */
    public void setEditorChangeListener(BiConsumer<String, String> listener) {
        this.editorChangeListener = listener;
    }

    /**
     * Define callback para mudança do ficheiro ativo.
     *
     * @param listener Callback com nome do ficheiro ativo (ou null)
     */
    public void setActiveFileChangeListener(Consumer<String> listener) {
        this.activeFileChangeListener = listener;
    }

    /**
     * Retorna o nome do ficheiro ativo.
     *
     * @return Nome do ficheiro ou null
     */
    public String getActiveFileName() {
        return activeFileName;
    }

    /**
     * Define o conteúdo do output do utilizador.
     *
     * @param content Output capturado da execução
     */
    public void setUserOutput(String content) {
        userOutputArea.setText(content);
    }

    /**
     * Define o resultado da comparação (formatado).
     *
     * @param result Resultado da comparação
     */
    public void setComparisonResult(ComparisonResult result) {
        if (result == null) {
            comparisonArea.setText("Erro ao comparar outputs.");
            return;
        }
        
        String report = result.generateReport();
        comparisonArea.setText(report);
    }

    /**
     * Retorna a linha atual do cursor no editor ativo.
     *
     * @return Número da linha (0-based)
     */
    public int getCurrentCursorLine() {
        if (activeEditor == null) return 0;
        
        String text = activeEditor.getText();
        int caretPosition = activeEditor.getCaretPosition();

        int lineCount = 0;
        for (int i = 0; i < caretPosition && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
            }
        }

        return lineCount;
    }

    /**
     * Retorna o conteúdo da linha atual do cursor.
     *
     * @return String com conteúdo da linha
     */
    public String getCurrentLineContent() {
        if (activeEditor == null) return "";
        
        String text = activeEditor.getText();
        int caretPosition = activeEditor.getCaretPosition();

        int lineStart = text.lastIndexOf('\n', caretPosition - 1) + 1;
        int lineEnd = text.indexOf('\n', caretPosition);
        if (lineEnd == -1) {
            lineEnd = text.length();
        }

        return text.substring(lineStart, lineEnd);
    }

    /**
     * Insere uma sugestão no editor ativo na posição do cursor.
     *
     * @param suggestion Sugestão a inserir
     */
    public void insertSuggestion(String suggestion) {
        if (activeEditor == null) return;
        
        int caretPosition = activeEditor.getCaretPosition();
        String currentText = activeEditor.getText();

        String newText = currentText.substring(0, caretPosition) +
                        "\n" + suggestion +
                        currentText.substring(caretPosition);

        activeEditor.setText(newText);
        activeEditor.positionCaret(caretPosition + suggestion.length() + 1);
    }

    /**
     * Retorna todo o código do editor ativo.
     *
     * @return String com código completo
     */
    public String getUserCode() {
        if (activeEditor == null) return "";
        return activeEditor.getText();
    }

    /**
     * Retorna todos os conteúdos dos ficheiros de solução editados.
     *
     * @return Map com nome do ficheiro -> conteúdo
     */
    public Map<String, String> getAllSolutionFileContents() {
        Map<String, String> contents = new HashMap<>();
        for (Map.Entry<String, TextArea> entry : solutionFileEditors.entrySet()) {
            contents.put(entry.getKey(), entry.getValue().getText());
        }
        return contents;
    }

    /**
     * Limpa todas as tabs de output.
     */
    public void clearOutputs() {
        userOutputArea.clear();
        comparisonArea.clear();
    }
}

