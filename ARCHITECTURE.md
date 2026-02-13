# Arquitetura do Mini Judge de Programação Java

## 1. Estrutura de Pastas

```
LocalJavaLeetCode/
├── pom.xml
├── ARCHITECTURE.md (este ficheiro)
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── org/
    │   │       └── example/
    │   │           └── judge/
    │   │               ├── MainApp.java          # Entry point JavaFX
    │   │               ├── Launcher.java         # Wrapper para JAR executável
    │   │               ├── ui/
    │   │               │   ├── UIController.java # Controlador principal da UI
    │   │               │   ├── TabManager.java   # Gestor das tabs fixas
    │   │               │   └── SuggestionPopup.java # Popup de sugestões
    │   │               ├── core/
    │   │               │   ├── FileManager.java     # Gestão de ficheiros
    │   │               │   ├── CodeExecutor.java    # Compilação e execução
    │   │               │   ├── OutputComparator.java # Comparação de outputs
    │   │               │   └── SuggestionEngine.java # Motor de sugestões
    │   │               ├── model/
    │   │               │   ├── ExecutionResult.java  # Resultado da execução
    │   │               │   ├── ComparisonResult.java # Resultado da comparação
    │   │               │   └── CodeContext.java      # Contexto do código para sugestões
    │   │               └── util/
    │   │                   ├── CodeAnalyzer.java     # Análise de código (tokens, blocos)
    │   │                   └── FileUtils.java        # Utilitários de ficheiros
    │   └── resources/
    │       ├── styles/
    │       │   └── application.css   # Estilos CSS da aplicação
    │       └── icons/
    │           └── lightbulb.png     # Ícone da lâmpada de sugestão
    └── test/
        └── java/
            └── org/
                └── example/
                    └── judge/
                        ├── CodeExecutorTest.java
                        ├── OutputComparatorTest.java
                        └── SuggestionEngineTest.java
```

## 2. Fluxo de Execução

```
[Iniciar Aplicação]
        ↓
[UI Carregada - MainApp]
        ↓
[Utilizador faz Upload do Enunciado]
        ↓ (FileManager.saveStatement())
[Preview do Enunciado no SplitPane]
        ↓
[Utilizador faz Upload da Solução Oficial]
        ↓ (FileManager.saveSolution())
[Solução armazenada em /tmp]
        ↓
[Utilizador escreve código na Tab "Código do Utilizador"]
        ↓
[Utilizador clica "Executar"]
        ↓ (FileManager.saveUserCode())
[Código guardado em /tmp]
        ↓ (CodeExecutor.compile())
[Compilação com javac via ProcessBuilder]
        ↓
[Sucesso?] ──No──> [Mostrar erros de compilação]
        ↓ Yes
[CodeExecutor.execute()]
        ↓
[Capturar stdout e stderr]
        ↓
[Mostrar output na Tab "Output do Utilizador"]
        ↓ (OutputComparator.compare())
[Comparar linha a linha com solução oficial]
        ↓
[Mostrar resultado na Tab "Resultado da Comparação"]
        ↓
[Utilizador clica na "Lâmpada de Sugestão"]
        ↓ (SuggestionEngine.suggestLine())
[Analisar contexto da linha atual]
        ↓
[Encontrar linha equivalente na solução oficial]
        ↓
[Mostrar sugestão em Popup]
```

## 3. Classes e Responsabilidades

### 3.1. MainApp.java (JavaFX Entry Point)

**Responsabilidade:** Iniciar a aplicação JavaFX e carregar a interface principal.

**Métodos Esperados:**
- `void start(Stage primaryStage)` - Inicializa o palco principal e carrega a UI
- `void stop()` - Cleanup ao fechar a aplicação (limpar /tmp)
- `public static void main(String[] args)` - Entry point

**Dependências:**
- UIController (instancia e configura)
- FileManager (para cleanup)

---

### 3.2. Launcher.java (Wrapper para JAR)

**Responsabilidade:** Wrapper necessário para executar JavaFX a partir de um JAR executável (contorna limitações do JavaFX).

**Métodos Esperados:**
- `public static void main(String[] args)` - Chama MainApp.main()

---

### 3.3. ui/UIController.java

**Responsabilidade:** Controlador principal da interface gráfica. Gere todos os componentes visuais.

