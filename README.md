# Mini Judge de Programação Java
Sistema local de avaliação automática de código Java com interface gráfica JavaFX.
## 📋 Descrição
Este projeto implementa um mini judge de programação que permite:
- Carregar enunciados de problemas
- Escrever código Java num editor integrado
- Compilar e executar o código automaticamente
- Comparar output com solução oficial
- Obter sugestões inteligentes linha a linha baseadas em análise contextual
## 🏗️ Arquitetura
Consulte [ARCHITECTURE.md](ARCHITECTURE.md) para a documentação completa da arquitetura.
## 🔧 Compilação e Execução
### Requisitos
- JDK 23 ou superior
- Maven 3.6+
### Executar com Maven
```powershell
mvn javafx:run
```
### Gerar JAR Executável
```powershell
mvn clean package
```
### Executar JAR
```powershell
java -jar target/LocalJavaLeetCode-1.0-SNAPSHOT-shaded.jar
```
## 📚 Documentação
Ver [ARCHITECTURE.md](ARCHITECTURE.md) para detalhes completos sobre:
- Estrutura de classes
- Fluxo de execução
- Algoritmo de sugestões contextuais
- Diagramas e especificações
