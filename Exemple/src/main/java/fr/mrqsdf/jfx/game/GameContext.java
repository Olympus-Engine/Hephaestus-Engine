// ============================================================================
// FILE: fr/mrqsdf/jfx/game/GameContext.java
// AJOUT: grantOutputsFromRecipe(...) + choix output via MaterialMatcher
// ============================================================================
package fr.mrqsdf.jfx.game;

import fr.olympus.hephaestus.factory.Factory;
import fr.olympus.hephaestus.materials.MaterialInstance;
import fr.olympus.hephaestus.processing.MaterialMatcher;
import fr.olympus.hephaestus.processing.ProcessRecipe;
import fr.olympus.hephaestus.processing.TimeWindow;
import fr.olympus.hephaestus.register.ProcessRecipeRegistryEntry;
import fr.olympus.hephaestus.resources.HephaestusData;

import java.util.*;

public final class GameContext {

    private final HephaestusData data;
    private final MaterialInventory inventory;

    public GameContext(HephaestusData data, MaterialInventory inventory) {
        this.data = Objects.requireNonNull(data, "data");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    public HephaestusData data() {
        return data;
    }

    public MaterialInventory inventory() {
        return inventory;
    }

    public boolean tryStartProduction(Factory factory, ProcessRecipe recipe) {
        if (factory == null || recipe == null) return false;

        // --- Choix des inputs (1 par matcher)
        List<String> chosenIds = new ArrayList<>();
        for (MaterialMatcher matcher : recipe.inputs()) {
            String chosen = chooseOneMaterialIdForInputMatcher(matcher);
            if (chosen == null) return false;
            chosenIds.add(chosen);
        }

        // --- Vérif quantité
        Map<String, Integer> needed = new HashMap<>();
        for (String id : chosenIds) needed.merge(id, 1, Integer::sum);

        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            if (!inventory.hasAtLeast(e.getKey(), e.getValue())) return false;
        }

        // --- Consomme
        for (Map.Entry<String, Integer> e : needed.entrySet()) {
            if (!inventory.remove(e.getKey(), e.getValue())) return false;
        }

        // --- Injecte dans la factory
        for (String id : chosenIds) {
            factory.insert(new MaterialInstance(id, dummyVoxels()));
        }

        // --- Lance session hephaestus (même si on force-complete côté jeu)
        factory.setSession(recipe);
        factory.startFactory();
        return true;
    }

    /** Tick Hephaestus (si certaines recipes produisent d'elles-mêmes) */
    public int tickFactory(Factory factory, float dt) {
        if (factory == null) return 0;

        factory.update(dt, data);

        List<MaterialInstance> out = factory.extractAllOutputs();
        if (out.isEmpty()) return 0;

        for (MaterialInstance mi : out) {
            inventory.add(mi.materialId(), 1);
        }
        return out.size();
    }

    public ProcessRecipeRegistryEntry getRecipeEntry(String recipeId) {
        return data.getProcessRecipeById(recipeId);
    }

    public List<ProcessRecipeRegistryEntry> recipesForFactory(Factory factory) {
        return data.getProcessRecipesByFactoryId(factory.getRegistryId(), factory.getRegistryGroups(), factory.getRegistryLevel());
    }

    /** Force: ajoute les outputs de la recipe dans l'inventaire */
    public int grantOutputsFromRecipe(ProcessRecipe recipe) {
        if (recipe == null) return 0;

        int added = 0;
        for (MaterialMatcher matcher : recipe.outputs()) {
            String outId = chooseOneMaterialIdForOutputMatcher(matcher);
            if (outId == null) continue;
            inventory.add(outId, 1);
            added++;
        }
        return added;
    }

    public float minSecondsOrZero(ProcessRecipe recipe) {
        if (recipe == null) return 0f;
        TimeWindow w = recipe.timeWindowOrNull();
        return w == null ? 0f : w.minSeconds();
    }

    // -----------------------
    // Matcher resolution
    // -----------------------

    private String chooseOneMaterialIdForInputMatcher(MaterialMatcher matcher) {
        if (matcher == null) return null;

        return switch (matcher.getKind()) {
            case ID -> {
                String id = matcher.getMaterialId();
                yield (id != null && inventory.getAmount(id) > 0) ? id : null;
            }
            case ANY -> firstAnyAvailable();
            case ANY_OF_CATEGORIES -> firstMatchingAnyOfAvailable(matcher.getCategoryKeys());
            case ALL_OF_CATEGORIES -> firstMatchingAllOfAvailable(matcher.getCategoryKeys());
        };
    }

    private String chooseOneMaterialIdForOutputMatcher(MaterialMatcher matcher) {
        if (matcher == null) return null;

        return switch (matcher.getKind()) {
            case ID -> matcher.getMaterialId();
            case ANY -> firstAnyRegistered();
            case ANY_OF_CATEGORIES -> firstMatchingAnyOfRegistered(matcher.getCategoryKeys());
            case ALL_OF_CATEGORIES -> firstMatchingAllOfRegistered(matcher.getCategoryKeys());
        };
    }

    private String firstAnyAvailable() {
        for (String id : data.getAllMaterialIds()) {
            if (inventory.getAmount(id) > 0) return id;
        }
        return null;
    }

    private String firstAnyRegistered() {
        for (String id : data.getAllMaterialIds()) {
            return id; // le premier
        }
        return null;
    }

    private String firstMatchingAnyOfAvailable(Set<String> wanted) {
        if (wanted == null || wanted.isEmpty()) return null;
        for (String id : data.getAllMaterialIds()) {
            if (inventory.getAmount(id) <= 0) continue;
            Set<String> keys = data.getMaterialCategoryKeys(id);
            for (String w : wanted) {
                if (keys.contains(w)) return id;
            }
        }
        return null;
    }

    private String firstMatchingAllOfAvailable(Set<String> wanted) {
        if (wanted == null || wanted.isEmpty()) return null;
        for (String id : data.getAllMaterialIds()) {
            if (inventory.getAmount(id) <= 0) continue;
            Set<String> keys = data.getMaterialCategoryKeys(id);
            if (keys.containsAll(wanted)) return id;
        }
        return null;
    }

    private String firstMatchingAnyOfRegistered(Set<String> wanted) {
        if (wanted == null || wanted.isEmpty()) return null;
        for (String id : data.getAllMaterialIds()) {
            Set<String> keys = data.getMaterialCategoryKeys(id);
            for (String w : wanted) {
                if (keys.contains(w)) return id;
            }
        }
        return null;
    }

    private String firstMatchingAllOfRegistered(Set<String> wanted) {
        if (wanted == null || wanted.isEmpty()) return null;
        for (String id : data.getAllMaterialIds()) {
            Set<String> keys = data.getMaterialCategoryKeys(id);
            if (keys.containsAll(wanted)) return id;
        }
        return null;
    }

    private static byte[][][] dummyVoxels() {
        return new byte[1][1][1];
    }
}