**Componentes:**
- `SplitPane mainSplitPane` - Divide enunciado e área de trabalho
- `TextArea statementPreview` - Preview do enunciado (read-only)
- `TabPane workspaceTabPane` - Tabs fixas
- `Button uploadStatementBtn`
- `Button uploadSolutionBtn`
- `Button executeBtn`
- `Button suggestionBtn` - Lâmpada de sugestão

**Métodos Esperados:**
- `void initialize()` - Inicializa todos os componentes da UI
- `void setupSplitPane()` - Configura o SplitPane ajustável
- `void setupTabs()` - Cria as 4 tabs fixas
- `void handleUploadStatement()` - Handler para upload do enunciado
- `void handleUploadSolution()` - Handler para upload da solução
- `void handleExecute()` - Handler para executar código
- `void handleSuggestion()` - Handler para mostrar sugestão
- `void displayStatement(String content)` - Mostra enunciado no preview
- `void displayUserOutput(String output)` - Mostra output na tab
- `void displayComparison(ComparisonResult result)` - Mostra comparação
- `void showError(String message)` - Mostra erros ao utilizador

**Dependências:**
- FileManager
- CodeExecutor
- OutputComparator
- SuggestionEngine
- TabManager
- SuggestionPopup

---

### 3.4. ui/TabManager.java

**Responsabilidade:** Gere as 4 tabs fixas e o seu conteúdo.

**Tabs:**
1. "Código do Utilizador" (editável) - TextArea/CodeArea
2. "Output Esperado" (read-only) - TextArea
3. "Output do Utilizador" (read-only) - TextArea
4. "Resultado da Comparação" (read-only) - TextArea com cores

**Métodos Esperados:**
- `TabPane createFixedTabs()` - Cria as 4 tabs fixas
- `TextArea getUserCodeEditor()` - Retorna o editor de código
- `void setExpectedOutput(String content)` - Define o output esperado
- `void setUserOutput(String content)` - Define o output do utilizador
- `void setComparisonResult(ComparisonResult result)` - Mostra comparação formatada
- `int getCurrentCursorLine()` - Retorna a linha atual do cursor no editor
- `String getCurrentLineContent()` - Retorna o conteúdo da linha atual
- `void insertSuggestion(String suggestion)` - Insere sugestão no editor

**Dependências:**
- Nenhuma (componente puro de UI)

---

### 3.5. ui/SuggestionPopup.java

**Responsabilidade:** Cria e mostra um popup com a sugestão de código.

**Métodos Esperados:**
- `void show(String suggestion, double x, double y)` - Mostra popup nas coordenadas
- `void hide()` - Esconde o popup
- `Popup createStyledPopup(String suggestion)` - Cria popup estilizado

**Dependências:**
- Nenhuma

---

### 3.6. core/FileManager.java

**Responsabilidade:** Gestão de todos os ficheiros (upload, guardar, limpar).

**Atributos:**
- `Path tmpDir` - Diretório temporário (/tmp ou equivalente Windows)
- `Path statementPath` - Caminho do enunciado
- `Path solutionPath` - Caminho da solução oficial
- `Path userCodePath` - Caminho do código do utilizador
- `Path userOutputPath` - Caminho do output do utilizador

**Métodos Esperados:**
- `void initializeTempDirectory()` - Cria /tmp/judge-{timestamp}/
- `String readStatement(File file)` - Lê e retorna conteúdo do enunciado
- `void saveStatement(File file)` - Copia enunciado para /tmp
- `void saveSolution(File file)` - Copia solução para /tmp
- `void saveUserCode(String code)` - Guarda código do utilizador em /tmp
- `String readSolution()` - Lê solução oficial
- `Path getUserCodePath()` - Retorna caminho do ficheiro do utilizador
- `void saveUserOutput(String output)` - Guarda output em ficheiro
- `String readUserOutput()` - Lê output do utilizador
- `void cleanup()` - Remove todos os ficheiros temporários
- `String readFileContent(Path path)` - Utilitário para ler ficheiros

**Dependências:**
- java.nio.file.*
- java.io.*

---

### 3.7. core/CodeExecutor.java

**Responsabilidade:** Compilar e executar código Java usando ProcessBuilder.

