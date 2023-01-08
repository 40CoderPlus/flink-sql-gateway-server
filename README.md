# Gradle Single Module Template

Using plugin
- [Baseline](https://github.com/palantir/gradle-baseline)
- [Spotless](https://github.com/diffplug/spotless)
- [Sonar](https://github.com/SonarSource/sonar-scanner-gradle)
- [Dependency Check](https://github.com/dependency-check/dependency-check-gradle)

# Baseline

```shell
gradle idea
```

# Build

```shell
gradle build -x test
```

# Dependency Check

Execute dependency check:

```shell
gradle dependencyCheckAggregate
```

# Spotless

```shell
gradle spotlessApply
```

# Sonar

Configuration：

add `gradle.properties` in folder `GRADLE_USER_HOME`

add content:

```text
nexusUsername=foo
nexusPassword=bar

systemProp.sonar.host.url=http://sonarqube.40coderplus.com
systemProp.sonar.login=keyForSonarqube
```

Execute sonar check:
```shell
gradle sonar
```

# Publish to Maven

```shell
gradle publish
```