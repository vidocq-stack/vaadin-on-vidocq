# Vaadin on Vidocq

A [Vaadin Flow](https://vaadin.com/flow) application running on the [Vidocq](https://vidocq.dev) runtime, with Foy,
Vidocq's Servlet 6.1 container, as its servlet engine. One view, no workaround class, and four ways to run it: in
development, as a packaged distribution, and as a self-contained `jlink` image with its own JDK.

Everything below is explained in the [Vaadin guide](https://doc.vidocq.dev/vidocq/dev/vaadin.html) of the Vidocq
documentation.

## Requirements

- JDK 25 (`sdk env` picks it up from `.sdkmanrc`).
- Network access to Maven Central: Vidocq 0.4.0 is not released yet, so the build uses the snapshot builds from
  Central's snapshot repository.

No Node: the frontend comes precompiled from `vaadin-prod-bundle`, and the application runs in Vaadin's production mode.

## Run it

| Goal | Command | Then |
|---|---|---|
| Development, reloaded on every change | `./mvnw vidocq:dev` | http://localhost:8080 |
| Run, as in production | `./mvnw vidocq:run` | http://localhost:8080 |
| Packaged distribution | `./mvnw clean package vidocq:package` | `target/vaadin-on-vidocq-*/bin/vaadin-on-vidocq.sh` |
| Self-contained image | `./mvnw -Pjlink clean package vidocq:jlink` | `JDK_JAVA_OPTIONS=-Dvaadin.productionMode=true target/dist/bin/vaadin-on-vidocq` |

The port is set in `src/main/resources/vidocq.properties`.

## What makes it work

- **`vidocq.app.layer=all`** (in `pom.xml`). Vaadin looks the application's resources up, `index.html` first, through
  the class loader that loaded Vaadin. With this setting, the Vaadin jars share the application's layer, as
  `WEB-INF/lib` does in a servlet container. Without it, every request answers 500 with *Unable to find index.html*.
- **Foy's codegen bundle** on the compiler's `annotationProcessorPaths`. `vidocq:generate` also runs Foy's processors
  over `flow-server` and `atmosphere-runtime`, so Vaadin's `@WebListener` and `ServletContainerInitializer` classes
  are registered. The startup report shows `2 enriched (Foy 2)`.
- **Production mode** is set once in `pom.xml` for `vidocq:dev`, `vidocq:run` and the `vidocq:package` scripts. The
  `jlink` launcher reads it from `JDK_JAVA_OPTIONS`.

## The `jlink` profile

`jlink` accepts named Java modules only, and no Vaadin jar is one. The `jlink` profile adds `vauban:modularize`, which
gives every jar of the closure a `module-info`, and sets two flags for two Vaadin layouts the module system does not
allow:

- `vidocq.jlink.mergeSharedResources`: `flow-server` and `flow-react` both ship files under
  `com/vaadin/flow/server/frontend/`, a split package. The flag moves them into one module of the image.
- `vauban.modularize.forceExplicit`: `flow-server`'s `Lookup` calls `ServiceLoader` for service types of modules that
  depend on `flow-server`, so no legal `module-info` exists for it. The flag writes one anyway, without the impossible
  directives. **Collaboration Engine and LitTemplate cannot be used in the image.**

What only Vaadin can change is collected in
[Vidocq/vidocq#256](https://codefloe.com/Vidocq/vidocq/issues/256).

## Known limits

Only production mode was checked, on this one view: the page and its frontend bundles. Push, websockets and larger
applications were not exercised yet. Feedback is welcome.