**Métodos Esperados:**
- `ExecutionResult compile(Path sourceFile)` - Compila código Java
  - Usa `javac` via ProcessBuilder
  - Captura stderr para erros de compilação
  - Retorna ExecutionResult com sucesso/falha
  
- `ExecutionResult execute(Path classFile)` - Executa código compilado
  - Usa `java` via ProcessBuilder
  - Captura stdout e stderr
  - Define timeout (ex: 5 segundos)
  - Retorna ExecutionResult com output e erros
  
- `ProcessBuilder createCompileProcess(Path sourceFile)` - Cria processo de compilação
- `ProcessBuilder createExecutionProcess(Path classFile)` - Cria processo de execução
- `String captureOutput(Process process)` - Captura stdout
- `String captureErrors(Process process)` - Captura stderr

**Dependências:**
- FileManager (para obter caminhos)
- ExecutionResult (modelo)

---

### 3.8. core/OutputComparator.java

**Responsabilidade:** Comparar output do utilizador com solução oficial, linha a linha.

**Métodos Esperados:**
- `ComparisonResult compare(String userOutput, String expectedOutput)` - Comparação principal
  - Compara linha a linha
  - Ignora whitespace extra (opcional)
  - Identifica linhas diferentes
  - Retorna ComparisonResult
  
- `List<DiffLine> lineDiff(String[] userLines, String[] expectedLines)` - Diff detalhado
- `boolean isMatch()` - Retorna se outputs são idênticos
- `String generateDiffReport()` - Gera relatório formatado de diferenças
- `String highlightDifferences(String line1, String line2)` - Destaca diferenças

**Dependências:**
- ComparisonResult (modelo)

---

### 3.9. core/SuggestionEngine.java

**Responsabilidade:** Motor de sugestões inteligente. Analisa contexto e sugere UMA linha da solução oficial.

**Estratégia de Matching Contextual:**

Este é o componente mais complexo. NÃO usa números de linha diretamente. Usa heurísticas:

1. **Análise de Contexto:**
   - Analisa as 2-3 linhas acima do cursor no código do utilizador
   - Extrai tokens, palavras-chave, identificadores
   - Identifica em que bloco está (método, if, for, while, class)

2. **Busca na Solução Oficial:**
   - Percorre a solução oficial linha a linha
   - Para cada linha, calcula "score de similaridade contextual"
   - Considera:
     - Tokens em comum nas linhas anteriores
     - Mesmo nível de indentação/profundidade
     - Mesma estrutura de bloco (ex: ambos dentro de um for)
     - Palavras-chave similares

3. **Scoring:**
   - Atribui pontos por cada token correspondente
   - Atribui pontos por estrutura similar
   - Atribui pontos por profundidade de bloco similar

4. **Seleção:**
   - Escolhe a linha com maior score
   - Retorna a linha seguinte (a sugestão)

**Métodos Esperados:**
- `String suggestNextLine(String userCode, int cursorLine, String officialSolution)` - Método principal
  - Analisa contexto do utilizador
  - Encontra linha equivalente na solução
  - Retorna sugestão
  
- `CodeContext analyzeUserContext(String userCode, int cursorLine)` - Analisa contexto
  - Extrai linhas anteriores
  - Identifica bloco atual
  - Extrai tokens relevantes
  
- `List<ScoredLine> scoreOfficialLines(CodeContext userContext, String officialSolution)` - Scoring
  - Para cada linha da solução, calcula score
  - Retorna lista ordenada por score
  
- `int calculateContextScore(CodeContext userCtx, CodeContext officialCtx)` - Calcula similaridade
  - Compara tokens
  - Compara estrutura
  - Compara profundidade
  
- `String findBestMatch(List<ScoredLine> scoredLines)` - Seleciona melhor match
- `String getNextLine(String solution, int matchedLineIndex)` - Retorna linha seguinte

**Dependências:**
- CodeAnalyzer (para análise de tokens e estrutura)
- CodeContext (modelo)

---

### 3.10. util/CodeAnalyzer.java

**Responsabilidade:** Análise estática de código Java (tokens, blocos, estrutura).

**Métodos Esperados:**
- `List<String> extractTokens(String code)` - Extrai todos os tokens
  - Identifica palavras-chave Java
  - Identifica identificadores
  - Identifica operadores
  
- `BlockType identifyBlock(String code, int lineNumber)` - Identifica tipo de bloco
  - CLASS, METHOD, IF, FOR, WHILE, TRY, etc.
  
