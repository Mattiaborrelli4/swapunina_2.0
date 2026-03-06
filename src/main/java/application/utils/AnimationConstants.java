package application.utils;

import javafx.animation.Interpolator;

/**
 * Costanti e configurazioni per animazioni premium.
 *
 * Ispirato a Stripe/Vercel con spring physics naturali.
 * Tutte le animazioni usano interpolatori custom per feel premium.
 */
public class AnimationConstants {

    private AnimationConstants() {
        // Utility class - previene istanziazione
    }

    // ========== INTERPOLATORI SPRING PREMIUM ==========

    /**
     * Spring bounce effect - per animazioni magnetiche e entrance
     * Eagcubic bezier con overshoot simulato
     * Nota: JavaFX SPLINE richiede coordinate in [0,1], quindi usiamo EASE_BOTH
     */
    public static final Interpolator EASE_SPRING = Interpolator.EASE_BOTH;

    /**
     * Smooth deceleration - per transizioni fluide
     * Usiamo interpolator built-in per compatibilità
     */
    public static final Interpolator EASE_OUT_QUINT = Interpolator.EASE_OUT;

    /**
     * Smooth symmetric - per animazioni bilanciate
     */
    public static final Interpolator EASE_IN_OUT = Interpolator.EASE_BOTH;

    /**
     * Quick exit - per hover states veloci
     */
    public static final Interpolator EASE_OUT = Interpolator.EASE_OUT;

    /**
     * Back effect - per animazioni con overshoot
     * Simulato con built-in per evitare coordinate fuori range
     */
    public static final Interpolator EASE_BACK = Interpolator.EASE_OUT;

    // ========== DURATE ANIMAZIONE (millisecondi) ==========

    /** Animazione istantanea - 100ms */
    public static final int DURATION_INSTANT = 100;

    /** Animazione veloce - 150ms (hover states) */
    public static final int DURATION_FAST = 150;

    /** Animazione normale - 250ms (transizioni standard) */
    public static final int DURATION_NORMAL = 250;

    /** Animazione lenta - 400ms (entrance animations) */
    public static final int DURATION_SLOW = 400;

    /** Animazione molto lenta - 600ms (page transitions) */
    public static final int DURATION_VERY_SLOW = 600;

    // ========== SPRING PHYSICS CONSTANTS ==========

    /** Spring stiffness - forza della molla (0.0 - 1.0) */
    public static final double SPRING_STIFFNESS = 0.6;

    /** Spring damping - smorzamento oscillazioni (0.0 - 1.0) */
    public static final double SPRING_DAMPING = 0.35;

    /** Mass per magnetic hover effect */
    public static final double MAGNETIC_MASS = 0.5;

    /** Magnetic strength - forza dell'effetto magnetico */
    public static final double MAGNETIC_STRENGTH = 25.0;

    // ========== STAGGER ANIMATION DELAYS ==========

    /** Delay tra elementi in stagger animation rapido */
    public static final int STAGGER_FAST = 30;

    /** Delay tra elementi in stagger animation normale */
    public static final int STAGHER_NORMAL = 50;

    /** Delay tra elementi in stagger animation lento */
    public static final int STAGGER_SLOW = 75;

    // ========== SCALE TRANSFORM VALUES ==========

    /** Scale hover per bottoni - leggero ingrandimento */
    public static final double SCALE_HOVER = 1.05;

    /** Scale active per bottoni premuti */
    public static final double SCALE_ACTIVE = 0.97;

    /** Scale normale */
    public static final double SCALE_NORMAL = 1.0;

    // ========== TRANSLATE VALUES ==========

    /** TranslateY hover per card - sollevamento */
    public static final double TRANSLATE_HOVER_CARD = -8.0;

    /** TranslateY hover per bottoni - sollevamento leggero */
    public static final double TRANSLATE_HOVER_BUTTON = -2.0;

    /** TranslateY entrace per card - partenza da sotto */
    public static final double TRANSLATE_ENTRANCE_CARD = 40.0;

    // ========== OPACITY VALUES ==========

    /** Opacity invisible */
    public static final double OPACITY_INVISIBLE = 0.0;

    /** Opacity半 transparent - per skeleton */
    public static final double OPACITY_SEMI = 0.5;

    /** Opacity normale */
    public static final double OPACITY_NORMAL = 1.0;

    // ========== ROTATION VALUES ==========

    /** Rotate per icon animation */
    public static final double ROTATE_ICON = 15.0;

    /** Rotate per refresh animation */
    public static final double ROTATE_REFRESH = 360.0;

    // ========== UTILITY METHODS ==========

    /**
     * Restituisce l'interpolatore appropriato per il tipo di animazione
     *
     * @param type Tipo di animazione
     * @return Interpolatore configurato
     */
    public static Interpolator getInterpolatorForType(AnimationType type) {
        return switch (type) {
            case SPRING -> EASE_SPRING;
            case EXIT -> EASE_OUT_QUINT;
            case SYMMETRIC -> EASE_IN_OUT;
            case QUICK -> EASE_OUT;
            case BACK -> EASE_BACK;
            default -> EASE_OUT_QUINT;
        };
    }

    /**
     * Restituisce la durata appropriata per il tipo di animazione
     *
     * @param type Tipo di durata
     * @return Durata in millisecondi
     */
    public static int getDurationForType(DurationType type) {
        return switch (type) {
            case INSTANT -> DURATION_INSTANT;
            case FAST -> DURATION_FAST;
            case NORMAL -> DURATION_NORMAL;
            case SLOW -> DURATION_SLOW;
            case VERY_SLOW -> DURATION_VERY_SLOW;
        };
    }

    // ========== ENUM TYPES ==========

    /** Tipi di animazione */
    public enum AnimationType {
        /** Spring bounce effect */
        SPRING,
        /** Smooth deceleration */
        EXIT,
        /** Symmetric easing */
        SYMMETRIC,
        /** Quick exit */
        QUICK,
        /** Back overshoot */
        BACK
    }

    /** Tipi di durata */
    public enum DurationType {
        /** Istantanea */
        INSTANT,
        /** Veloce */
        FAST,
        /** Normale */
        NORMAL,
        /** Lenta */
        SLOW,
        /** Molto lenta */
        VERY_SLOW
    }
}
