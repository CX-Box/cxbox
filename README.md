Cxbox
=========
Cxbox is a framework that allows you to quickly develop applications using the full power of existing enterprise Java solutions.
---

## Core features

Cxbox includes:

- Abstraction of a business component to simplify access to data;
- A fixed contract with a user interface called [Cxbox-UI](https://github.com/CX-Box/cxbox-ui), which allows you to create typical interface elements in the form of Json files;
- A single DAO layer, simplifying work with JPA;
- The SQL engine that allows you to quickly generate typical business components in the application;
- Abstraction of the task scheduler to create background tasks;
- Built-in BPM - an engine that allows you to create business processes.

## Projects

Cxbox is based on the following opensource projects:

- Spring 5.x and Spring Boot 2.x;
- Hibernate as a JPA implementaton;
- Liquibase for database migration;
- Quartz for planning tasks;
- Etc.

## Database support

The following databases are currently supported:

- Postgresql 9.6.15 and later
- Oracle 11g and later

## Versioning

Cxbox follows [semver](https://semver.org/), e.g. MAJOR.MINOR.PATCH
All significant changes are documented in our [changelog file](./CHANGELOG.md).  
Backwards incompatible changes are denoted with `[BREAKING CHANGE]` mark

## Contributing

Please check ours [contributing guide](./CONTRIBUTING.md)