- `int calculateIndentationLevel(String line)` - Calcula nível de indentação
- `List<String> extractKeywords(String code)` - Extrai palavras-chave Java
- `String normalizeWhitespace(String code)` - Normaliza espaços
- `boolean isInsideMethod(String code, int lineNumber)` - Verifica se está dentro de método
- `String getMethodSignature(String code, int lineNumber)` - Retorna assinatura do método atual

**Dependências:**
- Nenhuma (utilitário puro)

---

### 3.11. util/FileUtils.java

**Responsabilidade:** Utilitários gerais para manipulação de ficheiros.

**Métodos Esperados:**
- `String readFile(Path path)` - Lê ficheiro completo
- `void writeFile(Path path, String content)` - Escreve ficheiro
- `void copyFile(Path source, Path destination)` - Copia ficheiro
- `void deleteDirectory(Path directory)` - Remove diretório recursivamente
- `Path createTempDirectory(String prefix)` - Cria diretório temporário

---

### 3.12. model/ExecutionResult.java

**Responsabilidade:** Modelo que representa o resultado de compilação/execução.

**Atributos:**
- `boolean success` - Se foi bem-sucedido
- `String output` - Output do programa (stdout)
- `String errors` - Erros (stderr)
- `int exitCode` - Código de saída
- `long executionTimeMs` - Tempo de execução

**Métodos:**
- Getters e Setters
- `boolean hasErrors()`
- `String getFormattedOutput()`

---

### 3.13. model/ComparisonResult.java

**Responsabilidade:** Modelo que representa o resultado da comparação de outputs.

**Atributos:**
- `boolean isMatch` - Se outputs são idênticos
- `List<DiffLine> differences` - Lista de diferenças
- `int totalLines` - Total de linhas comparadas
- `int matchingLines` - Linhas que coincidem
- `double similarityPercentage` - Percentagem de similaridade

**Classes Internas:**
- `class DiffLine`:
  - `int lineNumber`
  - `String expectedLine`
  - `String userLine`
  - `DiffType type` (MATCH, DIFFERENT, MISSING, EXTRA)

**Métodos:**
- Getters e Setters
- `String generateReport()` - Gera relatório formatado

---

### 3.14. model/CodeContext.java

**Responsabilidade:** Modelo que representa o contexto de uma posição no código.

**Atributos:**
- `int lineNumber` - Linha atual
- `String currentLine` - Conteúdo da linha atual
- `List<String> previousLines` - 2-3 linhas anteriores
- `List<String> tokens` - Tokens extraídos do contexto
- `BlockType blockType` - Tipo de bloco (IF, FOR, METHOD, etc.)
- `int indentationLevel` - Nível de indentação
- `String methodSignature` - Assinatura do método (se aplicável)
- `List<String> keywords` - Palavras-chave presentes

**Métodos:**
- Getters e Setters
- `boolean isSimilarTo(CodeContext other)` - Compara contextos

---

## 4. Descrição Detalhada do SuggestionEngine

### 4.1. Algoritmo de Matching Contextual

O SuggestionEngine é o componente mais crítico. Funciona assim:

**Passo 1: Análise do Código do Utilizador**
```
- Obtém posição do cursor (linha atual)
- Extrai 3 linhas anteriores + linha atual
- Analisa tokens: palavras-chave, identificadores, operadores
- Identifica estrutura: dentro de método? qual bloco (if/for/while)?
- Calcula indentação
```

**Passo 2: Criação do Perfil Contextual**
```
CodeContext {
  tokens: [if, int, sum, for, i]
  blockType: FOR_LOOP
  indentationLevel: 2
  previousLines: ["int sum = 0;", "for (int i = 0; i < n; i++) {"]
  keywords: [int, for]
}
```

**Passo 3: Varredura da Solução Oficial**
```
Para cada linha da solução oficial:
  1. Criar CodeContext para essa linha
  2. Calcular score de similaridade:
     - +10 pontos por cada token em comum
     - +20 pontos se blockType é igual
     - +15 pontos se indentationLevel é igual (±1)
     - +5 pontos por cada keyword em comum
     - +30 pontos se previousLines têm >50% tokens em comum
  3. Guardar linha + score
```

