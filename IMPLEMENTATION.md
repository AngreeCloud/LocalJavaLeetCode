# Instruções de Implementação - Mini Judge

## ✅ Status do Projeto

### Arquitetura Completa Criada

A estrutura completa do projeto foi criada com:

1. ✅ **Documentação completa** (ARCHITECTURE.md com 800+ linhas)
2. ✅ **Estrutura de diretórios** completa
3. ✅ **18 classes Java** com esqueletos completos e documentação Javadoc
4. ✅ **3 classes de teste** com JUnit 5
5. ✅ **Ficheiro CSS** para estilos da interface
6. ✅ **pom.xml** configurado com todas as dependências
7. ✅ **README.md** com documentação do projeto

---

## 📁 Estrutura de Ficheiros Criados

```
LocalJavaLeetCode/
├── ARCHITECTURE.md          ✅ Documentação completa da arquitetura
├── README.md                ✅ Documentação do projeto
├── IMPLEMENTATION.md        ✅ Este ficheiro
├── pom.xml                  ✅ Configurado com JavaFX, RichTextFX, JUnit 5
│
├── src/main/java/org/example/judge/
│   ├── Launcher.java        ✅ Wrapper para JAR executável
│   ├── MainApp.java         ✅ Entry point JavaFX
│   │
│   ├── ui/                  ✅ Pacote de interface
│   │   ├── UIController.java        ✅ Controlador principal
│   │   ├── TabManager.java          ✅ Gestor das 4 tabs fixas
│   │   └── SuggestionPopup.java     ✅ Popup de sugestões
│   │
│   ├── core/                ✅ Pacote de lógica de negócio
│   │   ├── FileManager.java         ✅ Gestão de ficheiros
│   │   ├── CodeExecutor.java        ✅ Compilação e execução
│   │   ├── OutputComparator.java    ✅ Comparação de outputs
│   │   └── SuggestionEngine.java    ✅ Motor de sugestões (CRÍTICO)
│   │
│   ├── model/               ✅ Pacote de modelos
│   │   ├── ExecutionResult.java     ✅ Modelo de resultado de execução
│   │   ├── ComparisonResult.java    ✅ Modelo de comparação
│   │   └── CodeContext.java         ✅ Modelo de contexto de código
│   │
│   └── util/                ✅ Pacote de utilitários
│       ├── CodeAnalyzer.java        ✅ Análise de código
│       └── FileUtils.java           ✅ Utilitários de ficheiros
│
├── src/main/resources/
│   └── styles/
│       └── application.css  ✅ Estilos CSS da aplicação
│
└── src/test/java/org/example/judge/
    ├── CodeExecutorTest.java        ✅ Testes de compilação/execução
    ├── OutputComparatorTest.java    ✅ Testes de comparação
    └── SuggestionEngineTest.java    ✅ Testes do motor de sugestões
```

---

## 🔧 Próximos Passos - Implementação

### Fase 1: Componentes Básicos (Começar aqui)

#### 1.1. FileManager.java
**Prioridade: ALTA** (outros componentes dependem deste)

Implementar métodos:
- [ ] `initializeTempDirectory()` - Criar diretório /tmp/judge-{timestamp}
- [ ] `saveUserCode(String code)` - Guardar código em UserCode.java
- [ ] `readSolution()` - Ler solução oficial
- [ ] `cleanup()` - Remover ficheiros temporários

**Teste:** Executar e verificar que diretório é criado em C:\Users\{user}\AppData\Local\Temp\

---

#### 1.2. FileUtils.java
**Prioridade: ALTA** (suporte ao FileManager)

Implementar métodos:
- [ ] `readFile(Path path)` - Ler ficheiro completo
- [ ] `writeFile(Path path, String content)` - Escrever ficheiro
- [ ] `deleteDirectory(Path directory)` - Remover diretório recursivamente
- [ ] `createTempDirectory(String prefix)` - Criar diretório temporário

---

### Fase 2: Interface Gráfica Básica

#### 2.1. TabManager.java
**Prioridade: ALTA**

