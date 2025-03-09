# java-bash
Run commands in bash terminal session


```
./gradlew clean test
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