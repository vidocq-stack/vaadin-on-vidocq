# Vaadin on Vidocq

A [Vaadin Flow](https://vaadin.com/flow) application running on the [Vidocq](https://vidocq.dev) runtime, with Foy,
Vidocq's Servlet 6.1 container, as its servlet engine. One view, no workaround class, and four ways to run it: in
development, as in production, as a packaged distribution, and as a self-contained `jlink` image with its own JDK.

Everything below is explained in the [Vaadin guide](https://doc.vidocq.dev/vidocq/dev/vaadin.html) of the Vidocq
documentation.

## Requirements

- JDK 25 (`sdk env` picks it up from `.sdkmanrc`).
- Network access to Maven Central and to `https://maven.vidocq.dev/snapshots/`: Vidocq 0.4.0 is not released yet, so
  the build uses the snapshot builds of Vidocq's own repository (`maven.bak.vidocq.dev` is its mirror). `pom.xml`
  declares them under `<repositories>` and under `<pluginRepositories>`: the Vidocq plugin is a build extension,
  which Maven resolves from the plugin repositories only, so a build with the first block alone stops with
  _Unresolveable build extension_ on a machine that has not cached the plugin. Snapshots only: a release comes from
  Maven Central.

No Node: the frontend comes precompiled from `vaadin-prod-bundle`, and the application runs in Vaadin's production mode.

## Run it

Each command works from a fresh clone: `vidocq:dev` and `vidocq:run` compile the project and build the frontend first.

| Goal | Command | Then |
|---|---|---|
| Development, reloaded on every change | `./mvnw vidocq:dev` | http://localhost:8080 |
| Run, as in production | `./mvnw vidocq:run` | http://localhost:8080 |
| Packaged distribution | `./mvnw clean package` | `target/vaadin-on-vidocq-*/bin/vaadin-on-vidocq.sh` |
| Self-contained image | `./mvnw -Pjlink clean package vidocq:jlink` | `JDK_JAVA_OPTIONS=-Dvaadin.productionMode=true target/dist/bin/vaadin-on-vidocq` |

The port is set in `src/main/resources/vidocq.properties`.

With the [Vidocq CLI](https://doc.vidocq.dev/vidocq/dev/modules/vidocq-runtime-cli.html), `vidocq dev`, `vidocq start`
and `vidocq build` run the first three; `vidocq dev --port 8081` moves the port. The image needs the `jlink`
profile, which the CLI cannot pass: use the Maven command. For IntelliJ, `vidocq idea` writes `(dev)`, `(run)` and
`(debug)` run configurations into `.run/`.

The distribution is also zipped as `target/vaadin-on-vidocq-*-dist.zip`; unzipped elsewhere, it starts with
`bin/vaadin-on-vidocq.sh`. The ZIP keeps the launcher's execute bit from Vidocq `0.4.0-SNAPSHOT` build
`0.4.0-20261116.045035-145` on: if your local repository holds an older snapshot, build with `./mvnw -U clean package`.

## What makes it work

- **The Vaadin jars share the application's layer**, as `WEB-INF/lib` does in a servlet container. Vaadin looks the
  application's resources up, `index.html` first, through the class loader that loaded Vaadin. Nothing to set: since
  Vidocq 0.4.0, `vidocq:run` and the `vidocq:package` distribution put every dependency outside the runtime in the
  application's layer (`vidocq.app.layer=all` is the default). A `jlink` image has one layer, the JVM's own.
- **The `generate` execution** of `vidocq-runtime-maven-plugin`. The plugin puts Foy's annotation processors on the
  compiler's path by itself, and `vidocq:generate` also runs them over `flow-server` and `atmosphere-runtime`, so
  Vaadin's `@WebListener` and `ServletContainerInitializer` classes are registered. The startup report shows
  `2 enriched (Foy 2)`.
- **Production mode** is set once in `pom.xml` for `vidocq:dev`, `vidocq:run` and the `vidocq:package` scripts. The
  `jlink` launcher reads it from `JDK_JAVA_OPTIONS`.
- **No main class.** The runtime's own main boots the module that `vidocq.mainModule` names, under every goal down to
  the `jlink` image.

## An app shell

A theme, stylesheets or PWA settings mean an `AppShellConfigurator`. In a `jlink` image, Vaadin looks it up through
`ServiceLoader` from `flow.server`, which the image cannot allow: the first request then never answers. Add
`vidocq-runtime-vaadin-extension` and `requires io.vidocq.runtime.extensions.jakartaee.web.vaadin;`
(`vidocq extension add vaadin` writes both). It changes nothing under the other goals. See
[An app shell in a jlink image](https://doc.vidocq.dev/vidocq/dev/vaadin.html#jlink-app-shell).

## The `jlink` profile

`jlink` accepts named Java modules only, and no Vaadin jar is one. The `jlink` profile adds `vauban:modularize`, which
gives every jar of the closure a `module-info` (the parent manages the plugin's version), and sets two flags for two Vaadin layouts the module system does not
allow:

- `vidocq.jlink.mergeSharedResources`: `flow-server` and `flow-react` both ship files under
  `com/vaadin/flow/server/frontend/`, a split package. The flag moves them into one module of the image.
- `vauban.modularize.forceExplicit`: `flow-server`'s `Lookup` calls `ServiceLoader` for service types of modules that
  depend on `flow-server`, so no legal `module-info` exists for it. The flag writes one anyway, without the impossible
  directives. **Collaboration Engine and LitTemplate cannot be used in the image.**

What only Vaadin can change is collected in
[Vidocq/vidocq#256](https://codefloe.com/Vidocq/vidocq/issues/256).

## Known limits

- Vaadin runs in production mode only, with the precompiled bundle: development mode, with its Vite dev server, was
  not tried. A hot reload under `vidocq:dev` starts a new session: an open page reloads on its next request.
- Push works with `@Push(transport = Transport.LONG_POLLING)` only: Foy implements no Jakarta WebSocket
  ([Vidocq/vidocq#65](https://codefloe.com/Vidocq/vidocq/issues/65)). See
  [Push](https://doc.vidocq.dev/vidocq/dev/vaadin.html#push) in the Vaadin guide.
- Larger applications were not exercised yet. Feedback is welcome.
