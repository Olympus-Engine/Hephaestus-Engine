// ============================================================================
// FILE: fr/mrqsdf/jfx/module/InfoModule.java
// ============================================================================
package fr.mrqsdf.jfx.module;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class InfoModule extends VBox {
    private final ImageView iconImageView;
    private final Label amountLabel;
    private final String materialId;

    public InfoModule(ImageView iconImageView, Label amountLabel, String materialId) {
        this.iconImageView = iconImageView;
        this.amountLabel = amountLabel;
        this.materialId = materialId;

        this.getChildren().addAll(iconImageView, amountLabel);
        this.setSpacing(5);

        this.setStyle("""
            -fx-padding: 6;
            -fx-background-color: #252525;
            -fx-background-radius: 8;
            """);

        this.amountLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
    }

    public void setAmount(int amount) {
        this.amountLabel.setText(String.valueOf(amount));
    }

    public ImageView getIconImageView() {
        return iconImageView;
    }

    public Label getAmountLabel() {
        return amountLabel;
    }

    public String getMaterialId() {
        return materialId;
    }
}
