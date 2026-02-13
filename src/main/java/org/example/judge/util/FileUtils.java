package org.example.judge.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * FileUtils - Utilitários gerais para manipulação de ficheiros.
 *
 * Responsabilidades:
 * - Ler ficheiros
 * - Escrever ficheiros
 * - Copiar ficheiros
 * - Remover diretórios recursivamente
 * - Criar diretórios temporários
 * - Outras operações de I/O
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class FileUtils {

    /**
     * Construtor privado (classe utilitária).
     */
    private FileUtils() {
    }

    /**
     * Lê todo o conteúdo de um ficheiro.
     *
     * @param path Caminho do ficheiro
     * @return Conteúdo do ficheiro
     * @throws IOException Se houver erro na leitura
     */
    public static String readFile(Path path) throws IOException {
        return Files.readString(path);
    }

    /**
     * Escreve conteúdo num ficheiro.
     *
     * @param path Caminho do ficheiro
     * @param content Conteúdo a escrever
     * @throws IOException Se houver erro na escrita
     */
    public static void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    /**
     * Copia um ficheiro.
     *
     * @param source Ficheiro origem
     * @param destination Ficheiro destino
     * @throws IOException Se houver erro na cópia
     */
    public static void copyFile(Path source, Path destination) throws IOException {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Remove um diretório e todo o seu conteúdo recursivamente.
     *
     * @param directory Diretório a remover
     * @throws IOException Se houver erro na remoção
     */
    public static void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Erro ao remover: " + path + " - " + e.getMessage());
                    }
                });
        }
    }

    /**
     * Cria um diretório temporário com prefixo.
     *
     * @param prefix Prefixo do nome do diretório
     * @return Path do diretório criado
     * @throws IOException Se houver erro na criação
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    /**
     * Verifica se um ficheiro existe.
     *
     * @param path Caminho do ficheiro
     * @return true se existe
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Verifica se um caminho é um diretório.
     *
     * @param path Caminho a verificar
     * @return true se é diretório
     */
    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * Obtém a extensão de um ficheiro.
     *
     * @param filename Nome do ficheiro
     * @return Extensão (sem o ponto) ou empty string
     */
    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Cria diretórios (incluindo pais) se não existirem.
     *
     * @param path Caminho do diretório
     * @throws IOException Se houver erro na criação
     */
    public static void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }
}

