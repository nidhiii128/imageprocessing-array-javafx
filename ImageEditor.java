import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageEditor extends Application {

    private ImageView imageView;
    private BufferedImage currentImage;
    private BufferedImage originalImage;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Professional Image Editor");

        // Main layout using BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2e2e2e;");

        // Image area in the center
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);
        
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPrefSize(800, 600);
        imageContainer.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444; -fx-border-width: 2px; -fx-padding: 10px;");
        root.setCenter(imageContainer);
        BorderPane.setMargin(imageContainer, new Insets(20));

        // Create a VBox for the toolbar at the top
        VBox toolbar = new VBox(10);
        toolbar.setPadding(new Insets(15, 10, 5, 10));
        toolbar.setAlignment(Pos.TOP_LEFT);
        toolbar.setStyle("-fx-background-color: #3e3e3e;");

        // Create buttons with more professional styling
        Button loadBtn = new Button("Load Image");
        Button originalBtn = new Button("Original");
        Button invertBtn = new Button("Invert Colors");
        Button grayBtn = new Button("Grayscale");
        Button saveBtn = new Button("Save Image");
        
        // Disable the filter and save buttons initially
        originalBtn.setDisable(true);
        invertBtn.setDisable(true);
        grayBtn.setDisable(true);
        saveBtn.setDisable(true);

        // Apply a consistent, professional style to all buttons
        String buttonStyle = "-fx-background-color: #555; " +
                             "-fx-text-fill: white; " +
                             "-fx-font-size: 14px; " +
                             "-fx-padding: 8 16 8 16; " +
                             "-fx-background-radius: 5px; " +
                             "-fx-border-color: #888; " +
                             "-fx-border-radius: 5px;";
        
        String buttonHoverStyle = "-fx-background-color: #777;";

        loadBtn.setStyle(buttonStyle);
        originalBtn.setStyle(buttonStyle);
        invertBtn.setStyle(buttonStyle);
        grayBtn.setStyle(buttonStyle);
        saveBtn.setStyle(buttonStyle);

        // Add hover effects for a better user experience
        loadBtn.setOnMouseEntered(e -> loadBtn.setStyle(buttonHoverStyle + buttonStyle));
        loadBtn.setOnMouseExited(e -> loadBtn.setStyle(buttonStyle));
        originalBtn.setOnMouseEntered(e -> originalBtn.setStyle(buttonHoverStyle + buttonStyle));
        originalBtn.setOnMouseExited(e -> originalBtn.setStyle(buttonStyle));
        invertBtn.setOnMouseEntered(e -> invertBtn.setStyle(buttonHoverStyle + buttonStyle));
        invertBtn.setOnMouseExited(e -> invertBtn.setStyle(buttonStyle));
        grayBtn.setOnMouseEntered(e -> grayBtn.setStyle(buttonHoverStyle + buttonStyle));
        grayBtn.setOnMouseExited(e -> grayBtn.setStyle(buttonStyle));
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(buttonHoverStyle + buttonStyle));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(buttonStyle));

        // Group the buttons in an HBox for horizontal arrangement
        HBox buttonBox = new HBox(10, loadBtn, originalBtn, invertBtn, grayBtn, saveBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        toolbar.getChildren().add(buttonBox);

        root.setTop(toolbar);
        
        // Status bar at the bottom
        statusLabel = new Label("Ready to edit...");
        statusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
        VBox statusBar = new VBox(statusLabel);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #3e3e3e;");
        root.setBottom(statusBar);

        // Actions
        loadBtn.setOnAction(e -> loadImage(primaryStage));
        originalBtn.setOnAction(e -> applyOriginal());
        invertBtn.setOnAction(e -> applyInvert());
        grayBtn.setOnAction(e -> applyGrayscale());
        saveBtn.setOnAction(e -> saveImage(primaryStage));
        
        // Update button state based on image loading
        imageView.imageProperty().addListener((obs, oldImage, newImage) -> {
            boolean hasImage = newImage != null;
            originalBtn.setDisable(!hasImage);
            invertBtn.setDisable(!hasImage);
            grayBtn.setDisable(!hasImage);
            saveBtn.setDisable(!hasImage);
        });

        // Scene
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                BufferedImage loadedImage = ImageIO.read(file);
                if (loadedImage == null) {
                    statusLabel.setText("Error: Could not read image file.");
                    return;
                }
                
                // Store a deep copy of the original image
                ColorModel cm = loadedImage.getColorModel();
                boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                WritableRaster raster = loadedImage.copyData(null);
                originalImage = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
                
                // Set the current image and display it (also a deep copy)
                WritableRaster raster2 = loadedImage.copyData(null);
                currentImage = new BufferedImage(cm, raster2, isAlphaPremultiplied, null);
                
                imageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
                statusLabel.setText("Image loaded successfully: " + file.getName());
            } catch (Exception ex) {
                statusLabel.setText("Error loading image: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void applyOriginal() {
        if (originalImage == null) {
            statusLabel.setText("No original image to restore.");
            return;
        }
        
        // Make a new copy from the unmodified original image
        ColorModel cm = originalImage.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = originalImage.copyData(null);
        currentImage = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        
        imageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
        statusLabel.setText("Image restored to original.");
    }

    private void applyInvert() {
        if (currentImage == null) {
            statusLabel.setText("No image loaded to invert.");
            return;
        }

        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = currentImage.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = 255 - ((rgb >> 16) & 0xff);
                int g = 255 - ((rgb >> 8) & 0xff);
                int b = 255 - (rgb & 0xff);
                int newRgb = (a << 24) | (r << 16) | (g << 8) | b;
                currentImage.setRGB(x, y, newRgb);
            }
        }
        imageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
        statusLabel.setText("Invert colors applied.");
    }

    private void applyGrayscale() {
        if (currentImage == null) {
            statusLabel.setText("No image loaded to apply grayscale.");
            return;
        }

        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = currentImage.getRGB(x, y);

                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int newRgb = (a << 24) | (gray << 16) | (gray << 8) | gray;
                currentImage.setRGB(x, y, newRgb);
            }
        }
        imageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
        statusLabel.setText("Grayscale filter applied.");
    }

    private void saveImage(Stage stage) {
        if (currentImage == null) {
            statusLabel.setText("No image to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(currentImage, "png", file);
                statusLabel.setText("Image saved successfully to " + file.getAbsolutePath());
            } catch (Exception ex) {
                statusLabel.setText("Error saving image: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


//command to compile and run the program 

/*
C:\Users\irl dsa>javac --module-path "C:\Users\NIDHI\Downloads\openjfx-24.0.2_windows-x64_bin-sdk\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing ImageEditor.java

C:\Users\irl dsa>java --module-path "C:\Users\NIDHI\Downloads\openjfx-24.0.2_windows-x64_bin-sdk\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing ImageEditor
*/
