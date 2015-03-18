# l10n-maven-plugin [![Build Status](https://travis-ci.org/rquinio/l10n-maven-plugin.svg)](https://travis-ci.org/rquinio/l10n-maven-plugin)

> A Maven plugin to validate localization resources in Java properties files 

## Description

The localization process of an application often involves non technical people. Some translated resources may contain issues linked to formatting, encoding, or the context where the resource is used.

l10-maven-plugin can validate a set of [properties files](http://en.wikipedia.org/wiki/.properties) (mainly used in Java [ResourceBundle](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/ResourceBundle.html)). It aims to either:
  * Detect invalid l10n properties resources at build time, typically before the webapp is packaged, or when extracting properties files from a localization tool/database (**[l10n:validate goal](Usage#l10n:validate.md)**).
  * Build a Maven site report listing violations found on properties (**[l10n:report goal](Usage#l10n:report.md)**), see a [sample report](http://wiki.l10n-maven-plugin.googlecode.com/git/SampleReport.html).

The plugin was initially developed for some webapps translated in 7 languages with several thousands of properties across 5 resource bundles. It allowed to prevent buggy translations from slipping into production and reduced the cost of fixing them by detecting issues earlier.

It aims to be a pragmatic solution when having legacy constraints or needing fast & easy tool, yet probably not the ideal solution in terms of usability:
  * Some checks could rather be performed as soon as translator uploads/inputs the translation in the localization tool, to provide instant feedback.
  * Encoding/escaping should rather be managed by the code using the resources, based on the context they are being used (server side, client side, ...) rather than in the resources themselves.

## Usage

See [Usage](Usage.md) page for plugin goals and detailed configuration.

Plugin is available on [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22com.googlecode.l10n-maven-plugin%22), through [Sonatype OSS hosting](https://oss.sonatype.org).

```
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

The files are loaded as Java [Properties](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Properties.html) and some checks are performed, depending on the type of resource.

| Resource type | Detection | Notes |
|------------------|--------------|----------|
| Translated resource | Resource translated in more than 1 language |  |
| Parametric resource | Resource containing formatting parameters. Supports java.text.MessageFormat {0} {1,date} etc., as well as java.util.Formatter %1$s %2$s (explicitly indexed args only)|  |
| HTML resource | Resource key matching _htmlKeys_ plugin property (default: _.text._) | [HTML5 data-\* custom attributes](http://dev.w3.org/html5/spec/elements.html#embedding-custom-non-visible-data) are ignored |
| Js resource | Resource key matching _jsKeys_ plugin property (default: _.js._) | Also considered as an HTML/Text resource, after javascript unescaping |
| URL resource | Resource key matching _urlKeys_ plugin property (default: _.url_.) | HTML unescaping is applied before validation |
| Plain text resource | Resource key matching _textKeys_ plugin property (default: _.title._) |  |
| Custom resource | Resource key matching a custom pattern |  |
| Other resource | Resource key not matching any of the previous |  |

Most checks apply at property value level, but some coherency checks have a larger scope (whole property file, whole multi-language bundle)

| Resource type | Check | Severity | Description | Impact | Example |
|------------------|----------|-------------|----------------|-----------|------------|
| All | Duplicate keys | Error | Same key is used twice in same property file | Unspecified behaviour on which value will get loaded | - |
| Translated | Missing translations | Warn | Resource is translated in at least 2 languages excluding root bundle (meaning translation process has somehow started), but not all languages | Default language/nothing/???key??? is displayed depending on implementation | - |
| Translated | Identical translations | Info | Resource has identical translations in all languages, it could be moved to root bundle | Duplication, risk of de-synchronization between languages | - |
| Translated | Almost identical translations | Warn | Resource has identical translations in most languages, but a different one in few (less than half) languages, suggesting it may not be language dependent and a typo error. |  | - |
| Translated | Trailing [whitespace](http://docs.oracle.com/javase/6/docs/api/java/lang/Character.html#isWhitespace%28char%29) | Warn | Resource has a trailing character, that could indicate some translation concatenation. | Syntax errors in some languages (spaces, order of words) | `example.text.start=Hello` `example.text.end= !` |
| Translated | Spellcheck | Warn | Resource has some spell-checking errors, using [Jazzy Spellchecker](http://sourceforge.net/projects/jazzy/). Dictionaries have to be provided to the plugin. Proper nouns (brands, ...) and other domain specific keywords or terms can be added to a dedicated user-defined dictionary (language dependant or not) to reduce false positives. | Syntax errors |  |
| Parametric | Unescaped single quotes | Error | Single quotes are not escaped by another single quote. Limitation: Single quotes used as escape sequence (ex: '{this text is escaped}') will raise false positive | Single quotes swallowed by MessageFormat, or formatting not applied | `example.param=this is {0}'s book` |
| Parametric | Incoherent parameters | Warn | Usage of parameters between languages is incoherent (different number of parameters, or different index used), the order of the parameters being ignored. Limitation: Types are ignored here, {0, date} is considered the same as {0}  | Translation has no meaning | bundle\_en.properties: `example.param=Hello {0} !` bundle\_fr.properties: `example.param=Bonjour {1} !` |
| Parametric | Escaped single quotes | Warn | Some single quotes are escaped but resource does not contain any formatting parameter. Limitation: if the parametric replacement is not performed by MessageFormat - for instance in javascript-, it is assumed it will follow same behaviour and consume escaped single quotes | Two single quotes displayed | `example.noparam=this is John''s book` |
| Parametric | Unknown substituted resource | Error | If properties are loaded via a library allowing resource substitution (such as [eProperties](https://eproperties.googlecode.com/svn/docs/1.1.1/manual/syntax.html#substitution), [XProperties](http://www2.sys-con.com/ITSG/virtualcd/Java/archives/0612/mair/index.html) or [Apache commons](http://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html#Variable_Interpolation)), checks that the substituted resource actually exists |  | `example.included = Re-used text example.text = ${example.inclued}` |
| HTML | XHTML validation | Error | XHTML validation fails using [XHTM 1.0 transitional](http://www.w3.org/TR/xhtml1/) (default), XHTML 1.0 strict or [XHTML 5](http://www.w3.org/TR/html5/the-xhtml-syntax.html#the-xhtml-syntax) (using [XMLmind schema](http://www.xmlmind.com/xhtml5_resources.shtml)). Note: data-**attributes are ignored during validation**| Malformed HTML, dependent on browser quirks | `example.html=some <b>important<b> text` `example.html=bed &amp; breakfast` |
| HTML | Incoherent tags | Warn | HTML tag usage is not coherent between translations of a resource (order is ignored). Limitation: nested tags coherence is not checked | Incoherent translations | bundle\_en.properties: `example.text=This is <b>important</b>` bundle\_fr.properties: `example.text=C'est <strong>important</strong>` |
| Js | Newline characters | Error | Some newline characters (\n, \r) are present. They are interpreted as end of javascript statement by browsers | Javascript error | `example.js=Some client side text\n` |
| Js | Unescaped quotes | Error | Quotes are unescaped  (" or ' depending on configuration). | Javascript error | `example.js=click on "Ok"` `var text="<fmt:message key='example.js'/>";` |
| URL | Pseudo-URL syntax | Error | Not a valid (pseudo-)URL pattern. Limitaion: only https(s),ftp and mailto protocol allowed, if any | Invalid link (Unknown address) | `example.url=http//www.google.com` |
| URL | HTML imports | Error | URL is an HTML import and does not support https context (cf [scheme relative URLs](http://stackoverflow.com/questions/3583103/network-path-reference-uri-scheme-relative-urls)). | Mixed content warning | `example.url=http://www.google.com/script.js` |
| Plain text | No HTML | Error | Contains HTML | Tags not interpreted by browser | `example.alt=alternative text<br/>of an image` |
| Plain text | No URL | Error | Contains URL | URLs not clickable | `example.alt=Go to http://www.google.com` |
| Custom | Custom pattern | Error | [custom pattern](CustomPatterns.md) regex doesn't match. |  |  |
| Other | Wrong resource type | Warn | Contains HTML or URL but wasn't checked, so probably some key patterns are wrong in plugin configuration |  | `example.other=This is <br/> actually HTML` |
| Other | Excluded | Info | Resource has been skipped using _excludedKeys_ configuration property. If this was to bypass a false positive raised by the plugin for a somehow valid case, feel free to [report a bug](https://github.com/rquinio/l10n-maven-plugin/issues) ;-)  | No check performed |  |

There are 3 severity levels:
 * **Error**: Serious issue that will break the application in all cases. By default will fail the build (this is configurable via _ignoreFailure_ configuration property).
 * **Warn**: Issue that may impact the application in some cases, incoherence, or bad practice. Can be ignored via --quiet trigger
 * **Info**: Minor issue or recommendation. Can be ignored via --quiet trigger

## References

  * [java.util.Properties#load](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Properties.html#load%28java.io.InputStream%29) Javadoc
  * [java.text.MessageFormat](http://docs.oracle.com/javase/1.5.0/docs/api/java/text/MessageFormat.html) and [java.util.Formatter](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html) Javadoc.
  * [Javascript special characters](http://www.w3schools.com/js/js_special_characters.asp)
  * [java.util.Pattern](http://docs.oracle.com/javase/1.5.0/docs/api/java/util/regex/Pattern.html) Javadoc
  * [JSON specification](http://json.org/)
