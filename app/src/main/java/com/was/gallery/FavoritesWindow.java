package com.was.gallery;

import com.was.data.FavoritesRepository;
import com.was.model.CelestialBody;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;

/** A small JavaFX window for browsing and removing saved celestial bodies. */
public class FavoritesWindow {
    public void show(Stage owner, List<CelestialBody> catalog, FavoritesRepository favoritesRepository) {
        Stage window = new Stage();
        window.initOwner(owner);
        window.initModality(Modality.NONE);
        window.setTitle("내가 모은 천체");

        ListView<CelestialBody> listView = new ListView<>(
                FXCollections.observableArrayList(favoritesRepository.findFavorites(catalog)));
        listView.setPrefWidth(230);
        listView.setPlaceholder(new Label("아직 모은 천체가 없습니다."));
        listView.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CelestialBody body, boolean empty) {
                super.updateItem(body, empty);
                setText(empty || body == null ? null : body.getDisplayName());
            }
        });

        Label title = new Label("천체를 선택하세요");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f3f4f8;");
        Label constellation = new Label();
        constellation.setStyle("-fx-text-fill: #9ca6bd;");
        Label description = new Label();
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #d4d8e3; -fx-font-size: 14px; -fx-line-spacing: 5px;");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(420);
        imageView.setFitHeight(280);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Button removeButton = new Button("즐겨찾기 해제");
        removeButton.setDisable(true);
        VBox details = new VBox(14, imageView, title, constellation, description, removeButton);
        details.setPadding(new Insets(8, 4, 8, 8));
        details.setStyle("-fx-background-color: #0b0e16;");
        HBox.setHgrow(details, Priority.ALWAYS);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldBody, body) -> {
            if (body == null) {
                title.setText("천체를 선택하세요");
                constellation.setText("");
                description.setText("");
                imageView.setImage(null);
                removeButton.setDisable(true);
                return;
            }
            title.setText(body.getDisplayName());
            constellation.setText(body.getConstellation() == null ? "" : body.getConstellation());
            description.setText(body.getDescription() == null ? "" : body.getDescription());
            URL imageUrl = FavoritesWindow.class.getResource(body.getImageResource());
            imageView.setImage(imageUrl == null ? null : new Image(imageUrl.toExternalForm(), true));
            removeButton.setDisable(false);
        });

        removeButton.setOnAction(event -> {
            CelestialBody selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            favoritesRepository.removeFavorite(selected.getId());
            listView.getItems().remove(selected);
            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().selectFirst();
            } else {
                listView.getSelectionModel().clearSelection();
            }
        });

        Label heading = new Label("내가 모은 천체");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f3f4f8;");
        BorderPane root = new BorderPane(new HBox(20, listView, details));
        root.setTop(heading);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #0b0e16;");

        window.setScene(new Scene(root, 820, 560));
        if (!listView.getItems().isEmpty()) {
            listView.getSelectionModel().selectFirst();
        }
        window.show();
    }
}
