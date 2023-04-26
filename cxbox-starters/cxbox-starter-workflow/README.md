# Cxbox starter for Workflow

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
    <artifactId>cxbox-starter-workflow-api</artifactId>
</dependency>
<dependency>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-starter-workflow-core</artifactId>
</dependency>
<dependency>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-starter-workflow-model</artifactId>
</dependency>
```
### Liquibase migrations

In your liquibase change log check following line is included:

```
<include file="classpath:io/cxbox/db/changelog/workflow/cxbox-starter-workflow.xml" relativeToChangelogFile="false"/>
```

this file actually resides in:

```
<dependency>
    <groupId>org.cxbox</groupId>
    <artifactId>cxbox-starter-workflow-model</artifactId>
</dependency>
```
### (Optional) Liquibase migrations
Alternatively one can copy Liquibase migrations files directly to project

### (OPTIONAL) Liquibase load data
 Fill CHANGE_ME with you project path and add next changeset 
```
<changeSet author="initial" id="TASK_FIELD" runOnChange="true">
    <loadUpdateData tableName="TASK_FIELD" primaryKey="ID" file="CHANGE_ME/TASK_FIELD.csv"
      separator=";">
      <column name="ID" header="ID" type="NUMERIC"/>
      <column name="KEY" header="KEY" type="STRING"/>
      <column name="TITLE" header="TITLE" type="STRING"/>
    </loadUpdateData>
  </changeSet>
```