Implementar métodos:
- [ ] `createFixedTabs()` - Criar as 4 tabs fixas
- [ ] `getUserCodeEditor()` - Retornar TextArea do editor
- [ ] `setUserOutput(String content)` - Mostrar output na tab
- [ ] `getUserCode()` - Obter código do editor

**Como testar:**
```java
TabManager tm = new TabManager();
TabPane pane = tm.getTabPane();
// Verificar que tem 4 tabs
// Verificar que tabs não são closable
```

---

#### 2.2. UIController.java
**Prioridade: ALTA**

Implementar métodos (ordem sugerida):
1. [ ] `initialize()` - Criar layout básico
2. [ ] `setupSplitPane()` - Configurar SplitPane
3. [ ] `createToolbar()` - Criar botões
4. [ ] `handleUploadStatement()` - Upload de enunciado
5. [ ] `handleUploadSolution()` - Upload de solução
6. [ ] `displayStatement(String content)` - Mostrar enunciado

**Como testar:**
```java
FileManager fm = new FileManager();
UIController controller = new UIController(fm);
BorderPane root = controller.getRoot();
Scene scene = new Scene(root, 1200, 800);
// Verificar que UI aparece
```

---

#### 2.3. MainApp.java
**Prioridade: ALTA**

Implementar:
- [ ] `start(Stage primaryStage)` - Inicializar aplicação
- [ ] `stop()` - Cleanup ao fechar

**Como testar:**
```bash
mvn javafx:run
```

---

### Fase 3: Compilação e Execução

#### 3.1. CodeExecutor.java
**Prioridade: ALTA**

Implementar métodos (ordem sugerida):
1. [ ] `createCompileProcess(Path sourceFile)` - Criar processo javac
2. [ ] `captureOutput(Process process)` - Capturar stdout
3. [ ] `captureErrors(Process process)` - Capturar stderr
4. [ ] `compile(Path sourceFile)` - Compilar código
5. [ ] `execute(String className)` - Executar código
6. [ ] `waitWithTimeout(Process process, int timeoutSeconds)` - Timeout

**Exemplo de implementação (compile):**
```java
public ExecutionResult compile(Path sourceFile) {
    try {
        ProcessBuilder pb = new ProcessBuilder("javac", sourceFile.toString());
        pb.directory(fileManager.getTmpDir().toFile());
        
        Process process = pb.start();
        String errors = captureErrors(process);
        
        int exitCode = process.waitFor();
        boolean success = (exitCode == 0);
        
        return new ExecutionResult(success, "", errors, exitCode, 0);
    } catch (Exception e) {
        return new ExecutionResult(false, e.getMessage());
    }
}
```

**Como testar:**
```java
// Ver CodeExecutorTest.java
```

---

### Fase 4: Comparação de Outputs

#### 4.1. OutputComparator.java
**Prioridade: MÉDIA**

Implementar métodos:
- [ ] `compare(String userOutput, String expectedOutput)` - Comparação principal
- [ ] `lineDiff(String[] userLines, String[] expectedLines)` - Diff linha a linha
- [ ] `generateDiffReport(ComparisonResult result)` - Relatório formatado

**Algoritmo de comparação:**
```java
public ComparisonResult compare(String userOutput, String expectedOutput) {
    String[] userLines = userOutput.split("\n");
    String[] expectedLines = expectedOutput.split("\n");
    
    List<DiffLine> differences = new ArrayList<>();
    int matchingLines = 0;
    int totalLines = Math.max(userLines.length, expectedLines.length);
    
    for (int i = 0; i < totalLines; i++) {
        String userLine = i < userLines.length ? userLines[i] : null;
        String expectedLine = i < expectedLines.length ? expectedLines[i] : null;
        
        if (userLine != null && expectedLine != null) {
            if (linesEqual(userLine, expectedLine)) {
                differences.add(new DiffLine(i+1, expectedLine, userLine, DiffType.MATCH));
                matchingLines++;
            } else {
                differences.add(new DiffLine(i+1, expectedLine, userLine, DiffType.DIFFERENT));
            }
        } else if (expectedLine != null) {
            differences.add(new DiffLine(i+1, expectedLine, null, DiffType.MISSING));
        } else {
            differences.add(new DiffLine(i+1, null, userLine, DiffType.EXTRA));
        }
    }
    
    boolean isMatch = (matchingLines == totalLines);
    return new ComparisonResult(isMatch, differences, totalLines, matchingLines);
}
```

