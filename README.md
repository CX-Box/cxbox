<h2 align="center">CXBOX CORE</h2>
<h4 align="center">CXBOX - Rapid Enterprise Level Application Development Platform</h4>

<p align="center">
<a href="http://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat" alt="license" title=""></a>
</p>


<div align="center">
  <h3>
    <a href="https://www.cxbox.org/" target="_blank">
      Website
    </a>
    <span> | </span>
    <a href="https://www.demo.cxbox.org/" target="_blank">
      Demo
    </a>
    <span> | </span>
    <a href="https://www.doc.cxbox.org/" target="_blank">
      Documentation
    </a>
  </h3>
</div>

## Description
CXBOX main purpose is to speed up development of typical Enterprise Level Application based on Spring Boot. A fixed contract with a user interface called [Cxbox-UI](https://github.com/CX-Box/cxbox-ui) allows backend developer to create typical interfaces providing just Json meta files. Full set of typical Enterprise Level UI components included - widgets, fields, layouts (views), navigation (screens). 

## Using CXBOX
To get started, 
- [download Intellij Plugin](https://plugins.jetbrains.com/plugin/19523-tesler-helper). [Intellij Plugin](https://plugins.jetbrains.com/plugin/19523-tesler-helper) adds platform specific autocomplete, inspection, navigation and code generation features.

- [download Demo](https://github.com/CX-Box/cxbox-demo) and follow [README.md](https://github.com/CX-Box/cxbox-demo#readme) instructions. Feel free to use demo as template project to start your own projects

## Building CXBOX CORE From Source

- Checkout the repository:

```bash
git clone https://github.com/CX-Box/cxbox.git
```

- Install JDK 11.

- Build framework modules to the local Maven:

```bash
mvn clean install
```

## Database support

The following databases are currently supported:

- Postgresql 9.6.15 and later
- Oracle 11g and later


## Contributing

Please check ours [contributing guide](./CONTRIBUTING.md)

## License
CXBOX is an open-source project with the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) license.