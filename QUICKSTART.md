# 🚀 Quick Start Guide - Mini Judge System

## Passo a Passo para Começar

### 1️⃣ Pré-requisitos
- ✅ Java 17 ou superior instalado
- ✅ Maven instalado (ou use o IDE com suporte Maven)
- ✅ IDE recomendado: IntelliJ IDEA, Eclipse ou VS Code

### 2️⃣ Executar a Aplicação

#### Opção A: Usando Maven (Recomendado)
```bash
cd "C:\Users\utilizador 1\Downloads\LocalJavaLeetCode"
mvn javafx:run
```

#### Opção B: Usando IDE
1. Abrir o projeto no IntelliJ IDEA
2. Esperar o Maven carregar as dependências
3. Localizar a classe `MainApp.java`
4. Clicar com botão direito → Run 'MainApp.main()'

#### Opção C: Gerar e Executar JAR
```bash
mvn clean package
java -jar target/LocalJavaLeetCode-1.0-SNAPSHOT.jar
```

### 3️⃣ Usar a Aplicação

#### Passo 1: Carregar Enunciado
1. Clique no botão **"📄 Upload Enunciado"**
2. Selecione um ficheiro `.txt` com o enunciado do problema
3. O enunciado aparecerá no painel da esquerda

#### Passo 2: Carregar Solução Oficial (Output Esperado)
1. Clique no botão **"📝 Upload Solução"**
2. Selecione um ficheiro `.txt` com o output esperado
3. O output aparecerá na tab "Output Esperado"

#### Passo 3: Escrever Código
1. Vá para a tab **"Código do Utilizador"**
2. Escreva o seu programa Java
3. Exemplo:
```java
public class UserCode {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i <= 10; i++) {
            sum += i;
        }
        System.out.println("Sum: " + sum);
    }
}
```

#### Passo 4: Executar o Código
1. Clique no botão **"▶️ Executar"**
2. O sistema irá:
   - ✅ Compilar o código
   - ✅ Executar o programa
   - ✅ Capturar o output
   - ✅ Comparar com a solução oficial
   - ✅ Mostrar resultado na tab "Resultado da Comparação"

#### Passo 5: Obter Sugestões (Opcional)
1. Posicione o cursor onde precisa de ajuda
2. Clique no botão **"💡 Sugestão"**
3. Um popup aparecerá com uma sugestão
4. Clique em **"Inserir"** para adicionar ao código ou **"Ignorar"** para fechar

## 📝 Exemplo Completo

### Ficheiro: enunciado.txt
```
Problema: Soma de 1 a N

Escreva um programa que calcula a soma dos números de 1 até N.

Input: N = 10
Output esperado: Sum: 55
```

### Ficheiro: solucao.txt
```
Sum: 55
```

### Código do Utilizador
```java
public class UserCode {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i <= 10; i++) {
            sum += i;
        }
        System.out.println("Sum: " + sum);
    }
}
```

### Resultado Esperado
```
✓ OUTPUTS IDÊNTICOS!
Similaridade: 100%
Todas as linhas coincidem.
```

## 🔧 Resolução de Problemas

### Erro: "javac not found"
**Solução**: Certifique-se que o JDK está instalado e no PATH
```bash
java -version
javac -version
```

### Erro: "TIMEOUT: Execução excedeu 5 segundos"
**Causa**: Código tem loop infinito ou demora muito tempo
**Solução**: Reveja o código e corrija loops

### Erro de Compilação
**Causa**: Erro de sintaxe no código
**Solução**: Leia a mensagem de erro na janela de erro e corrija

### Aplicação Não Inicia
**Solução 1**: Verificar JavaFX
```bash
mvn javafx:run
```

**Solução 2**: Limpar e recompilar
```bash
mvn clean install
mvn javafx:run
```

## 💡 Dicas de Uso

### Como Funciona o Sistema de Sugestões

1. **Escreva algumas linhas de código**
   ```java
   int sum = 0;
   for (int i = 0; i < 10; i++) {
       |← Cursor aqui
   ```

2. **Clique em 💡 Sugestão**

3. **O sistema analisa**:
   - Tokens: `[int, sum, for, i, 0, 10]`
   - Bloco: `FOR_LOOP`
   - Indentação: `2 espaços`

4. **Sugere a próxima linha**:
   ```java
   sum += i;
   ```

### Atalhos Úteis
- **Executar**: Alt + E (após configurar)
- **Sugestão**: Alt + S (após configurar)
- **Tab entre tabs**: Ctrl + Tab

### Boas Práticas
1. ✅ Sempre carregue o enunciado primeiro
2. ✅ Carregue a solução oficial antes de executar
3. ✅ Use o sistema de sugestões quando estiver preso
4. ✅ Leia a comparação detalhada para entender erros

## 📚 Recursos Adicionais

- **ARCHITECTURE.md**: Arquitetura completa do sistema
- **IMPLEMENTATION_STATUS.md**: Estado de implementação
- **IMPLEMENTATION.md**: Detalhes de implementação
- **README.md**: Documentação geral

## 🆘 Suporte

### Ficheiros de Log
Os ficheiros temporários são criados em:
- Windows: `C:\Users\{user}\AppData\Local\Temp\judge-{timestamp}\`
- Linux/Mac: `/tmp/judge-{timestamp}/`

### Estrutura de Ficheiros Temporários
```
/tmp/judge-20260213-143025/
├── statement.txt          # Enunciado
├── solution.txt           # Solução oficial
├── UserCode.java          # Seu código
├── UserCode.class         # Código compilado
└── user-output.txt        # Output do seu programa
```

## ✅ Checklist de Verificação

Antes de executar, certifique-se:
- [ ] Java 17+ instalado
- [ ] Maven instalado (ou IDE com Maven)
- [ ] Projeto descarregado e extraído
- [ ] Todas as dependências carregadas (`mvn clean install`)
- [ ] Porta 8080 não está em uso (se aplicável)

---

**Pronto para começar!** Execute `mvn javafx:run` e divirta-se! 🎉