**Passo 4: Seleção da Melhor Correspondência**
```
- Ordenar linhas por score (descendente)
- Selecionar linha com maior score
- Retornar a PRÓXIMA linha (a sugestão)
```

**Exemplo Prático:**

```java
// Código do Utilizador (cursor na linha 5):
1: public class Sum {
2:   public static void main(String[] args) {
3:     int sum = 0;
4:     for (int i = 0; i < 10; i++) {
5:       |← CURSOR AQUI
6:   }
7: }

// Solução Oficial:
1: public class Sum {
2:   public static void main(String[] args) {
3:     int sum = 0;
4:     for (int i = 0; i < 10; i++) {
5:       sum += i;
6:     }
7:     System.out.println(sum);
8:   }
9: }

// Análise:
- UserContext: tokens=[int, sum, for, i], blockType=FOR_LOOP, indent=2
- Varredura da solução:
  - Linha 1: score=10 (só tem "public class")
  - Linha 2: score=15 (public static void main)
  - Linha 3: score=40 (int sum = 0 - muitos tokens comuns)
  - Linha 4: score=80 (for loop - contexto anterior muito similar)
  - Linha 5: score=95 ← MAIOR SCORE (contexto idêntico: dentro de for, mesmo indent)
  
// Sugestão: linha 5 da solução → "sum += i;"
```

### 4.2. Heurísticas Adicionais

Para melhorar a precisão:

1. **Prioridade a blocos estruturais:** Se o utilizador está dentro de um `for`, dê mais peso a linhas da solução também dentro de `for`.

2. **Análise de variáveis:** Se o utilizador declarou `int sum`, procure linhas que usem `sum`.

3. **Sequência de tokens:** Não só tokens individuais, mas sequências (bigrams): `int sum`, `for i`.

4. **Distância relativa:** Se já houve 3 sugestões e todas foram no início do método, a próxima provavelmente também está no início.

5. **Linha já sugerida:** Manter histórico de sugestões para não repetir.

---

## 5. Fluxo de Dados

```
┌─────────────────────────────────────────────────────────────┐
│                         UIController                         │
│  - Gere eventos de utilizador                                │
│  - Coordena outros componentes                               │
└────────────┬───────────────────────────────┬─────────────────┘
             │                               │
             ↓                               ↓
   ┌─────────────────┐           ┌─────────────────────┐
   │  TabManager     │           │  FileManager        │
   │  - Gere tabs    │           │  - Upload/Save      │
   │  - Editor       │           │  - Read files       │
   └─────────────────┘           └──────────┬──────────┘
                                            │
                                            ↓
                                  ┌─────────────────────┐
                                  │  CodeExecutor       │
                                  │  - Compile          │
                                  │  - Execute          │
                                  └──────────┬──────────┘
                                            │
                                            ↓
                                  ┌─────────────────────┐
                                  │  OutputComparator   │
                                  │  - Compare outputs  │
                                  └─────────────────────┘
                                            
   ┌─────────────────────────────────────────────────────────┐
   │  SuggestionEngine (acionado por botão)                  │
   │  - Analisa contexto do utilizador                       │
   │  - Lê solução oficial                                   │
   │  - Calcula matching                                     │
   │  - Retorna sugestão                                     │
   └────────────┬────────────────────────────────────────────┘
                │
                ↓
   ┌─────────────────────┐           ┌─────────────────────┐
   │  CodeAnalyzer       │           │  SuggestionPopup    │
   │  - Tokens           │           │  - Mostra sugestão  │
   │  - Blocos           │           └─────────────────────┘
   └─────────────────────┘
```

---

## 6. Geração de JAR Executável

### 6.1. Compilação com Maven

```bash
mvn clean package
```

Isto gera:
- `target/LocalJavaLeetCode-1.0-SNAPSHOT.jar` (JAR normal)
- `target/LocalJavaLeetCode-1.0-SNAPSHOT-shaded.jar` (JAR executável com dependências)

### 6.2. Execução do JAR

```bash
java -jar target/LocalJavaLeetCode-1.0-SNAPSHOT-shaded.jar
```

**Nota:** O Launcher.java é necessário porque JavaFX não permite `MainApp` como entry point direto num JAR.

---

## 7. Preparação para .exe com jpackage (Futuro)

Quando necessário criar executável nativo Windows:

