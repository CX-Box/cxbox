<h2 align="center">CXBOX CORE</h2>
<div align="center">
<a href="https://github.com/CX-Box/cxbox/actions/workflows/build_main.yml"><img src="https://github.com/CX-Box/cxbox/actions/workflows/build_main.yml/badge.svg" title="">
</a>
<a href="https://sonarcloud.io/summary/overall?id=CX-Box_cxbox"><img src="https://sonarcloud.io/api/project_badges/measure?project=CX-Box_cxbox&metric=alert_status&branch=main" alt="sonar" title="">
</a>
</div>

<blockquote>
<div> 
<p align="center">
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



<h3>Description</h2>
<p>
CXBOX main purpose is to speed up development of typical Enterprise Level Application based on Spring Boot. A fixed
contract with a user interface called <a href="https://github.com/CX-Box/cxbox-ui" target="_blank">Cxbox-UI</a> allows backend developer to create
typical interfaces providing just Json meta files. Full set of typical Enterprise Level UI components included -
widgets, fields, layouts (views), navigation (screens).
</p>
</div>

<h3>Using CXBOX</h2>
<ul>
<li> <a href="https://plugins.jetbrains.com/plugin/19523-tesler-helper" target="_blank">download Intellij Plugin</a> adds platform specific autocomplete, inspection, navigation and code generation features.
</li>
<li>
 <a href="https://github.com/CX-Box/cxbox-demo" target="_blank">download Demo</a> and follow <a href="https://github.com/CX-Box/cxbox-demo#readme" target="_blank">README.md</a> instructions. Feel free to use demo as template project to start your own projects
</li>
</ul>
</blockquote>

# CXBOX CORE

## Building From Source

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