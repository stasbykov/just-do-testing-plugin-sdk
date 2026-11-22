package example.shipping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import io.github.stasbykov.justdotesting.plugin.*;
import io.github.stasbykov.justdotesting.plugin.Field.*;
import java.util.*;

public final class ShippingPlugin implements BuilderPlugin {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;
    private static final List<Option> ZONES = List.of(new Option("city", "Город"), new Option("remote", "За городом"));
    @Override public List<ObjectBuilder> builders() { return List.of(new Depot(), new Batch()); }

    private static final class Depot implements ObjectBuilder {
        public Type type() { return new Type("depot", "Склад", 1, Format.JSON, false); }
        public ObjectNode defaults() { return JSON.objectNode().put("zone", "city"); }
        public Form form(ObjectNode draft, Optional<Parent> parent, Event event, String path) {
            return new Form(draft, List.of(Field.choice("zone", "Зона доставки", ZONES, true)), List.of());
        }
        public Body generate(ObjectNode settings, Context context) { return new Body(settings.toString()); }
    }

    private static final class Batch implements ObjectBuilder {
        private static final Option STANDARD = new Option("standard", "Обычная");
        private static final Option EXPRESS = new Option("express", "Экспресс");
        private static final Option FREIGHT = new Option("freight", "Грузовая");
        public Type type() { return new Type("batch", "Партия отправлений", 1, Format.JSON, true); }
        public ObjectNode defaults() {
            var state = JSON.objectNode().put("service", "standard").put("insured", false);
            state.putArray("items").addObject().put("pieces", 1);
            return state;
        }
        public Form form(ObjectNode draft, Optional<Parent> parent, Event event, String path) {
            String zone = parent.filter(p -> p.pluginId().equals("example.shipping") && p.typeId().equals("depot") && p.version() == 1
                            || p.pluginId().isEmpty() && p.typeId().equals("json"))
                    .map(p -> p.values().path("zone").asText()).filter(z -> z.equals("city") || z.equals("remote")).orElse("");
            var services = switch (zone) {
                case "city" -> List.of(STANDARD, EXPRESS);
                case "remote" -> List.of(STANDARD, FREIGHT);
                default -> List.of(STANDARD);
            };
            if (services.stream().noneMatch(o -> o.value().equals(draft.path("service").asText())))
                draft.put("service", STANDARD.value());
            String mode = switch (zone) { case "city" -> "Курьер"; case "remote" -> "Перевозчик"; default -> ""; };
            draft.put("mode", mode);
            var fields = new ArrayList<Field>();
            fields.add(new Field(Kind.TEXT, "mode", "Способ доставки", false, false, List.of(), List.of(), null));
            fields.add(Field.choice("service", "Услуга", services, true));
            fields.add(Field.input(Kind.CHECK, "insured", "Добавить страховку", false));
            if (draft.path("insured").asBoolean()) fields.add(Field.input(Kind.NUMBER, "amount", "Сумма страховки", true));
            fields.add(Field.rows("items", "Места", JSON.objectNode().put("pieces", 1),
                    List.of(Field.input(Kind.NUMBER, "pieces", "Количество мест", true))));
            var issues = new ArrayList<Issue>();
            if (draft.path("items").isEmpty()) issues.add(new Issue("/items", "Добавьте позицию"));
            for (int i = 0; i < draft.path("items").size(); i++) {
                var n = draft.path("items").get(i).path("pieces");
                if (!n.canConvertToExactIntegral() || !n.canConvertToInt() || n.intValue() < 1)
                    issues.add(new Issue("/items/" + i + "/pieces", "Количество мест должно быть целым положительным числом"));
            }
            if (draft.path("insured").asBoolean() && draft.path("amount").decimalValue().signum() <= 0)
                issues.add(new Issue("/amount", "Сумма страховки должна быть положительной"));
            return new Form(draft, fields, issues);
        }
        public Body generate(ObjectNode settings, Context context) throws InterruptedException {
            var body = JSON.objectNode().put("id", context.namedVariable().apply("uuidv7:batch"));
            for (String key : List.of("mode", "service", "items")) body.set(key, settings.get(key));
            if (settings.path("insured").asBoolean()) body.set("insuranceAmount", settings.get("amount"));
            long total = 0;
            for (JsonNode row : settings.path("items")) { context.checkCancelled(); total += row.path("pieces").intValue(); }
            body.put("totalPieces", total);
            return new Body(body.toString());
        }
    }
}
