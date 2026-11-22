package io.github.stasbykov.justdotesting.plugin;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;

/** Ключ относится к текущей строке или корню настроек. GROUP только группирует поля визуально. */
public record Field(Kind kind, String key, String label, boolean required, boolean editable,
                    List<Option> options, List<Field> children, ObjectNode rowDefaults) {
    public enum Kind { TEXT, NUMBER, CHOICE, CHECK, GROUP, ROWS }
    public record Option(String value, String label) {
        @Override public String toString() { return label; }
    }
    public Field {
        Objects.requireNonNull(kind);
        if (key == null || key.isBlank() || label == null || label.isBlank()) throw new IllegalArgumentException("Полю нужны ключ и название");
        options = List.copyOf(options);
        children = List.copyOf(children);
        if (kind == Kind.ROWS) rowDefaults = Objects.requireNonNull(rowDefaults).deepCopy();
    }
    public static Field input(Kind kind, String key, String label, boolean required) {
        return new Field(kind, key, label, required, true, List.of(), List.of(), null);
    }
    public static Field choice(String key, String label, List<Option> options, boolean required) {
        return new Field(Kind.CHOICE, key, label, required, true, options, List.of(), null);
    }
    public static Field group(String key, String label, List<Field> children) {
        return new Field(Kind.GROUP, key, label, false, true, List.of(), children, null);
    }
    public static Field rows(String key, String label, ObjectNode defaults, List<Field> children) {
        return new Field(Kind.ROWS, key, label, false, true, List.of(), children, defaults);
    }
}
