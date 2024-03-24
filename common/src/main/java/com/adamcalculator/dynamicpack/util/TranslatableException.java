package com.adamcalculator.dynamicpack.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TranslatableException extends RuntimeException {
    private final String key;
    private final Object[] args;

    public TranslatableException(String message, String key, Object... args) {
        super(message);
        this.key = key;
        this.args = args;
    }

    public TranslatableException(String message, Throwable cause, String key, Object... args) {
        super(message, cause);
        this.key = key;
        this.args = args;
    }

    public TranslatableException(Throwable cause, String key, Object... args) {
        super(cause);
        this.key = key;
        this.args = args;
    }

    public TranslatableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String key, Object... args) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.key = key;
        this.args = args;
    }

    public static Component _findComponentOnException(Throwable e) {
        if (e == null) {
            return null;
        }
        if (e instanceof TranslatableException) {
            TranslatableException tr = (TranslatableException) e;
            return new TranslatableComponent(tr.key, tr.args);

        } else if (e.getCause() != null) {
            return _findComponentOnException(e.getCause());
        }
        return null;
    }

    public static Component getComponentFromException(Throwable e) {
        Component component = _findComponentOnException(e);
        if (component == null) {
            component = new TextComponent(e.getMessage());
        }
        return component;
    }
}
