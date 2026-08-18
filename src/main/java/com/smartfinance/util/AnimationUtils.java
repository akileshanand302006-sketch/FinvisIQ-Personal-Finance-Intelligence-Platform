package com.smartfinance.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.util.Duration;

/**
 * Animation Utilities - Safe, smooth, and robust transitions using built-in JavaFX Interpolators
 * to prevent any platform-specific layout or rendering crashes.
 */
public class AnimationUtils {

    /** Fade in a node with smooth ease-out. */
    public static void fadeIn(Node node, double durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    /** Fade out a node with callback. */
    public static void fadeOut(Node node, double durationMs, Runnable onFinish) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_IN);
        if (onFinish != null) ft.setOnFinished(e -> onFinish.run());
        ft.play();
    }

    /** Slide in a node from the bottom. */
    public static void slideInFromBottom(Node node, double durationMs) {
        node.setTranslateY(40);
        node.setOpacity(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromY(40);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs * 0.7), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }

    /** Slide in a node from the right. */
    public static void slideInFromRight(Node node, double durationMs) {
        node.setTranslateX(60);
        node.setOpacity(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromX(60);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs * 0.7), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }

    /** Slide in a node from the left. */
    public static void slideInFromLeft(Node node, double durationMs) {
        node.setTranslateX(-60);
        node.setOpacity(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromX(-60);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs * 0.7), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }

    /** Add smooth scale hover effect. */
    public static void addHoverScale(Node node, double scale) {
        ScaleTransition stEnter = new ScaleTransition(Duration.millis(200), node);
        stEnter.setToX(scale);
        stEnter.setToY(scale);
        stEnter.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition stExit = new ScaleTransition(Duration.millis(250), node);
        stExit.setToX(1.0);
        stExit.setToY(1.0);
        stExit.setInterpolator(Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> {
            stExit.stop();
            stEnter.playFromStart();
        });
        node.setOnMouseExited(e -> {
            stEnter.stop();
            stExit.playFromStart();
        });
    }

    /** Add hover effect with glow for interactive elements. */
    public static void addHoverGlow(Node node, double glowLevel) {
        Glow glow = new Glow(0);

        node.setOnMouseEntered(e -> {
            Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(200),
                    new KeyValue(glow.levelProperty(), glowLevel, Interpolator.EASE_OUT))
            );
            node.setEffect(glow);
            tl.play();
        });

        node.setOnMouseExited(e -> {
            Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(300),
                    new KeyValue(glow.levelProperty(), 0, Interpolator.EASE_IN))
            );
            tl.play();
        });
    }

    /** Animate a label counter from 0 to target value with smooth easing. */
    public static void animateCounter(Label label, double targetValue, String prefix, String suffix) {
        Timeline timeline = new Timeline();
        int frames = 40;
        double frameDuration = 700.0 / frames;

        for (int i = 0; i <= frames; i++) {
            final double fraction = (double) i / frames;
            double easedFraction = 1 - Math.pow(1 - fraction, 3);
            double value = targetValue * easedFraction;

            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * frameDuration), e -> {
                label.setText(prefix + String.format("%,.0f", value) + (suffix != null ? suffix : ""));
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(frames * frameDuration + 50), e -> {
            label.setText(prefix + String.format("%,.0f", targetValue) + (suffix != null ? suffix : ""));
        }));

        timeline.play();
    }

    /** Shake animation for error indication. */
    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(40), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(8);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    /** Safe bounce in animation with overshoot fallback. */
    public static void bounceIn(Node node) {
        node.setScaleX(0.7);
        node.setScaleY(0.7);
        node.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(400), node);
        st.setFromX(0.7);
        st.setFromY(0.7);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.play();
    }

    /** Staggered entrance for children of a container. */
    public static void staggerChildren(javafx.scene.layout.Pane parent, double delayBetween) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            child.setOpacity(0);
            child.setTranslateY(20);

            TranslateTransition tt = new TranslateTransition(Duration.millis(350), child);
            tt.setFromY(20);
            tt.setToY(0);
            tt.setDelay(Duration.millis(i * delayBetween));
            tt.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition ft = new FadeTransition(Duration.millis(300), child);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(i * delayBetween));
            ft.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition pt = new ParallelTransition(tt, ft);
            pt.play();
        }
    }

    /** Pulse animation. */
    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    /** Smooth scale-in animation. */
    public static void scaleIn(Node node, double durationMs) {
        node.setScaleX(0.9);
        node.setScaleY(0.9);
        node.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
        st.setFromX(0.9);
        st.setFromY(0.9);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs * 0.8), node);
        ft.setFromValue(0);
        ft.setToValue(1);

        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.play();
    }

    /** Smooth float animation for decorative elements. */
    public static void float_(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(3 + Math.random() * 3), node);
        tt.setFromY(0);
        tt.setToY(-15 - Math.random() * 20);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }
}
