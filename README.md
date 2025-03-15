# java-bash
Run commands in bash terminal session
pkg:maven/io.github.andreyzebin/json-bash@0.0.1

```
./gradlew clean test

./gradlew clean build
./gradlew sign
./gradlew publishMavenJavaPublicationToMavenRepository
./gradlew packageDistribution

```

```

set JAVA_HOME=C:\Path\To\Your\Java\Home   // Windows
export JAVA_HOME=/path/to/your/java/home  // Mac/Linux


User home gradle.properties
signing.keyId=24875D73
signing.password=
signing.secretKeyRingFile=/Users/me/.gnupg/secring.gpg

signingKeyId=xxx
signingKey=/Users/me/.gnupg/secring.gpg
signingPassword=xxx

CI
export ORG_GRADLE_PROJECT_signingKeyId='24875D73'
export ORG_GRADLE_PROJECT_signingKey='Hi, world'
export ORG_GRADLE_PROJECT_signingPassword='Hi, world'

```

[![Gradle Package](https://github.com/andreyzebin/java-bash/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/andreyzebin/java-bash/actions/workflows/gradle-publish.yml)