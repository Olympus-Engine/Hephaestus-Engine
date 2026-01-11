// ============================================================================
// FILE: fr/mrqsdf/jfx/game/MaterialInventory.java
// ============================================================================
package fr.mrqsdf.jfx.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MaterialInventory {

    private final Map<String, IntegerProperty> amounts = new ConcurrentHashMap<>();

    public IntegerProperty amountProperty(String materialId) {
        return amounts.computeIfAbsent(materialId, k -> new SimpleIntegerProperty(0));
    }

    public int getAmount(String materialId) {
        IntegerProperty p = amounts.get(materialId);
        return p == null ? 0 : p.get();
    }

    public void setAmount(String materialId, int amount) {
        if (amount < 0) amount = 0;
        amountProperty(materialId).set(amount);
    }

    public void add(String materialId, int delta) {
        if (delta <= 0) return;
        IntegerProperty p = amountProperty(materialId);
        p.set(p.get() + delta);
    }

    public boolean remove(String materialId, int delta) {
        if (delta <= 0) return true;
        IntegerProperty p = amountProperty(materialId);
        int cur = p.get();
        if (cur < delta) return false;
        p.set(cur - delta);
        return true;
    }

    public boolean hasAtLeast(String materialId, int amount) {
        return getAmount(materialId) >= amount;
    }

    public Set<String> knownMaterialIds() {
        return Collections.unmodifiableSet(amounts.keySet());
    }
}
