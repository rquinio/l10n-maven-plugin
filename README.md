# l10n-maven-plugin [![Build Status](https://travis-ci.org/rquinio/l10n-maven-plugin.svg)](https://travis-ci.org/rquinio/l10n-maven-plugin) [![Coverage Status](https://coveralls.io/repos/rquinio/l10n-maven-plugin/badge.svg)](https://coveralls.io/r/rquinio/l10n-maven-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.l10n-maven-plugin/l10n-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.l10n-maven-plugin/l10n-maven-plugin)

> A Maven plugin to validate localization resources in Java properties files 

## Description

The localization process of an application often involves non technical people. Some translated resources may contain issues linked to formatting, encoding, or the context where the resource is used.

l10-maven-plugin can validate a set of [properties files](http://en.wikipedia.org/wiki/.properties) (mainly used in Java [ResourceBundle](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/ResourceBundle.html)). It aims to either:
  * Detect invalid l10n properties resources at build time, typically before the webapp is packaged, or when extracting properties files from a localization tool/database (**[l10n:validate goal](https://github.com/rquinio/l10n-maven-plugin/wiki/l10n:validate goal)**).
  * Build a Maven site report listing violations found on properties (**[l10n:report goal](https://github.com/rquinio/l10n-maven-plugin/wiki/l10n:report goal)**), see a [sample report](https://cdn.rawgit.com/wiki/rquinio/l10n-maven-plugin/SampleReport.html).

The plugin was initially developed for some webapps translated in 7 languages with several thousands of properties across 5 resource bundles. It allowed to prevent buggy translations from slipping into production and reduced the cost of fixing them by detecting issues earlier.

It aims to be a pragmatic solution when having legacy constraints or needing fast & easy tool, yet probably not the ideal solution in terms of usability:
  * Some checks could rather be performed as soon as translator uploads/inputs the translation in the localization tool, to provide instant feedback.
  * Encoding/escaping should rather be managed by the code using the resources, based on the context they are being used (server side, client side, ...) rather than in the resources themselves.

## Usage

See [Usage](https://github.com/rquinio/l10n-maven-plugin/wiki/Usage) page for plugin goals and detailed configuration.

Plugin is available on [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22com.googlecode.l10n-maven-plugin%22), through [Sonatype OSS hosting](https://oss.sonatype.org).

```xml
<plugin>
  <groupId>com.googlecode.l10n-maven-plugin</groupId>
  <artifactId>l10n-maven-plugin</artifactId>
  <version>1.8</version>
</plugin>
```

## Requirements

The following specifies the _minimum_ requirements to run this Maven plugin:

| Library | Min version | Notes |
|--------|------------|------|
| **Maven** | **2.2.1** | Might work on previous Maven 2 versions, but not tested. |
| **JDK** | **1.5** |  |


## Validation

See the list of [validators](https://github.com/rquinio/l10n-maven-plugin/wiki/Validators) and associated checks.

## References

  * [java.util.Properties#load](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Properties.html#load%28java.io.InputStream%29) Javadoc
  * [java.text.MessageFormat](http://docs.oracle.com/javase/1.5.0/docs/api/java/text/MessageFormat.html) and [java.util.Formatter](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html) Javadoc.
  * [Javascript special characters](http://www.w3schools.com/js/js_special_characters.asp)
  * [java.util.Pattern](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html) Javadoc
  * [JSON specification](http://json.org/)
