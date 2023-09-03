[![Maven Central](https://img.shields.io/maven-central/v/com.randomnoun.common/jessop.svg)](https://search.maven.org/artifact/com.randomnoun.common/jessop)

# Jessop

**Jessop**  is a polyglot templating language in the style of Java Server Pages (JSPs).

Like all templating languages, output can be any text stream ( e.g. English text, HTML/CSS or other text-based programming or markup languages), but the statements, conditions and expressions within the language can be written in any language that the Java scripting engine ( javax.script ) supports.

_e.g._  by enabling the '**javascript**' target language, you can embed javascript code within your jessop script (which executes using the 'rhino' engine), or by enabling the '**python**' target language, you can embed python code within your jessop script (which executes using the 'jython' engine).

The jessop syntax should be familiar to anyone who has written JSPs. A file may contain

-   a  **declaration**  in the form
    
```<%@ jessop language="javascript" engine="rhino" %>```
    
-   **expressions**  to generate output in the form
    
```<%= name %>```
    
-   **scriptlets**  to execute arbitrary code in the form
    
```
<% if (name==null) { %>
    
    No name
    
<% } %>
```
    

See the  [Javadoc overview](https://randomnoun.github.io/jessop/apidocs/index.html)  for a more complete description of the  **jessop**  syntax.

## Why another template language ?

See http://www.randomnoun.com/wp/2016/07/04/2897/

## What languages can be used within jessop ?
* Javascript ( via rhino or nashorn )
* Python ( jython )
* Ruby ( jruby )
* Lua ( luaj )
* Lisp ( abcl )

## Licensing

Jessop is licensed under the BSD 2-clause license.

## Anything else ?

Well, you could check out the examples and generated site documentation here: https://randomnoun.github.io/jessop/
