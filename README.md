DataQuery
===========
A java library for working with various data sources.  It currently has support for Oracle, Postgres, H2, SQLite, and Sharepoint (lists).
It is essentually a wrapper around JDBC for relational databases, and when working with sharepoint uses the sharepoint [URL Protocol](docs/sharepoint_url_protocol.pdf).  There are two APIs in the library, the original API and a more recent Fluent API.

Connecting To A Database
------------------------
Since DataQuery uses JDBC, create a datasource or connection pool using your JDBC documentation.  For example, using an Oracle Connection Pool:

```java
OracleDataSource dataSource = new OracleDataSource();
java.util.Properties prop = new java.util.Properties();
prop.setProperty("MinLimit", "0");
prop.setProperty("MaxLimit", "5");
prop.setProperty("InactivityTimeout","1800");
dataSource.setConnectionCachingEnabled(true);
dataSource.setURL("jdbc.url");
dataSource.setUser("jdbc.user");
dataSource.setPassword("jdbc.pass");
dataSource.setLoginTimeout(2);
dataSource.setConnectionCacheProperties(prop);
```
Regarding connection pools, this is not an endorsement of the above method.  Oracle currently recommends using the Oracle Universal Connection ([UCP](http://docs.oracle.com/cd/E11882_01/java.112/e12265/toc.htm)) pool although it is my experience that the UCP can have issues under load.  Granted, I have not tested it in a while.  Currently we prefer using the [HikariCP Connection Pool](https://github.com/brettwooldridge/HikariCP) .

Configuring an Entity
---------------------
Similar to [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API) DataQuery uses annotations to configure entities.  Below is an example entity with annotations.

```java
@Entity(useCamelCase=false,
        schema="NWP")
public class LeveeSystemAlignLine implements NldFeature{
    private Integer id;
    private Long alignId;
    private Double fcSegmentId;
    private String alignTypeD;
    private JGeometry shape;
    private Double measure; //optional.  not part of table but sometimes returned from select query

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="SEQ_LEVEE_SYSTEM_ALIGN_LINE")
    public Integer getObjectid() {
        return objectid;
    }

    public void setObjectid(Integer objectid) {
        this.objectid = objectid;
    }

    @Column(name="ALIGNMENT_ID")
    public Long getAlignId() {
        return alignId;
    }

    @Column(name="ALIGNMENT_ID")
    public void setAlignId(Long alignId) {
        this.alignId = alignId;
    }

    public Double getFcSegmentId() {
        return fcSegmentId;
    }

    public void setFcSegmentId(Double fcSegmentId) {
        this.fcSegmentId = fcSegmentId;
    }

    public String getAlignTypeD() {
        return alignTypeD;
    }

    public void setAlignTypeD(String alignTypeD) {
        this.alignTypeD = alignTypeD;
    }

    @Override
    public JGeometry getShape() {
        return shape;
    }

    public void setShape(JGeometry shape) {
        this.shape = shape;
    }

    @Optional
    public Double getMeasure() {
        return measure;
    }

    @Optional
    public void setMeasure(Double measure) {
        this.measure = measure;
    }

    @Transient
    public int getFeatureCount(){
        //....enumerate geom to get feature count
        return featureCount;
    }

}


```
Entity Conventions:
------------------
  - Table and getter/setter names:
    - by convention table and bean names are mapped to the database by converting camel cased syntax to 'underscores' for the database.  
      - For example, the entity class above is by convention mapped to the table 'levee_system_align_line'
      - Correspondingly, the methods getObjectId and setObjectId are mapped to the field 'object_id'.


Annotations:
--------------
  - @Entity:
     - schema: optional string for database schema if entity is not in connection users schema.  
     - useCamelCase: optional. default 'false'. When true, maps getters/setters to camel case.
       - using the convention examples above and useCamelCase=true:
         - the entity class above is now mapped to table named 'leveeSystemAlignLine'
         - the methods getObjectId and setObjectId are mapped to 'objectId'
     - table: optional table name if the table name, by convention, is not based on the class name;
     - sql: an SQL statment if user defined SQL should be used to query the database.
  - @Column:
    - an optional database column name which will override the standard conventions.
  - @Id: annotates the getter method for the database ID field
  - @GeneratedValue: annotates the getter method for the database Id field with info related to getting new key values.
    - GenerationType:
  - @Transient: annotates getters and setters.  Queries will ignore fields with this annotation.
  - @Optional : Not recommended.  Can cause significant performance problems.  Purpose originally is for entity fields that will be returned in some queries but not in others.

#Using:
-------

##FLUENT API
The fluent API is used via the DbQuery class.  The DbQuery class can be constructed using a connection or using a database type and url.
```java
DbQuery db = new DbQuery(connectionPool.getConnection());
DbQuery db = new DbQuery(DB.ORACLE,dbUrl);
db.close();
```

The DbQuery class implements automatic resource management so it is recommeded to use the Java 7 try with syntax.

```java
try(DbQuery db = new DbQuery(connectionPool.getConnection())){
  //...do stuff here
  //...DbQuery will automatically release all resources
}
```

###SELECT
Select statement can either map a resultset into an object, or return simple java collections.  The types of select statements that can be used are presented below.

######Map select statement to an entity

```java
//get a list of object
List<LeveeSystemAlignLine> list=db.select(LeveeSystemAlignLine.class)
                                  .criteria("where object_id in (?,?,?)")
                                  .params(100,101,102)
                                  .fetch();

//get a single object
LeveeSystemAlignLine line = db.select(LeveesystemAlignLine.class)
                              .criteria("where object_id=?")
                              .params(100)
                              .fetchRow();

//use full sql command as opposed to object fields
List<LeveeSystemAlignLine> list = db.select(LeveeSystemAlignLine.class)
                                    .sql("select * from levee_system_align_line")
                                    .fetch();

```
Note: Order of precedence for SQL statements is:
  - SQL included in SQL function
  - SQL included in Entity annotation
  - SQL derived from Entity Bean fields

######Map select statement to simple collections
DbQuery provides a number of ways to map query results into other data structures.  These are documented below:

######ArrayList: Map a single field into a List
```java
 List<Double> testRecords = db.select(ReturnType.ARRAYLIST)
                             .sql("select depth from test_table1 where test_four>?")
                             .returnedTypes(Double.class) //optionally provide return type
                             .params(4)
                             .fetch();
```

######ArrayList of Tuple: Map two fields into a List
```java
List<Tuple<Integer,Double>> testRecords =db.select(ReturnType.ARRAYLIST_TUPLE)
                                           .sql("select test_four,depth from test_table1 where test_four<?")
                                           .returnedTypes(Integer.class,Double.class)
                                           .params(2)
                                           .fetch();
```

######ArrayList of Tuple3: Map 3 fields into a List
```java
List<Tuple3<Integer,Double,String>> testRecords = db.select(ReturnType.ARRAYLIST_TUPLE3)
                                                    .sql("select test_four,depth,test_one from test_table1 where id<?")
                                                    .returnedTypes(Integer.class,Double.class, String.class)
                                                    .params(2)
                                                    .fetch();
```

######ArrayList of List: Map an arbitrary number of fields into a List (a.k.a. RECORDSET)
```java
List<List> testRecords = db.select(ReturnType.RECORDSET)
                           .sql("select test_four,depth,test_one,test_three from test_table1 where test_four<?")
                           .returnedTypes(Integer.class,Double.class, String.class, Date.class)
                           .params(2)
                           .fetch();

for(List record:records){
    sum+=(double)record.get(1);
}
```

######ArrayList of Map (a.k.a RECORDSETMAP)
```java
List<HashMap<String,Object>> records = db.select(ReturnType.RECORDSETMAP)
                                         .sql("select id,depth,test_four from test_table1")
                                         .returnedTypes(Integer.class,Double.class,Integer.class)
                                         .fetch();

for(HashMap record:records){
    sum+=(double)record.get("DEPTH");
}
```

###### Get a single Value: return only one value from a query.  Use 'fetchValue()'
```java
Double sumDepth= db.select(ReturnType.SINGLEVALUE)
                   .sql("select sum(depth) from test_table1 where test_four<?")
                   .returnedTypes(Double.class)
                   .params(2)
                   .fetchValue();
```

###### Get a hashmap of two fields. Creates a hashmap from query where the 1st field returned is the key and the 2nd field is the value.
```java
HashMap<Integer, String> recordMap = db.select(ReturnType.HASHMAP)
                                   .sql("select test_four,test_one from test_table1 where test_four<?")
                                   .returnedTypes(Integer.class, String.class)
                                   .params(2)
                                   .fetchMap();
```

######Quick and dirty query to json.  All fields converted to json strings.  Recommend using more robust JSON libraries like GSON, but this useful if the data is simple or memory utilization is a premium.
```java
String jsonRecords = db.select(ReturnType.JSON)
                       .sql("select test_four from test_table1 where test_four<?")
                       .params(2)
                       .fetchValue();
```


###Cursors
For large datasets it might be preferable to enumerate over the result set as opposed to loading the result set into a collection. DbQuery provides a a method for this with by invoking 'fetchCursor()' as opposed to 'fetch()'.  The difference is that instead of returning a List, fetchCursor returns a Cursor iterator.  

```java
//for cursor, do not invoke autoclose on dbquery.  Return the cursor and wrap the cursor in a try-with-resources where it is used.
public Cursor<LeveeSystemAlignLine> getRecordCursor(){
    DbQuery db = new DbQuery(connectionPool.getConnection();
    Cursor<LeveeSystemAlignLine> records = db.select(LeveeSystemAlignLine.class)
                                                    .sql("select * from levee_system_align_line")
                                                    .fetchCursor();
    return records;
}

public doSomethingWithRecords(){
    try(Cursor<LeveeSystemAlignLine> records = getRecordCursor()){
        //...do stuff here
        //...Cursor will automatically close resources
    }
}

```

###INSERT
Insert statement examples for Entities are below:
```java
db.insert()
   .records(testList)
   .useBatch(false) //optional default=true
   .useDeclaredOnly(true) //optional default=false
   .execute();

db.insert()
   .records(testList)
   .batchSize(1000) //default is 100.  Generally 100 is a good value
   .execute();

//can also insert a single record
db.insert()
   .record(testRecord)
   .execute();

//when inserting a single record, can request the new ID if primary key is autogenerated.
//ID is returned in a GeneratedKey object.
GeneratedKey newKey = db.insert()
                        .record(testRecord)
                        .returnKey(true)
                        .execute();

String idFieldName=newKey.getName();
Object idFieldValue=newKey.getValue();
```

###UPDATE
Update statement examples for Entities are below:
```java
db.update()
  .records(testRecords)
  .execute();

//or for a single record

db.update()
  .record(testRecord)
  .execute();
```

###DELETE
Delete statement examples are below:
```java
//delete using an entity, criteria and params
db.delete(TestTable1.class)
  .criteria("where id>?")
  .params(100)
  .execute();

//alternatively can delete a record or a list of records
db.delete()
  .record(record)
  .execute();

db.delete()
  .records(records)
  .execute();  
```

###EXECUTE UPDATE
Queries can be passed directly to jdbc using execute update by passing sql and params
```java
String sql=
db.executeUpdate()
  .sql("delete from test where id between ? and ?")
  .params(1,100)
  .execute();

db.executeUpdate
  .sql("insert into test values (?,?,?)")
  .params(10,'Test',12.123)
  .execute();
```


###DBSTRUCT
It is also possible to annotate an entity as a DbStruct and get typsafe recordset without having to implement an entity bean (getters/setters).  If the entity is annotated with DbStruct, then public fields in the class will be mapped to the database recordset as opposed to getter/setter methods.

example:  This example will execute the provided SQL and map into MyDatabaseView class.
```java
@Entity(sql="select t1.field1, t1.field2, t2.field3
            from table1 t1, table2 t2
            where t1.id=t2.table1_id and t1.field3 between ? and ?")
@DbStruct
public static class MyDatabaseView {
  public Date field1,
  public Double field2,
  public Integer field3  
}

List<MyDatabaseView> = db.select(MyDatabaseView.class)
                         .params(10,100)
                         .fetch();

```
another example: maps to a entity in the dabase named VW_TEST
```java
@DbStruct
public static class VwTest {
  public Date field1,
  public Double field2,
  public Integer field3  
}

List<MyDatabaseView> = db.select(MyDatabaseView.class)
                         .critieria("where field1=?")
                         .params(10)
                         .fetch();

```

#Transactions
Transactions can be implemented by directly invoking transaction methods from the NativeRdbmsQuery reference in the fluent API or by wrapping the database methods in a Transaction block.
```java
//using RdbmsQuery
DbQuery db = new DbQuery(connectionPool.getConnection());
try{
  db.rdq.startTransaction();
  //do stuff
  db.rdq.commitTransaction();
}
catch(SQLException ex){
  db.nrq.rollbackTransaction();
}
finally{
  db.close();
}

//or using transaction block (Java 7)

db.transaction(new Transaction(){
    @Override
    public void transaction() {
        db.insert()....
        db.update().....
    }  
});

//or transaction block Java 8
db.transaction(()->{
  db.insert()...
  db.update()...
})
```
#Date Considerations
For inserts:
  - When using an entity, java.util.Date will be automatically converted to java.sql.Date.  Rule is to use the java.util.Date in entity beans.
  - When using 'executeUpdate' methods, there is no implicit type conversion for dates.  Consequenty the user must provide a java.sql.Date as the bound parameter.
