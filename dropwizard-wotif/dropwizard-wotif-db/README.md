dropwizard-wotif-db
-------------------

dropwizard-wotif-db extends dropwizard's DataSourceFactory configuration with appropriate defaults
we arrived at in consultation with the DBAs.

config.yaml
-----------
```
db:
    url: jdbc:oracle:thin:@//oraprimary:1521/wotif_misc
    user: foo
    password: far
```

Configuration.java
------------------
```
@Valid
@NotNull
@JsonProperty
private WotifDataSourceFactory db;
```

Application.java
----------------
```
ManagedDataSource dataSource = config.getDb().build(environment.metrics(), "db");
environment.lifecycle().manage(dataSource);
environment.healthChecks().register("db", new DataSourceHealthCheck(dataSource, config.getDb()));
```
