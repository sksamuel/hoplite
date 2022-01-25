# Hoplite and GraalVM native image example

## Prerequisites

You need a GraalVM java distribution with installed native image compiler:

```shell
gu install native-image
```

See also:
- [Install GraalVM](https://www.graalvm.org/22.0/docs/getting-started/#install-graalvm)
- [Native image support](https://www.graalvm.org/22.0/reference-manual/native-image/#install-native-image)

## Updating GraalVM native image configs

```shell
../gradlew run -Dnative.image.agent=1 --args="config.yaml"
```

## Building native executable

```shell
../gradlew clean nativeBuild
```

A key config that you need is [kotlin-resource.json](https://github.com/sksamuel/hoplite/tree/master/example-native/META-INF/native-image/kotlin-resource.json)
It should be added into `-H:ResourceConfigurationFiles` native build option, see `build.gradle.kts` for details.

## Running native executable

```shell
./build/native/nativeBuild/example config.yaml
```
