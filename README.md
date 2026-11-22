# Just Do Testing Plugin SDK

[![CI](https://github.com/stasbykov/just-do-testing-plugin-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/stasbykov/just-do-testing-plugin-sdk/actions/workflows/ci.yml)

Java 21 API для разработки конструкторов объектов в Just Do Testing. Один плагин может добавить несколько типов объектов. Для каждого типа автор описывает поля формы, зависимость от выбранного родителя, проверку значений и генерацию JSON или текста.

## Подключить SDK

После публикации версии в Maven Central добавьте зависимость в POM расширения:

```xml
<dependency>
  <groupId>io.github.stasbykov.justdotesting</groupId>
  <artifactId>scenario-plugin-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

Чтобы работать с исходниками или до первого релиза, установите JDK 21 и выполните из корня этого репозитория:

```shell
./mvnw clean install
./mvnw -f examples/shipping-builder/pom.xml clean verify
```

В Windows используйте `mvnw.cmd`. Вторая команда собирает `examples/shipping-builder/target/shipping-builder.jar`. Скопируйте его в каталог из меню **☰ → Модули… → Открыть каталог модулей** и перезапустите приложение. В примере тип «Склад» задаёт доступные услуги и способ доставки в форме «Партия отправлений». Готовый JAR примера прикладывается к релизу после публикации SDK.

[Руководство разработчика](docs/plugin-development.md) · [Исходники примера](examples/shipping-builder) · [Релизы](https://github.com/stasbykov/just-do-testing-plugin-sdk/releases)

## Выпуск версии

1. Укажите версию SDK в свойстве `revision` корневого POM. Обновите `sdk.version` примера и версию зависимости в документации. Дождитесь успешного CI в `main`.
2. Настройте GitHub Environment `maven-central` с секретами `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` (пара токена Central Portal), `MAVEN_GPG_PRIVATE_KEY` и `MAVEN_GPG_PASSPHRASE`. Для публикации требуются права на пространство имён `io.github.stasbykov.justdotesting`.
3. Опубликуйте обычный GitHub Release с тегом вида `v1.0.0`, указывающим на коммит из `main`. Workflow сверит тег с версией POM, подпишет артефакты и отправит SDK, исходники и Javadoc в Maven Central. После подтверждения публикации в GitHub Release появится `shipping-builder.jar`.

Push в `main` и pull request запускают сборку SDK и проверки примера. Dependabot еженедельно проверяет зависимости Maven и версии GitHub Actions.