---

### Fase 5: Motor de Sugestões (COMPONENTE MAIS COMPLEXO)

#### 5.1. CodeAnalyzer.java
**Prioridade: ALTA** (SuggestionEngine depende deste)

Implementar métodos (ordem sugerida):
1. [ ] `extractTokens(String code)` - Extrair tokens
2. [ ] `calculateIndentationLevel(String line)` - Calcular indentação
3. [ ] `extractKeywords(String code)` - Extrair keywords Java
4. [ ] `getPreviousLines(String code, int lineNumber, int count)` - Linhas anteriores
5. [ ] `identifyBlock(String code, int lineNumber)` - Identificar tipo de bloco

**Exemplo de implementação (extractTokens):**
```java
public List<String> extractTokens(String code) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(code);
    
    while (matcher.find()) {
        String token = matcher.group();
        if (!token.isEmpty()) {
            tokens.add(token);
        }
    }
    
    return tokens;
}
```

---

#### 5.2. SuggestionEngine.java
**Prioridade: CRÍTICA** (componente principal do sistema)

Implementar métodos (ordem sugerida):

**Passo 1:** Análise de contexto
```java
private CodeContext analyzeUserContext(String userCode, int cursorLine) {
    String[] lines = userCode.split("\n");
    
    // Extrair linha atual e anteriores
    String currentLine = cursorLine < lines.length ? lines[cursorLine] : "";
    List<String> previousLines = codeAnalyzer.getPreviousLines(userCode, cursorLine, 3);
    
    // Analisar contexto
    String contextCode = String.join("\n", previousLines) + "\n" + currentLine;
    List<String> tokensList = codeAnalyzer.extractTokens(contextCode);
    Set<String> tokens = new HashSet<>(tokensList);
    
    BlockType blockType = codeAnalyzer.identifyBlock(userCode, cursorLine);
    int indentLevel = codeAnalyzer.calculateIndentationLevel(currentLine);
    List<String> keywordsList = codeAnalyzer.extractKeywords(contextCode);
    Set<String> keywords = new HashSet<>(keywordsList);
    
    return new CodeContext(cursorLine, currentLine, previousLines, 
                          tokens, blockType, indentLevel, null, keywords);
}
```

**Passo 2:** Scoring de linhas
```java
private List<ScoredLine> scoreOfficialLines(CodeContext userContext, String officialSolution) {
    String[] solutionLines = officialSolution.split("\n");
    List<ScoredLine> scoredLines = new ArrayList<>();
    
    for (int i = 0; i < solutionLines.length; i++) {
        // Criar contexto para cada linha da solução
        String line = solutionLines[i];
        List<String> prevLines = codeAnalyzer.getPreviousLines(officialSolution, i, 3);
        
        String contextCode = String.join("\n", prevLines) + "\n" + line;
        Set<String> tokens = new HashSet<>(codeAnalyzer.extractTokens(contextCode));
        BlockType blockType = codeAnalyzer.identifyBlock(officialSolution, i);
        int indentLevel = codeAnalyzer.calculateIndentationLevel(line);
        Set<String> keywords = new HashSet<>(codeAnalyzer.extractKeywords(contextCode));
        
        CodeContext officialCtx = new CodeContext(i, line, prevLines, 
                                                  tokens, blockType, indentLevel, null, keywords);
        
        // Calcular score
        int score = calculateContextScore(userContext, officialCtx);
        scoredLines.add(new ScoredLine(i, score));
    }
    
    // Ordenar por score descendente
    scoredLines.sort((a, b) -> Integer.compare(b.score, a.score));
    
    return scoredLines;
}
```

