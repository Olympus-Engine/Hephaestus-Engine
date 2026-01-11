// ============================================================================
// FILE: fr/mrqsdf/jfx/module/MaterialModule.java
// (CHANGÉ en VBox pour une vraie "liste")
// ============================================================================
package fr.mrqsdf.jfx.module;

import fr.mrqsdf.jfx.game.MaterialInventory;
import fr.olympus.hephaestus.resources.HephaestusData;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MaterialModule extends VBox {

    private final List<InfoModule> infoModules = new ArrayList<>();

    public MaterialModule(HephaestusData data, MaterialInventory inventory) {
        setSpacing(8);
        setStyle("-fx-padding: 8; -fx-background-color: #1b1b1b;");

        // Liste triée des matériaux connus du registry
        List<String> ids = new ArrayList<>(data.getAllMaterialIds());
        ids.sort(Comparator.naturalOrder());

        for (String id : ids) {
            Label amount = new Label("0");
            ImageView icon = new ImageView(); // placeholder (tu mettras tes textures ici)

            InfoModule info = new InfoModule(icon, amount, id);

            // bind amount
            inventory.amountProperty(id).addListener((obs, oldV, newV) -> info.setAmount(newV.intValue()));
            info.setAmount(inventory.getAmount(id));

            // (optionnel) afficher l'id
            Label idLabel = new Label(id);
            idLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
            info.getChildren().add(0, idLabel);

            infoModules.add(info);
            getChildren().add(info);
        }
    }

    public List<InfoModule> getInfoModules() {
        return infoModules;
    }

    public InfoModule getInfoModule(String materialId) {
        for (InfoModule m : infoModules) {
            if (m.getMaterialId().equals(materialId)) return m;
        }
        return null;
    }
}
