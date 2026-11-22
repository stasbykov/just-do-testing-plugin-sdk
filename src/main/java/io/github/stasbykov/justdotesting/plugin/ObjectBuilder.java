package io.github.stasbykov.justdotesting.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.util.function.Function;

public interface ObjectBuilder {
    enum Format { JSON, TEXT }
    record Type(String id, String name, int version, Format format, boolean acceptsParent) {
        public Type {
            if (id == null || id.isBlank() || name == null || name.isBlank() || version < 1)
                throw new IllegalArgumentException("Некорректное описание типа");
            Objects.requireNonNull(format);
        }
    }
    /** Editor data: builder settings or the literal body of a manually defined object. */
    record Parent(UUID id, String name, String pluginId, String typeId, int version, JsonNode values) {
        public Parent { values = values.deepCopy(); }
        @Override public JsonNode values() { return values.deepCopy(); }
    }
    record Issue(String path, String message) {}
    record Form(ObjectNode settings, List<Field> fields, List<Issue> issues) {
        public Form {
            settings = settings.deepCopy();
            fields = List.copyOf(fields);
            issues = List.copyOf(issues);
        }
        @Override public ObjectNode settings() { return settings.deepCopy(); }
    }
    enum Event { OPEN, EDIT, PARENT }
    record Context(int iteration, Function<String,String> namedVariable) {
        public void checkCancelled() throws InterruptedException {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        }
    }
    record Body(String value, List<String> sensitivePaths) {
        public Body { Objects.requireNonNull(value); sensitivePaths = List.copyOf(sensitivePaths); }
        public Body(String value) { this(value, List.of()); }
    }
    Type type();
    ObjectNode defaults();
    Form form(ObjectNode draft, Optional<Parent> parent, Event event, String changedPath);
    Body generate(ObjectNode settings, Context context) throws Exception;
    default ObjectNode migrate(int from, ObjectNode settings) {
        if (from != type().version()) throw new IllegalArgumentException("Версия настроек " + from + " не поддерживается");
        return settings;
    }
}
