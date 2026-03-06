package application.utils;

import javafx.scene.shape.SVGPath;
import java.util.Map;

/**
 * Provider centralizzato per icone SVG in stile Lucide/Heroicons.
 *
 * Sistema di icone professionali e scalabili per l'interfaccia SwapUnina.
 * Tutte le icone usano SVGPath per perfetta qualità a qualsiasi risoluzione.
 */
public class IconProvider {

    /**
     * Paths SVG delle icone in stile Lucide (24x24 base)
     * Ogni path è ottimizzato per JavaFX SVGPath
     */
    private static final Map<String, String> ICON_PATHS = Map.ofEntries(
        // Navigation & Actions
        Map.entry("search", "M21 21l-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"),
        Map.entry("home", "m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"),
        Map.entry("settings", "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"),
        Map.entry("plus-circle", "M12 5v14m-7-7h14"),

        // Shopping & Commerce
        Map.entry("shopping-cart", "M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-8 2a2 2 0 1 1-4 0 2 2 0 0 1 4 0z"),
        Map.entry("shopping-bag", "M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z M3 6h18 M16 10a4 4 0 0 1-8 0"),
        Map.entry("tag", "M12 2H2v10l9.293 9.293a1 1 0 0 0 1.414 0l10-10a1 1 0 0 0 0-1.414L12 2Z M7 7h.01"),
        Map.entry("package", "M16.5 9.4 7.55 4.24m0 0L2 9.4m5.55-5.16L16.5 9.4M7.55 4.24l-4.4 10.8m4.4-10.8 11.4 6.52m-11.4-6.52 4.4 10.8m0-5.96 7-3.83"),

        // Communication
        Map.entry("message-circle", "M7.9 20A9 9 0 1 0 4 16.1L2 22Z"),
        Map.entry("message-square", "M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"),
        Map.entry("mail", "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6"),
        Map.entry("send", "M22 2 11 13 M22 2l-7 20-4-9-9-4 20-7z"),
        Map.entry("bell", "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9 M10.3 21a1.94 1.94 0 0 0 3.4 0"),

        // User & Account
        Map.entry("user", "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2c0 1 2 2 2 2h12s2-1 2-2z M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z"),
        Map.entry("users", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M17 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"),
        Map.entry("user-plus", "M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M8.5 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M19 8v6 M16 11h6"),
        Map.entry("log-out", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9"),

        // Actions
        Map.entry("heart", "M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"),
        Map.entry("star", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"),
        Map.entry("star-filled", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"),
        Map.entry("bookmark", "m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"),
        Map.entry("bookmark-check", "m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z M9 10l2 2 4-4"),

        // Exchange & Sync
        Map.entry("repeat", "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.27 6.96 12 12.01 20.73 6.96 M12 22.08V12"),
        Map.entry("refresh-cw", "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8 M5 19 9 21 21 5m0 0a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16 M19 5l-4-2-12 16"),
        Map.entry("sync", "M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8 M3 3v5h5 M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16 M21 21v-5h-5"),

        // UI Elements
        Map.entry("chevron-down", "m6 9 6 6 6-6"),
        Map.entry("chevron-up", "m18 15-6-6-6 6"),
        Map.entry("chevron-left", "m15 18-6-6 6-6"),
        Map.entry("chevron-right", "m9 18 6-6-6-6"),
        Map.entry("arrow-up-down", "m3 9 4 4 4-4 M21 15l-4-4-4 4"),
        Map.entry("filter", "M20 10H4M16 14H4m12 5H4M6 6l4 4 4-4"),

        // Status & Badges
        Map.entry("gift", "M20 12v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-6 M12 2v10 M6 7a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v1a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V7z"),
        Map.entry("gavel", "m14 13-7.5 7.5c-.83.83-2.17.83-3 0 0 0 0 0 0 0l-3-3 7.5-7.5 3 3zm-9.5-6.5c-2 2-2 5 0 7l3 3"),
        Map.entry("check-circle", "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4 12 14.01l-3-3"),
        Map.entry("x-circle", "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z M15 9l-6 6 M9 9l6 6"),

        // Time & Edit
        Map.entry("clock", "M12 8v4l3 3 M12 22c5.5 0 10-4.5 10-10S17.5 2 12 2 2 6.5 2 12s4.5 10 10 10z"),
        Map.entry("edit", "M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"),

        // Location
        Map.entry("map-pin", "M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z M12 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6z"),
        Map.entry("navigation", "m16.24 8.5-6-4a1 1 0 0 0-1 0l-6 4a1 1 0 0 0-.38.76v5.48a1 1 0 0 0 1 .62l6 4a1 1 0 0 0 1 0l6-4a1 1 0 0 0 1-.62V9.26a1 1 0 0 0-.38-.76z"),

        // Misc
        Map.entry("grid", "M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z"),
        Map.entry("list", "M8 6h13 M8 12h13 M8 18h13 M3 6h.01 M3 12h.01 M3 18h.01"),
        Map.entry("menu", "M4 6h16 M4 12h16 M4 18h16"),
        Map.entry("x", "M18 6 6 18M6 6l12 12"),
        Map.entry("info", "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z M12 16v-4 M12 8h.01")
    );

    /**
     * Dimensioni icone standardizzate
     */
    public enum IconSize {
        XS(12),   // 12px - Inline icons
        SM(16),   // 16px - Small buttons
        MD(20),   // 20px - Standard buttons
        LG(24),   // 24px - Large buttons, headers (default base size)
        XL(32),   // 32px - Hero sections
        XXL(48);  // 48px - Special cases

        private final int size;

        IconSize(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    /**
     * Restituisce un'icona SVGPath dato il nome
     *
     * @param iconName Nome dell'icona (deve esistere in ICON_PATHS)
     * @return SVGPath configurata con il path dell'icona
     * @throws IllegalArgumentException se il nome non esiste
     */
    public static SVGPath getIcon(String iconName) {
        String pathContent = ICON_PATHS.get(iconName);
        if (pathContent == null) {
            throw new IllegalArgumentException("Icon not found: " + iconName + ". Available icons: " + ICON_PATHS.keySet());
        }

        SVGPath icon = new SVGPath();
        icon.setContent(pathContent);
        return icon;
    }

    /**
     * Scala un'icona alla dimensione desiderata
     * La dimensione base è 24px (LG)
     *
     * @param icon Icona da scalare
     * @param size Dimensione desiderata
     */
    public static void scaleIcon(SVGPath icon, IconSize size) {
        double scaleFactor = (double) size.getSize() / 24.0;
        icon.setScaleX(scaleFactor);
        icon.setScaleY(scaleFactor);
    }

    /**
     * Crea un'icona già scalata alla dimensione desiderata
     *
     * @param iconName Nome dell'icona
     * @param size Dimensione desiderata
     * @return SVGPath configurata e scalata
     */
    public static SVGPath getIconScaled(String iconName, IconSize size) {
        SVGPath icon = getIcon(iconName);
        scaleIcon(icon, size);
        return icon;
    }

    /**
     * Restituisce la lista di tutte le icone disponibili
     *
     * @return Set con i nomi di tutte le icone
     */
    public static java.util.Set<String> getAvailableIcons() {
        return ICON_PATHS.keySet();
    }

    /**
     * Verifica se un'icona esiste
     *
     * @param iconName Nome dell'icona
     * @return true se l'icona esiste, false altrimenti
     */
    public static boolean hasIcon(String iconName) {
        return ICON_PATHS.containsKey(iconName);
    }
}