**Passo 3:** Cálculo de score
```java
private int calculateContextScore(CodeContext userCtx, CodeContext officialCtx) {
    int score = 0;
    
    // Tokens em comum (+10 cada)
    Set<String> commonTokens = new HashSet<>(userCtx.getTokens());
    commonTokens.retainAll(officialCtx.getTokens());
    score += commonTokens.size() * 10;
    
    // Tipo de bloco igual (+20)
    if (userCtx.getBlockType() == officialCtx.getBlockType()) {
        score += 20;
    }
    
    // Indentação similar (+15)
    if (Math.abs(userCtx.getIndentationLevel() - officialCtx.getIndentationLevel()) <= 1) {
        score += 15;
    }
    
    // Keywords em comum (+5 cada)
    Set<String> commonKeywords = new HashSet<>(userCtx.getKeywords());
    commonKeywords.retainAll(officialCtx.getKeywords());
    score += commonKeywords.size() * 5;
    
    // Similaridade das linhas anteriores (+30 se >50%)
    double prevSimilarity = calculatePreviousLinesSimilarity(userCtx, officialCtx);
    if (prevSimilarity > 0.5) {
        score += 30;
    }
    
    return score;
}
```

**Passo 4:** Método principal
```java
public String suggestNextLine(String userCode, int cursorLine, String officialSolution) {
    // 1. Analisar contexto do utilizador
    CodeContext userContext = analyzeUserContext(userCode, cursorLine);
    
    // 2. Calcular scores para todas as linhas da solução
    List<ScoredLine> scoredLines = scoreOfficialLines(userContext, officialSolution);
    
    // 3. Encontrar melhor match
    int bestMatchIndex = findBestMatch(scoredLines);
    
    if (bestMatchIndex == -1) {
        return null; // Nenhum match adequado
    }
    
    // 4. Retornar próxima linha
    String suggestion = getNextLine(officialSolution, bestMatchIndex);
    
    // 5. Guardar em histórico
    if (suggestion != null) {
        suggestedLines.add(bestMatchIndex);
    }
    
    return suggestion;
}
```

---

### Fase 6: Popup de Sugestões

#### 6.1. SuggestionPopup.java
**Prioridade: MÉDIA**

Implementar:
- [ ] `createPopup()` - Criar e estilizar popup
- [ ] `show(String suggestion, double x, double y, Runnable onInsert)` - Mostrar popup
- [ ] `hide()` - Esconder popup

---

### Fase 7: Conectar Tudo no UIController

#### 7.1. handleExecute()
```java
private void handleExecute() {
    try {
        // 1. Obter código do editor
        String userCode = tabManager.getUserCode();
        
        // 2. Guardar código
        fileManager.saveUserCode(userCode);
        
        // 3. Compilar
        ExecutionResult compileResult = codeExecutor.compile(fileManager.getUserCodePath());
        
        if (!compileResult.isSuccess()) {
            showError("Erro de compilação:\n" + compileResult.getErrors());
            return;
        }
        
        // 4. Executar
        ExecutionResult execResult = codeExecutor.execute(fileManager.getUserCodeClassName());
        
        if (!execResult.isSuccess()) {
            showError("Erro de execução:\n" + execResult.getErrors());
            return;
        }
        
        // 5. Mostrar output
        tabManager.setUserOutput(execResult.getOutput());
        
        // 6. Comparar com solução
        String expectedOutput = fileManager.readSolution();
        ComparisonResult comparison = outputComparator.compare(execResult.getOutput(), expectedOutput);
        
        // 7. Mostrar comparação
        tabManager.setComparisonResult(comparison);
        
        if (comparison.isMatch()) {
            showSuccess("✓ Output correto!");
        }
        
    } catch (Exception e) {
        showError("Erro: " + e.getMessage());
    }
}
```

