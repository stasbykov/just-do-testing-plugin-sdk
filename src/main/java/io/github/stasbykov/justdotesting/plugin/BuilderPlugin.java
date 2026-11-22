package io.github.stasbykov.justdotesting.plugin;

import java.util.List;

/** Точка входа JAR, объявляется через META-INF/services. */
public interface BuilderPlugin {
    List<ObjectBuilder> builders();
}
