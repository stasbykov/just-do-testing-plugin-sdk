package example.shipping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.stasbykov.justdotesting.plugin.BuilderPlugin;
import io.github.stasbykov.justdotesting.plugin.ObjectBuilder;
import io.github.stasbykov.justdotesting.plugin.ObjectBuilder.*;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShippingPluginTest {
    @Test
    void registeredBuildersApplyParentSettingsAndGenerateTheBatch() throws Exception {
        var plugin = ServiceLoader.load(BuilderPlugin.class).findFirst().orElseThrow();
        var builders = plugin.builders();
        assertEquals(2, builders.size());
        var depot = builders.stream().filter(b -> b.type().id().equals("depot")).findFirst().orElseThrow();
        ObjectBuilder batch = builders.stream().filter(b -> b.type().acceptsParent()).findFirst().orElseThrow();
        var parentId = UUID.randomUUID();
        var zone = new Parent(parentId, "Склад", "example.shipping", "depot", 1, depot.defaults());
        var city = batch.form(batch.defaults(), Optional.of(zone), Event.OPEN, "");
        assertEquals("Курьер", city.settings().path("mode").asText());
        var settings = city.settings().put("service", "express");
        var pieces = settings.putArray("items");
        pieces.addObject().put("pieces", 2);
        pieces.addObject().put("pieces", 3);
        var draft = batch.form(settings, Optional.of(zone), Event.EDIT, "/items");
        assertTrue(draft.issues().isEmpty());
        var result = new ObjectMapper().readTree(batch.generate(draft.settings(), new Context(1, key -> "test-id")).value());
        assertEquals(5, result.path("totalPieces").intValue());
        assertEquals("express", result.path("service").asText());
        assertEquals("test-id", result.path("id").asText());
        var remote = new Parent(parentId, "Склад", "example.shipping", "depot", 1, depot.defaults().put("zone", "remote"));
        var changed = batch.form(draft.settings(), Optional.of(remote), Event.PARENT, "");
        assertEquals("Перевозчик", changed.settings().path("mode").asText());
        assertEquals("standard", changed.settings().path("service").asText());
        assertEquals("", batch.form(changed.settings(), Optional.empty(), Event.PARENT, "").settings().path("mode").asText());
    }
}
