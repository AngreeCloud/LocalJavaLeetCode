package org.example.judge;

import org.example.judge.core.CodeExecutor;
import org.example.judge.core.FileManager;
import org.example.judge.model.ExecutionResult;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para CodeExecutor.
 *
 * Testa:
 * - Compilação de código válido
 * - Compilação de código com erros
 * - Execução de código compilado
 * - Timeout em loops infinitos
 * - Captura de output e erros
 */
class CodeExecutorTest {

    private FileManager fileManager;
    private CodeExecutor codeExecutor;

    @BeforeEach
    void setUp() throws IOException {
        fileManager = new FileManager();
        codeExecutor = new CodeExecutor(fileManager);
    }

    @AfterEach
    void tearDown() {
        if (fileManager != null) {
            fileManager.cleanup();
        }
    }

    @Test
    @DisplayName("Deve compilar código Java válido com sucesso")
    void testCompileValidCode() {
        // TODO: Implementar
        // 1. Criar código Java válido simples
        // 2. Guardar com fileManager
        // 3. Compilar
        // 4. Verificar que result.isSuccess() é true
        // 5. Verificar que ficheiro .class foi criado
    }

    @Test
    @DisplayName("Deve falhar ao compilar código com erros de sintaxe")
    void testCompileInvalidCode() {
        // TODO: Implementar
        // 1. Criar código com erro de sintaxe
        // 2. Tentar compilar
        // 3. Verificar que result.isSuccess() é false
        // 4. Verificar que result.hasErrors() é true
    }

    @Test
    @DisplayName("Deve executar código e capturar output")
    void testExecuteCode() {
        // TODO: Implementar
        // 1. Compilar código que imprime "Hello World"
        // 2. Executar
        // 3. Verificar que result.isSuccess() é true
        // 4. Verificar que result.getOutput() contém "Hello World"
    }

    @Test
    @DisplayName("Deve aplicar timeout em loop infinito")
    void testExecutionTimeout() {
        // TODO: Implementar
        // 1. Criar código com while(true) {}
        // 2. Executar
        // 3. Verificar que termina em ~5 segundos
        // 4. Verificar que result.isSuccess() é false
    }

    @Test
    @DisplayName("Deve capturar erros de runtime")
    void testRuntimeError() {
        // TODO: Implementar
        // 1. Criar código que lança exceção (ex: divisão por zero)
        // 2. Executar
        // 3. Verificar que result.hasErrors() é true
    }
}

