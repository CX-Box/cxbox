# Cxbox starter for Quartz

## Prerequisites
Your project uses cxbox with cxbox-starter-parent, e.g. you have in your pom.xml:
```
<parent>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-starter-parent</artifactId>
    <version>CHANGE_ME</version>
</parent>
```

## Getting started
### Dependency
In your pom.xml add
```
<dependency>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-starter-notifications-model</artifactId>
</dependency>
```
### Liquibase migrations

In your liquibase change log check following line is included:

```
<include file="classpath:io/cxbox/db/changelog/notifications/cxbox-starter-notifications.xml" relativeToChangelogFile="false"/>
```

### (Optional) Liquibase migrations
Alternatively one can copy Liquibase migrations files directly to project

