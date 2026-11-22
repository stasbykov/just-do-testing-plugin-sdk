package io.github.stasbykov.justdotesting.plugin;

import java.util.List;

/** JAR entry point registered through META-INF/services. */
public interface BuilderPlugin {
    List<ObjectBuilder> builders();
}
