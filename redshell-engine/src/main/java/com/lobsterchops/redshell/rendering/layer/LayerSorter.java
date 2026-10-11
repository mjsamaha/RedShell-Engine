package com.lobsterchops.redshell.rendering.layer;

import com.lobsterchops.redshell.rendering.sprite.Sprite2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Orders sprites by layer, then z-index, then Y (higher Y first, so lower sprites overlap higher ones). Stable. */
public final class LayerSorter {

    private static final Comparator<Sprite2D> ORDER = new Comparator<Sprite2D>() {
        @Override
        public int compare(Sprite2D a, Sprite2D b) {
            if (a.layer.order() != b.layer.order()) {
                return a.layer.order() < b.layer.order() ? -1 : 1;
            }
            if (a.zIndex != b.zIndex) {
                return a.zIndex < b.zIndex ? -1 : 1;
            }
            return Float.compare(b.sortY, a.sortY);
        }
    };

    private LayerSorter() {
    }

    public static void sort(List<Sprite2D> sprites) {
        Collections.sort(sprites, ORDER);
    }
}