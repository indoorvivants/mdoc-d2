# D2 diagrams in your Mdoc files

<!--toc:start-->
- [D2 diagrams in your Mdoc files](#d2-diagrams-in-your-mdoc-files)
  - [Installation](#installation)
  - [Usage](#usage)
  - [Contributing](#contributing)
<!--toc:end-->

[Mdoc](https://scalameta.org/mdoc/) is _the_ tool for compiled documentation in Scala projects

[D2](https://d2lang.com/) is a declarative diagram definition language

This project is a simple plugin for mdoc that allows you to embed D2 diagrams in 
your markdown files.

## Installation

- SBT: `libraryDependencies += "com.indoorvivants" %% "mdoc-d2" % "@VERSION@"`
- Mill: `def ivyDeps = Agg(ivy"com.indoorvivants::mdoc-d2:@VERSION@")`

|                | JVM  | 
| -------------- | ---  | 
| Scala 2.12  | ✅   | 
| Scala 2.13   | ✅   | 
| Scala 3    | ✅   | 


## Usage

You don't need to install D2, it will be bootstrapped by [Yank](https://github.com/indoorvivants/yank#d2).

Simple use the `scala mdoc:d2` language in your markdown snippets and enjoy:

````
```scala mdoc:d2
direction:right
dogs -> cats -> mice: chase
replica 1 <-> replica 2
a -> b: To err is human, to moo bovine {
  source-arrowhead: 1
  target-arrowhead: * {
    shape: diamond
  }
}
```
````

will be rendered as an image:

```scala mdoc:d2
direction:right
dogs -> cats -> mice: chase
replica 1 <-> replica 2
a -> b: To err is human, to moo bovine {
  source-arrowhead: 1
  target-arrowhead: * {
    shape: diamond
  }
}
```

You can also pass the parameters directly to D2 CLI tool, by using the special `#!` comments. For example, here we set layout to ELK and theme to 100:

````
```scala mdoc:d2
#!layout=elk
#!theme=100
direction:right
dogs -> cats -> mice: chase
replica 1 <-> replica 2
a -> b: To err is human, to moo bovine {
  source-arrowhead: 1
  target-arrowhead: * {
    shape: diamond
  }
}
```
````

will be rendered as an image:

```scala mdoc:d2
#!layout=elk
#!theme=100
direction:right
dogs -> cats -> mice: chase
replica 1 <-> replica 2
a -> b: To err is human, to moo bovine {
  source-arrowhead: 1
  target-arrowhead: * {
    shape: diamond
  }
}
```

## Contributing

If you want to update this documentation file, don't edit it directly - edit [docs/README.in.md](/docs/README.in.md) and run `sbt updateDocs`. It's annoying, but this document contains compiled snippets of code which I want to prevent from going out of date