```bash
jpackage --input target/ \
         --name MiniJudge \
         --main-jar LocalJavaLeetCode-1.0-SNAPSHOT-shaded.jar \
         --main-class org.example.judge.Launcher \
         --type exe \
         --icon src/main/resources/icons/app-icon.ico \
         --win-shortcut \
         --win-menu
```

Isto gera um instalador `.exe` que:
- Inclui JRE
- Cria atalho no menu iniciar
- É completamente standalone

---

## 8. Gestão de Ficheiros Temporários

### 8.1. Estrutura de /tmp

Windows: `C:\Users\{user}\AppData\Local\Temp\judge-{timestamp}\`

Linux/Mac: `/tmp/judge-{timestamp}/`

```
/tmp/judge-20260212-143022/
├── statement.txt           # Enunciado carregado
├── solution.txt            # Solução oficial (output esperado)
├── UserCode.java           # Código do utilizador
├── UserCode.class          # Compilado
└── user-output.txt         # Output capturado
```

### 8.2. Cleanup

- Ao fechar aplicação: `FileManager.cleanup()` remove tudo
- Opcional: Limpar ficheiros com >24h na inicialização

---

## 9. Detalhes de Implementação da UI

### 9.1. Layout Principal

```
┌──────────────────────────────────────────────────────────────┐
│  [Upload Enunciado] [Upload Solução] [Executar] [💡 Sugestão]│
├────────────────────┬─────────────────────────────────────────┤
│                    │  ┌──────────────────────────────────┐   │
│   ENUNCIADO        │  │  Tab: Código do Utilizador      │   │
│   (TextArea        │  ├──────────────────────────────────┤   │
│    read-only)      │  │                                  │   │
│                    │  │  // Editor de código aqui        │   │
│   Ajustável ←→     │  │                                  │   │
│                    │  │                                  │   │
│                    │  └──────────────────────────────────┘   │
│                    │  Outras tabs: Output Esperado,          │
│                    │  Output do Utilizador, Comparação       │
└────────────────────┴─────────────────────────────────────────┘
```

### 9.2. SplitPane

- `SplitPane.setDividerPositions(0.3)` - Enunciado ocupa 30% inicialmente
- `SplitPane.setResizableWithParent(true)` - Ajustável pelo utilizador

### 9.3. Tabs Fixas

- `Tab.setClosable(false)` - Não podem ser fechadas
- Ordem fixa: 1) Código, 2) Output Esperado, 3) Output Utilizador, 4) Comparação

### 9.4. Lâmpada de Sugestão

- Botão com ícone 💡 (ou imagem lightbulb.png)
- Atalho: `Ctrl+L` (definir KeyCombination)
- Tooltip: "Obter sugestão da próxima linha"

### 9.5. Popup de Sugestão

```
┌────────────────────────────────────┐
│ 💡 Sugestão:                       │
│                                    │
│   sum += i;                        │
│                                    │
│ [Inserir] [Ignorar]                │
└────────────────────────────────────┘
```

---

## 10. Tratamento de Erros

### 10.1. Erros de Compilação

- Capturar stderr do javac
- Mostrar na Tab "Output do Utilizador" ou em Alert
- Destacar erros em vermelho

### 10.2. Erros de Execução

- Timeout após 5 segundos (evitar loops infinitos)
- Capturar exceções (stderr)
- Mostrar na Tab "Output do Utilizador"

### 10.3. Erros de Upload

- Validar extensões de ficheiros (.txt, .java)
- Mostrar Alert se ficheiro inválido

### 10.4. Erros de Sugestão

- Se não encontrar match (score muito baixo), mostrar:
  "Não foi possível encontrar sugestão contextual."

---

## 11. Extensões Futuras (Preparadas na Arquitetura)

1. **Múltiplos casos de teste:**
   - FileManager pode guardar múltiplos inputs/outputs
   - OutputComparator pode iterar sobre todos

2. **Syntax highlighting:**
   - Usar RichTextFX (já incluído) em vez de TextArea
   - Aplicar estilos CSS para keywords

3. **Histórico de sugestões:**
   - SuggestionEngine guarda lista de sugestões anteriores
   - Permite "desfazer sugestão"

4. **Diferentes linguagens:**
   - CodeExecutor pode ser estendido para Python, C++
   - Usar ProcessBuilder com diferentes compiladores/interpretadores

5. **Sistema de pontuação:**
   - Atribuir pontos por solução correta
   - Guardar em JSON ou ficheiro

---

## 12. Considerações de Performance

1. **Compilation Cache:**
   - Se código não mudou, não recompilar
   - Comparar hash do código

2. **Suggestion Cache:**
   - Se contexto não mudou, não recalcular
   - Cache de sugestões por linha

3. **Lazy Loading:**
   - Carregar solução oficial só quando necessário

4. **Thread Pooling:**
   - Executar compilação/execução em threads separadas
   - Evitar bloquear UI (usar Task<T> do JavaFX)

---

## 13. Testes Unitários

### 13.1. CodeExecutorTest
- Testar compilação de código válido
- Testar código com erros de sintaxe
- Testar timeout em loops infinitos

### 13.2. OutputComparatorTest
- Testar outputs idênticos
- Testar outputs diferentes
- Testar whitespace e case sensitivity

### 13.3. SuggestionEngineTest
- Testar matching contextual simples (for loop)
- Testar matching em diferentes blocos
- Testar quando não há match

---

## 14. Resumo de Responsabilidades

| Componente            | Responsabilidade Principal                          |
|-----------------------|-----------------------------------------------------|
| MainApp               | Entry point, inicialização JavaFX                   |
| Launcher              | Wrapper para JAR executável                         |
| UIController          | Coordenação de toda a UI                            |
| TabManager            | Gestão das 4 tabs fixas                             |
| SuggestionPopup       | Mostrar sugestões ao utilizador                     |
| FileManager           | Upload, save, read, cleanup de ficheiros            |
| CodeExecutor          | Compilar e executar código Java                     |
| OutputComparator      | Comparar outputs linha a linha                      |
| SuggestionEngine      | **Analisar contexto e sugerir linha equivalente**   |
| CodeAnalyzer          | Análise estática: tokens, blocos, estrutura         |
| FileUtils             | Utilitários gerais de ficheiros                     |
| ExecutionResult       | Modelo de resultado de compilação/execução          |
| ComparisonResult      | Modelo de resultado de comparação                   |
| CodeContext           | Modelo de contexto de código                        |

---

## 15. Diagrama de Sequência - Obter Sugestão

```
User                UIController         SuggestionEngine      CodeAnalyzer      FileManager
 |                       |                     |                    |                 |
 |--[Clica 💡]---------->|                     |                    |                 |
 |                       |--getUserCode()----->|                    |                 |
 |                       |                     |                    |                 |
 |                       |--getCursorLine()--->|                    |                 |
 |                       |                     |                    |                 |
 |                       |                     |--analyzeContext()-->                 |
 |                       |                     |                    |                 |
 |                       |                     |--extractTokens()--->                 |
 |                       |                     |<----tokens---------|                 |
 |                       |                     |                    |                 |
 |                       |                     |---------------readSolution()-------->|
 |                       |                     |<---------solution-------------------|
 |                       |                     |                    |                 |
 |                       |                     |--scoreLines()----->|                 |
 |                       |                     |<--scores-----------|                 |
 |                       |                     |                    |                 |
 |                       |<----suggestion------|                    |                 |
 |                       |                     |                    |                 |
 |                       |--showPopup()------->|                    |                 |
 |<--[Popup Mostrado]----|                     |                    |                 |
```

---

## Conclusão

Esta arquitetura fornece uma base sólida para um mini judge de programação local com JavaFX. Os componentes estão bem separados (separation of concerns), facilitando manutenção e extensões futuras.

**Pontos-chave:**
- ✅ Interface JavaFX com SplitPane e Tabs fixas
- ✅ Upload e preview de enunciados
- ✅ Editor de código integrado
- ✅ Compilação e execução via ProcessBuilder
- ✅ Comparação de outputs linha a linha
- ✅ **Motor de sugestões inteligente baseado em análise contextual**
- ✅ Gestão de ficheiros temporários
- ✅ Pronto para empacotamento em JAR e .exe
- ✅ Sem base de dados, tudo em ficheiros

O componente mais complexo é o **SuggestionEngine**, que usa heurísticas de matching contextual (tokens, blocos, indentação, keywords) em vez de depender de números de linha. Isto torna as sugestões mais robustas e úteis para o utilizador.

