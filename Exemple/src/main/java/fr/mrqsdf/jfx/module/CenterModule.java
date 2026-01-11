// ============================================================================
// FILE: fr/mrqsdf/jfx/module/CenterModule.java
// (modif: après création, on force un resize/autosize avant de placer)
// ============================================================================
package fr.mrqsdf.jfx.module;

import fr.mrqsdf.jfx.game.GameContext;
import fr.olympus.hephaestus.factory.Factory;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CenterModule extends Pane {

    private final GameContext game;
    private final List<FactoryModule> modules = new ArrayList<>();

    private String placingFactoryId;

    public CenterModule(GameContext game) {
        this.game = Objects.requireNonNull(game, "game");

        setOnMouseClicked(e -> {
            if (placingFactoryId == null) return;
            if (e.getButton() != MouseButton.PRIMARY) return;

            Factory f = game.data().createFactory(placingFactoryId);
            FactoryModule fm = new FactoryModule(game, f);

            // force size (important)
            fm.applyCss();
            fm.autosize();
            fm.resize(fm.getPrefWidth(), fm.getPrefHeight());

            double x = e.getX() - fm.getWidth() * 0.5;
            double y = e.getY() - fm.getHeight() * 0.5;

            if (x < 0) x = 0;
            if (y < 0) y = 0;

            fm.relocate(x, y);

            getChildren().add(fm);
            modules.add(fm);

            endPlacement();
            e.consume();
        });
    }

    public void beginPlacement(String factoryId) {
        this.placingFactoryId = factoryId;
        setCursor(Cursor.CROSSHAIR);
    }

    public void endPlacement() {
        this.placingFactoryId = null;
        setCursor(Cursor.DEFAULT);
    }

    public void tick(float dt) {
        for (FactoryModule m : modules) {
            if (m.getParent() == null) continue;
            m.tick(dt);
        }
        modules.removeIf(m -> m.getParent() == null);
    }

    public List<FactoryModule> getModules() {
        return modules;
    }
}
