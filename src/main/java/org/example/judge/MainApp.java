package org.example.judge;

import org.example.judge.core.FileManager;
import org.example.judge.ui.UIController;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp - Entry point da aplicação JavaFX.
 *
 * Responsabilidades:
 * - Inicializar a aplicação JavaFX
 * - Criar e configurar o palco principal (Stage)
 * - Instanciar o UIController
 * - Realizar cleanup ao fechar
 *
 * Fluxo:
 * 1. start() - Inicializa UI
 * 2. Utilizador interage com a aplicação
 * 3. stop() - Cleanup ao fechar
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class MainApp extends Application {

    private UIController uiController;
    private FileManager fileManager;

    /**
     * Inicializa a aplicação JavaFX e cria a interface principal.
     *
     * @param primaryStage Palco principal da aplicação
     * @throws Exception Se houver erro na inicialização
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializar FileManager
        fileManager = new FileManager();

        // Criar UIController
        uiController = new UIController(fileManager);

        // Criar Scene
        Scene scene = new Scene(uiController.getRoot(), 1200, 800);

        // Carregar CSS (se existir)
        try {
            String css = getClass().getResource("/styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("CSS não encontrado, usando estilos padrão.");
        }

        // Configurar primaryStage
        primaryStage.setTitle("Mini Judge - Sistema de Avaliação Local");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setMaximized(true);  // Iniciar maximizado

        // Mostrar stage
        primaryStage.show();

        System.out.println("Aplicação iniciada!");
    }

    /**
     * Cleanup ao fechar a aplicação.
     * Remove ficheiros temporários e liberta recursos.
     */
    @Override
    public void stop() {
        if (fileManager != null) {
            fileManager.cleanup();
        }

        System.out.println("Aplicação fechada. Cleanup executado.");
    }

    /**
     * Entry point da aplicação.
     *
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}

