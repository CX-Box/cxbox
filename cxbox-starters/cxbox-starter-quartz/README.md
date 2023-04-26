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
    <artifactId>cxbox-starter-quartz</artifactId>
</dependency>
```
In your app add bean corresponding to your database vendor:
```
@Bean(name = "primaryDatabase")
Database primaryDatabase() {
	return Database.POSTGRESQL;
}
```
or 
```
@Bean(name = "primaryDatabase")
Database primaryDatabase() {
	return Database.ORACLE;
}
```
### Liquibase migrations

In your liquibase change log check following line is included:

```
<include file="classpath:io/cxbox/db/changelog/quartz/cxbox-starter-quartz.xml" relativeToChangelogFile="false"/>
```

### (Optional) Liquibase migrations
Alternatively one can copy Liquibase migrations files directly to project