#### 7.2. handleSuggestion()
```java
private void handleSuggestion() {
    try {
        // 1. Obter código e posição do cursor
        String userCode = tabManager.getUserCode();
        int cursorLine = tabManager.getCurrentCursorLine();
        
        // 2. Ler solução oficial
        String officialSolution = fileManager.readSolution();
        
        if (officialSolution == null || officialSolution.isEmpty()) {
            showError("Carregue primeiro a solução oficial!");
            return;
        }
        
        // 3. Obter sugestão
        String suggestion = suggestionEngine.suggestNextLine(userCode, cursorLine, officialSolution);
        
        if (suggestion == null) {
            showError("Não foi possível encontrar sugestão contextual.");
            return;
        }
        
        // 4. Mostrar popup
        // TODO: Calcular coordenadas do cursor
        suggestionPopup.show(suggestion, 400, 300, () -> {
            tabManager.insertSuggestion(suggestion);
        });
        
    } catch (Exception e) {
        showError("Erro ao gerar sugestão: " + e.getMessage());
    }
}
```

---

## 🧪 Como Testar

### Teste Manual Completo

1. **Executar aplicação:**
```bash
mvn javafx:run
```

2. **Carregar enunciado:**
   - Criar ficheiro `problema.txt` com enunciado
   - Clicar "Upload Enunciado"
   - Verificar preview no painel esquerdo

3. **Carregar solução oficial:**
   - Criar ficheiro `solucao.txt` com output esperado
   - Clicar "Upload Solução"

4. **Escrever código:**
   - Na tab "Código do Utilizador", escrever:
   ```java
   public class UserCode {
       public static void main(String[] args) {
           System.out.println("Hello World");
       }
   }
   ```

5. **Executar:**
   - Clicar "Executar"
   - Verificar compilação
   - Verificar output na tab "Output do Utilizador"
   - Verificar comparação na tab "Resultado da Comparação"

6. **Testar sugestão:**
   - Apagar parte do código
   - Posicionar cursor
   - Clicar na lâmpada 💡
   - Verificar se sugestão aparece

---

## 📦 Gerar JAR Executável

Quando tudo estiver implementado:

```bash
mvn clean package
```

Isto gera: `target/LocalJavaLeetCode-1.0-SNAPSHOT-shaded.jar`

Executar:
```bash
java -jar target/LocalJavaLeetCode-1.0-SNAPSHOT-shaded.jar
```

---

## 🎯 Ordem de Implementação Recomendada

1. **Semana 1:** FileManager + FileUtils + modelos
2. **Semana 2:** TabManager + UI básica + MainApp
3. **Semana 3:** CodeExecutor + OutputComparator
4. **Semana 4:** CodeAnalyzer (parcial) + SuggestionEngine (básico)
5. **Semana 5:** SuggestionEngine (refinamento) + SuggestionPopup
6. **Semana 6:** Testes + correções + polish

---

## 📚 Recursos e Documentação

- **JavaFX:** https://openjfx.io/
- **RichTextFX:** https://github.com/FXMisc/RichTextFX
- **ProcessBuilder:** https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/lang/ProcessBuilder.html
- **Arquitetura completa:** Ver `ARCHITECTURE.md`

---

## ⚠️ Notas Importantes

1. **SuggestionEngine é o componente mais complexo** - Comece com implementação básica e refine depois
2. **Testes são essenciais** - Implemente os testes em paralelo
3. **UI primeiro, depois lógica** - Mais fácil testar visualmente
4. **Compile frequentemente** - `mvn compile` após cada classe implementada

---

## ✅ Checklist Final

Antes de considerar o projeto completo:

- [ ] Todas as classes implementadas
- [ ] Todos os testes passam
- [ ] UI funcional e responsiva
- [ ] Upload de ficheiros funciona
- [ ] Compilação e execução funcionam
- [ ] Comparação de outputs funciona
- [ ] Sugestões funcionam (mesmo que básicas)
- [ ] Popup de sugestões funciona
- [ ] Cleanup de ficheiros temporários funciona
- [ ] JAR executável gerado e testado
- [ ] CSS aplicado e visual agradável
- [ ] Erros tratados adequadamente
- [ ] Documentação atualizada

---

**Boa sorte com a implementação! 🚀**

Consulte sempre `ARCHITECTURE.md` para detalhes sobre cada componente.

