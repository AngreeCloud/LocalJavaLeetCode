package org.example.judge.ui;

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

    // Tabs
    private Tab userCodeTab;
    private Tab expectedOutputTab;
    private Tab userOutputTab;
    private Tab comparisonTab;

    // Conteúdo das tabs
    private TextArea userCodeEditor;
    private TextArea expectedOutputArea;
    private TextArea userOutputArea;
    private TextArea comparisonArea;

    /**
     * Construtor - Cria as tabs fixas.
     */
    public TabManager() {
        createFixedTabs();
    }

    /**
     * Cria as 4 tabs fixas e não removíveis.
     */
    private void createFixedTabs() {
        // TODO: Implementar
        // 1. Criar TabPane
        // 2. Criar cada tab:
        //    - Tab 1: Editor de código (editável)
        //    - Tab 2: Output esperado (read-only)
        //    - Tab 3: Output do utilizador (read-only)
        //    - Tab 4: Comparação (read-only, com cores)
        // 3. Definir todas como não closable (setClosable(false))
        // 4. Adicionar ao TabPane
    }

    /**
     * Cria a tab de código do utilizador (editável).
     *
     * @return Tab configurada
     */
    private Tab createUserCodeTab() {
        // TODO: Implementar
        // 1. Criar TextArea ou CodeArea (RichTextFX)
        // 2. Configurar font monospace
        // 3. Adicionar placeholder text
        // 4. Envolver em ScrollPane
        return null; // TODO
    }

    /**
     * Cria tab de output esperado (read-only).
     *
     * @return Tab configurada
     */
    private Tab createExpectedOutputTab() {
        // TODO: Implementar
        // 1. Criar TextArea (read-only)
        // 2. Estilo diferente (indicar que é read-only)
        return null; // TODO
    }

    /**
     * Cria tab de output do utilizador (read-only).
     *
     * @return Tab configurada
     */
    private Tab createUserOutputTab() {
        // TODO: Implementar
        return null; // TODO
    }

    /**
     * Cria tab de comparação (read-only, formatada).
     *
     * @return Tab configurada
     */
    private Tab createComparisonTab() {
        // TODO: Implementar
        // Usar cores:
        // - Verde para linhas corretas
        // - Vermelho para linhas incorretas
        // - Cinza para informações
        return null; // TODO
    }

    /**
     * Retorna o TabPane principal.
     *
     * @return TabPane com as 4 tabs
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Retorna o editor de código do utilizador.
     *
     * @return TextArea do editor
     */
    public TextArea getUserCodeEditor() {
        return userCodeEditor;
    }

    /**
     * Define o conteúdo do output esperado.
     *
     * @param content Conteúdo da solução oficial
     */
    public void setExpectedOutput(String content) {
        // TODO: Implementar
        // expectedOutputArea.setText(content);
    }

    /**
     * Define o conteúdo do output do utilizador.
     *
     * @param content Output capturado da execução
     */
    public void setUserOutput(String content) {
        // TODO: Implementar
        // userOutputArea.setText(content);
    }

    /**
     * Define o resultado da comparação (formatado).
     *
     * @param result Resultado da comparação
     */
    public void setComparisonResult(ComparisonResult result) {
        // TODO: Implementar
        // 1. Formatar resultado (generateReport())
        // 2. Aplicar cores (verde/vermelho)
        // 3. Mostrar estatísticas
        // 4. Mostrar linhas diferentes
    }

    /**
     * Retorna a linha atual do cursor no editor.
     *
     * @return Número da linha (0-based)
     */
    public int getCurrentCursorLine() {
        String text = userCodeEditor.getText();
        int caretPosition = userCodeEditor.getCaretPosition();

        // Contar \n até a posição do cursor
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
        String text = userCodeEditor.getText();
        int caretPosition = userCodeEditor.getCaretPosition();

        // Encontrar início e fim da linha atual
        int lineStart = text.lastIndexOf('\n', caretPosition - 1) + 1;
        int lineEnd = text.indexOf('\n', caretPosition);
        if (lineEnd == -1) {
            lineEnd = text.length();
        }

        return text.substring(lineStart, lineEnd);
    }

    /**
     * Insere uma sugestão no editor na posição do cursor.
     *
     * @param suggestion Sugestão a inserir
     */
    public void insertSuggestion(String suggestion) {
        int caretPosition = userCodeEditor.getCaretPosition();
        String currentText = userCodeEditor.getText();

        // Inserir na nova linha
        String newText = currentText.substring(0, caretPosition) +
                        "\n" + suggestion +
                        currentText.substring(caretPosition);

        userCodeEditor.setText(newText);
        userCodeEditor.positionCaret(caretPosition + suggestion.length() + 1);
    }

    /**
     * Retorna todo o código do utilizador.
     *
     * @return String com código completo
     */
    public String getUserCode() {
        return userCodeEditor.getText();
    }

    /**
     * Limpa todas as tabs (exceto código do utilizador).
     */
    public void clearOutputs() {
        // TODO: Implementar
        // expectedOutputArea.clear();
        // userOutputArea.clear();
        // comparisonArea.clear();
    }
}

