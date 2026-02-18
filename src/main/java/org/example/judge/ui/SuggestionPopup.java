package org.example.judge.ui;

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * SuggestionPopup - Popup para mostrar sugestões de código.
 *
 * Responsabilidades:
 * - Criar popup estilizado com sugestão
 * - Mostrar nas coordenadas do cursor
 * - Botões para inserir ou ignorar sugestão
 * - Auto-hide quando clica fora
 *
 * Aparência:
 * ┌────────────────────────────────┐
 * │ 💡 Sugestão:                   │
 * │                                │
 * │   sum += i;                    │
 * │                                │
 * │ [Inserir] [Ignorar]            │
 * └────────────────────────────────┘
 *
 * @author Mini Judge System
 * @version 1.0
 */
public class SuggestionPopup {

    private static final double POPUP_WIDTH = 420;
    private static final double POPUP_HEIGHT = 170;
    private static final double WINDOW_MARGIN = 12;

    private Popup popup;
    private Label suggestionLabel;
    private Button insertButton;
    private Button ignoreButton;

    // Callback para inserir sugestão
    private Runnable onInsert;

    /**
     * Construtor - Cria o popup.
     */
    public SuggestionPopup() {
        createPopup();
    }

    /**
     * Cria e configura o popup.
     */
    private void createPopup() {
        popup = new Popup();
        popup.setAutoHide(true);

        // Container principal
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setPrefSize(POPUP_WIDTH, POPUP_HEIGHT);
        container.setStyle("-fx-background-color: white; " +
                          "-fx-border-color: #cccccc; " +
                          "-fx-border-width: 2; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");

        // Título
        Label titleLabel = new Label("💡 Sugestão:");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Label para mostrar sugestão
        suggestionLabel = new Label();
        suggestionLabel.setStyle("-fx-font-family: 'Courier New', monospace; " +
                                "-fx-font-size: 13px; " +
                                "-fx-background-color: #f0f0f0; " +
                                "-fx-padding: 10; " +
                                "-fx-border-color: #999999; " +
                                "-fx-border-width: 1;");
        suggestionLabel.setWrapText(false);

        // Botões
        insertButton = new Button("Inserir");
        insertButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        insertButton.setOnAction(e -> handleInsert());

        ignoreButton = new Button("Ignorar");
        ignoreButton.setOnAction(e -> handleIgnore());

        HBox buttonBox = new HBox(10, insertButton, ignoreButton);
        buttonBox.setStyle("-fx-alignment: center;");

        // Adicionar tudo ao container
        container.getChildren().addAll(titleLabel, suggestionLabel, buttonBox);

        popup.getContent().add(container);
    }

    /**
     * Mostra o popup com a sugestão nas coordenadas especificadas.
     *
     * @param suggestion Sugestão a mostrar
     * @param ownerWindow Janela proprietária do popup
     * @param x Coordenada X na tela
     * @param y Coordenada Y na tela
     * @param onInsert Callback executado ao inserir
     */
    public void show(String suggestion, Window ownerWindow, double x, double y, Runnable onInsert) {
        this.onInsert = onInsert;
        suggestionLabel.setText(suggestion);

        if (ownerWindow == null) {
            return;
        }

        if (popup.isShowing()) {
            popup.hide();
        }

        Rectangle2D ownerBounds = new Rectangle2D(
            ownerWindow.getX(),
            ownerWindow.getY(),
            ownerWindow.getWidth(),
            ownerWindow.getHeight()
        );

        Rectangle2D visualBounds = Screen.getScreensForRectangle(ownerBounds).isEmpty()
            ? Screen.getPrimary().getVisualBounds()
            : Screen.getScreensForRectangle(ownerBounds).get(0).getVisualBounds();

        double minX = Math.max(visualBounds.getMinX() + WINDOW_MARGIN, ownerWindow.getX() + WINDOW_MARGIN);
        double maxX = Math.min(
            visualBounds.getMaxX() - WINDOW_MARGIN - POPUP_WIDTH,
            ownerWindow.getX() + ownerWindow.getWidth() - WINDOW_MARGIN - POPUP_WIDTH
        );

        double minY = Math.max(visualBounds.getMinY() + WINDOW_MARGIN, ownerWindow.getY() + WINDOW_MARGIN);
        double maxY = Math.min(
            visualBounds.getMaxY() - WINDOW_MARGIN - POPUP_HEIGHT,
            ownerWindow.getY() + ownerWindow.getHeight() - WINDOW_MARGIN - POPUP_HEIGHT
        );

        double targetX = clamp(x, minX, maxX);
        double targetY = clamp(y, minY, maxY);

        popup.show(ownerWindow, targetX, targetY);
    }

    private double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }

        return Math.max(min, Math.min(value, max));
    }

    /**
     * Esconde o popup.
     */
    public void hide() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }

    /**
     * Handler do botão "Inserir".
     */
    private void handleInsert() {
        if (onInsert != null) {
            onInsert.run();
        }
        hide();
    }

    /**
     * Handler do botão "Ignorar".
     */
    private void handleIgnore() {
        hide();
    }

    /**
     * Verifica se o popup está visível.
     *
     * @return true se está a mostrar
     */
    public boolean isShowing() {
        return popup != null && popup.isShowing();
    }
}

