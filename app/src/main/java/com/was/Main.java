package com.was;

import com.was.data.CelestialBodyRepository;
import com.was.data.FavoritesRepository;
import com.was.gallery.LaunchCelestialService;
import com.was.gallery.FavoritesWindow;
import com.was.model.CelestialBody;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.net.URL;

public class Main extends Application {
    private static final double MIN_WIDTH = 50.0;
    private static final double MIN_HEIGHT = 50.0;
    private static final double INITIAL_WIDTH = 300.0;
    private static final double INITIAL_HEIGHT = 200.0;
    private static final double RESIZE_MARGIN = 10.0;

    @Override
    public void start(Stage stage) {
        List<CelestialBody> catalog = new CelestialBodyRepository().findAll();
        CelestialBody today = new LaunchCelestialService(catalog).getRandomForNextLaunch();
        FavoritesRepository favorites = new FavoritesRepository();

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double maxWidth = screen.getWidth() / 2.0;
        double maxHeight = screen.getHeight() / 2.0;

        String petResource = today.getAnimationResource();
        if (petResource == null || petResource.isBlank()) {
            petResource = today.getImageResource();
        }
        URL petImageUrl = Main.class.getResource(petResource);
        if (petImageUrl == null) {
            throw new IllegalStateException("천체 이미지를 찾을 수 없습니다: " + petResource);
        }
        Image petImage = new Image(petImageUrl.toExternalForm());
        if (petImage.isError() || petImage.getWidth() <= 0 || petImage.getHeight() <= 0) {
            throw new IllegalStateException("천체 이미지를 불러오지 못했습니다: " + petResource);
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMaxWidth(maxWidth);
        stage.setMaxHeight(maxHeight);

        ImageView imageView = new ImageView(petImage);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(false);
        imageView.fitWidthProperty().bind(stage.widthProperty().subtract(2.0));
        imageView.fitHeightProperty().bind(stage.heightProperty().subtract(2.0));

        StackPane imageArea = new StackPane(imageView);
        imageArea.setStyle("-fx-background-color: #02030a;");
        StackPane root = new StackPane(imageArea);
        root.setStyle("-fx-background-color: #02030a; -fx-border-color: #59627a; -fx-border-width: 1;");

        DragState drag = new DragState();
        root.setOnMouseMoved(event -> {
            Point2D point = root.sceneToLocal(event.getSceneX(), event.getSceneY());
            root.setCursor(cursorFor(edgeAt(point.getX(), point.getY(), root.getWidth(), root.getHeight())));
        });
        root.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) return;
            Point2D point = root.sceneToLocal(event.getSceneX(), event.getSceneY());
            drag.resizeEdge = edgeAt(point.getX(), point.getY(), root.getWidth(), root.getHeight());
            drag.moving = drag.resizeEdge == null;
            drag.moved = false;
            drag.startMouseX = event.getScreenX();
            drag.startMouseY = event.getScreenY();
            drag.startX = stage.getX();
            drag.startY = stage.getY();
            drag.startWidth = stage.getWidth();
            drag.startHeight = stage.getHeight();
            event.consume();
        });
        root.setOnMouseDragged(event -> {
            double dx = event.getScreenX() - drag.startMouseX;
            double dy = event.getScreenY() - drag.startMouseY;
            if (Math.abs(dx) > 3 || Math.abs(dy) > 3) drag.moved = true;
            String edge = drag.resizeEdge;
            if (edge == null && drag.moving) {
                stage.setX(drag.startX + dx);
                stage.setY(drag.startY + dy);
            } else if (edge != null) {
                boolean fromLeft = edge.contains("left");
                boolean fromRight = edge.contains("right");
                boolean fromTop = edge.contains("top");
                boolean fromBottom = edge.contains("bottom");
                double newWidth = drag.startWidth;
                double newHeight = drag.startHeight;
                if (fromLeft) newWidth = clamp(drag.startWidth - dx, MIN_WIDTH, maxWidth);
                else if (fromRight) newWidth = clamp(drag.startWidth + dx, MIN_WIDTH, maxWidth);
                if (fromTop) newHeight = clamp(drag.startHeight - dy, MIN_HEIGHT, maxHeight);
                else if (fromBottom) newHeight = clamp(drag.startHeight + dy, MIN_HEIGHT, maxHeight);
                stage.setWidth(newWidth);
                stage.setHeight(newHeight);
                if (fromLeft) stage.setX(drag.startX + drag.startWidth - newWidth);
                if (fromTop) stage.setY(drag.startY + drag.startHeight - newHeight);
            }
            event.consume();
        });
        root.setOnMouseReleased(event -> {
            drag.moving = false;
            drag.resizeEdge = null;
        });
        root.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !drag.moved) {
                openWebsite(today.getId());
                event.consume();
            }
            drag.moved = false;
        });

        MenuItem favoriteItem = new MenuItem();
        Runnable updateFavoriteLabel = () -> favoriteItem.setText(
                favorites.isFavorite(today.getId()) ? "즐겨찾기 해제" : "즐겨찾기에 추가");
        updateFavoriteLabel.run();
        favoriteItem.setOnAction(event -> {
            try {
                favorites.toggleFavorite(today.getId());
                updateFavoriteLabel.run();
            } catch (RuntimeException exception) {
                showError("즐겨찾기 저장 실패", exception.getMessage());
            }
        });

        MenuItem collectionItem = new MenuItem("즐겨찾기 모아보기");
        collectionItem.setOnAction(event -> new FavoritesWindow().show(stage, catalog, favorites));
        MenuItem websiteItem = new MenuItem("천체 설명 열기");
        websiteItem.setOnAction(event -> openWebsite(today.getId()));
        MenuItem exitItem = new MenuItem("프로그램 종료");
        exitItem.setOnAction(event -> stage.close());
        ContextMenu contextMenu = new ContextMenu(
                favoriteItem, collectionItem, new SeparatorMenuItem(), websiteItem, exitItem);
        root.setOnContextMenuRequested(event -> contextMenu.show(root, event.getScreenX(), event.getScreenY()));

        stage.setScene(new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT));
        stage.setTitle(today.getDisplayName());
        stage.show();
    }

    private void openWebsite(String celestialId) {
        Path page = findWebsitePage(celestialId);
        if (page == null) {
            showError("페이지를 찾을 수 없습니다", "website/rosette.html 파일이 없습니다.");
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(page.toUri());
            } else {
                getHostServices().showDocument(page.toUri().toString());
            }
        } catch (IOException | UnsupportedOperationException | SecurityException exception) {
            showError("페이지를 열 수 없습니다", exception.getMessage());
        }
    }

    private static Path findWebsitePage(String celestialId) {
        Path directory = Paths.get("").toAbsolutePath().normalize();
        while (directory != null) {
            Path websiteDirectory = directory.resolve("website");
            String pageName = "rosette-nebula".equals(celestialId) ? "rosette.html" : celestialId + ".html";
            Path objectPage = websiteDirectory.resolve(pageName);
            if (Files.isRegularFile(objectPage)) return objectPage;
            Path defaultPage = websiteDirectory.resolve("rosette.html");
            if (Files.isRegularFile(defaultPage)) return defaultPage;
            directory = directory.getParent();
        }
        return null;
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "알 수 없는 오류가 발생했습니다." : message);
        alert.showAndWait();
    }

    private static String edgeAt(double x, double y, double width, double height) {
        boolean left = x <= RESIZE_MARGIN;
        boolean right = x >= width - RESIZE_MARGIN;
        boolean top = y <= RESIZE_MARGIN;
        boolean bottom = y >= height - RESIZE_MARGIN;
        if (top && left) return "top-left";
        if (top && right) return "top-right";
        if (bottom && left) return "bottom-left";
        if (bottom && right) return "bottom-right";
        if (left) return "left";
        if (right) return "right";
        if (top) return "top";
        if (bottom) return "bottom";
        return null;
    }

    private static Cursor cursorFor(String edge) {
        if (edge == null) return Cursor.DEFAULT;
        switch (edge) {
            case "top-left": return Cursor.NW_RESIZE;
            case "top-right": return Cursor.NE_RESIZE;
            case "bottom-left": return Cursor.SW_RESIZE;
            case "bottom-right": return Cursor.SE_RESIZE;
            case "left":
            case "right": return Cursor.H_RESIZE;
            case "top":
            case "bottom": return Cursor.V_RESIZE;
            default: return Cursor.DEFAULT;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private static final class DragState {
        private boolean moving;
        private boolean moved;
        private String resizeEdge;
        private double startMouseX;
        private double startMouseY;
        private double startX;
        private double startY;
        private double startWidth;
        private double startHeight;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

