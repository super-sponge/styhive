学习研究Hive相关技术
======================

### hive 指向hdfs更改名字
    hive 指向hdfs更改名字后，hive中的表依然指向以前的hdfs，此中情况需要手工更改元数据（此语句针对的是hive 1.1.0, 其他版本请分析后执行
    localhost 原来主机名字,host29新主机名字
    set sql_safe_updates = 0;
    
    UPDATE hive.SDS  SET LOCATION=REPLACE(LOCATION,'localhost','host29') ;
    update hive.DBS set DB_LOCATION_URI =REPLACE(DB_LOCATION_URI,'localhost','host29') ;
    select * from hive.SDS;
    select * from hive.DBS;

### hiveUDF 程序依赖程序包 groupID org.apache.hive
* hive-exec
* hive-common
* 同时需要hadoop的hadoop-common
* hive 版本为0.14.0时在pom.xml中需要添加


    <repositories>
    <repository>
    <id>conjars.org</id>
    <url>http://conjars.org/repo</url>
    </repository>
    </repositories>

    <dependency>
    <groupId>org.apache.calcite</groupId>
    <artifactId>calcite-core</artifactId>
    <version>0.9.2-incubating</version>
    </dependency>
    <dependency>
    <groupId>org.apache.calcite</groupId>
    <artifactId>calcite-avatica</artifactId>
    <version>0.9.2-incubating</version>
    </dependency>


### hiveUDF使用

      add jar /home/hadoop/styhive-1.0-SNAPSHOT.jar;

      CREATE TEMPORARY FUNCTION decode AS 'com.example.UDFDecode';
      CREATE TEMPORARY FUNCTION encode AS 'com.example.UDFEncode';
      CREATE TEMPORARY FUNCTION msqrt AS 'com.example.UDFMSqrt';

      select encode('facebook') from tmp;
      select encode('facebook','fkey','skey','tkey') from tmp;

      select decode('1CC7376126B8AE1DE343E4C20EAE9ADA') from tmp;
      select decode('5BB6A40B0CEA149B0A1645E74C7E460C','fkey','skey','tkey') from tmp;
### hiveJdbc 使用
    详细参考 com.sponge.srd.hive.HiveServer2Client

---

## hive 常用命令
### system command
    ! <command>
    dfs <dfs command>
    <query string>
    source FILE <filepath>

    set [-v]
    reset
    exit/quit
### Hive Resources
    add [FILE|JAR|ARCHIVE] <value> [<value>]*
    list [FILE|JAR|ARCHIVE] [<value> [<value>]*]
    delete [FILE|JAR|ARCHIVE] <value> [<value>]*
### logging
    hive --hiveconf hive.root.logger=INFO,console
### Hive Command Line Options

     -d,--define <key=value>          Variable substitution to apply to hive
                                      commands. e.g. -d A=B or --define A=B
     -e <quoted-query-string>         SQL from command line
     -f <filename>                    SQL from files
     -H,--help                        Print help information
     -h <hostname>                    Connecting to Hive Server on remote host
        --hiveconf <property=value>   Use value for given property
        --hivevar <key=value>         Variable substitution to apply to hive
                                      commands. e.g. --hivevar A=B
     -i <filename>                    Initialization SQL file
     -p <port>                        Connecting to Hive Server on port number
     -S,--silent                      Silent mode in interactive shell
     -v,--verbose                     Verbose mode (echo executed SQL to the
                                      console)
### Examples
    hive -e 'select a.col from tab1 a' --hiveconf hive.exec.scratchdir=/home/my/hive_scratch  --hiveconf mapred.reduce.tasks=32

### HiveServer2 Clients（beeline)
    !connect jdbc:hive2://host29:10000 hadoop hadoop org.apache.hive.jdbc.HiveDriver
    beeline -u jdbc:hive2://host29:10000 hadoop hadoop org.apache.hive.jdbc.HiveDriver
    
    SQuirrel SQL Client 配置（hiveserver 必须启动）
        拷贝$HIVE_HOME/lib下面的jar到Extra class path 
        拷贝hadoop-common*jar 到Extra class path
        根据操作即可连接hive

---

# hive DDL 基本语法
## hive 数据库操作
### 基本语法
    CREATE (DATABASE|SCHEMA) [IF NOT EXISTS] database_name
      [COMMENT database_comment]
      [LOCATION hdfs_path]
      [WITH DBPROPERTIES (property_name=property_value, ...)];

    DROP (DATABASE|SCHEMA) [IF EXISTS] database_name [RESTRICT|CASCADE];

    ALTER (DATABASE|SCHEMA) database_name SET DBPROPERTIES (property_name=property_value, ...);
    ALTER (DATABASE|SCHEMA) database_name SET OWNER [USER|ROLE] user_or_role;

    describe database database_name;

### 注意事项
* database 与 schema 表达的是同一个意思
* localtion 默认为${hive.metastore.warehouse.dir}/database.db 即在hive元数据目录下面建立一个数据库名字加上.db后缀的目录

### 实例
    beeline
    !connect jdbc:hive2://host29:10000 hadoop hadoop org.apache.hive.jdbc.HiveDriver
    create database testdb;
    describe database testdb;
    dfs -ls /user/hive/warehouse
    alter database testdb set owner role public;
    describe database testdb;

    use testdb;
    create table testtb(key string, value string);
    load data local inpath '../data/files/kv1.txt' overwrite into table testtb;
    select * from testtb limit 10;
    dfs -ls /user/hive/warehouse/testdb.db;
    dfs -cat /user/hive/warehouse/testdb.db/testtb/kv1.txt;

    drop database testdb cascade;
    dfs -ls /user/hive/warehouse

## hive 表操作
### 基本语法
#### 创建表
    CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name    -- (Note: TEMPORARY available in Hive 0.14.0 and later)
      [(col_name data_type [COMMENT col_comment], ...)]
      [COMMENT table_comment]
      [PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)]
      [CLUSTERED BY (col_name, col_name, ...) [SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS]
      [SKEWED BY (col_name, col_name, ...)                  -- (Note: Available in Hive 0.10.0 and later)]
         ON ((col_value, col_value, ...), (col_value, col_value, ...), ...)
         [STORED AS DIRECTORIES]
      [
       [ROW FORMAT row_format]
       [STORED AS file_format]
         | STORED BY 'storage.handler.class.name' [WITH SERDEPROPERTIES (...)]  -- (Note: Available in Hive 0.6.0 and later)
      ]
      [LOCATION hdfs_path]
      [TBLPROPERTIES (property_name=property_value, ...)]   -- (Note: Available in Hive 0.6.0 and later)
      [AS select_statement];   -- (Note: Available in Hive 0.5.0 and later; not supported for external tables)

    CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name
      LIKE existing_table_or_view_name
      [LOCATION hdfs_path];

    data_type
      : primitive_type
      | array_type
      | map_type
      | struct_type
      | union_type  -- (Note: Available in Hive 0.7.0 and later)

    primitive_type
      : TINYINT
      | SMALLINT
      | INT
      | BIGINT
      | BOOLEAN
      | FLOAT
      | DOUBLE
      | STRING
      | BINARY      -- (Note: Available in Hive 0.8.0 and later)
      | TIMESTAMP   -- (Note: Available in Hive 0.8.0 and later)
      | DECIMAL     -- (Note: Available in Hive 0.11.0 and later)
      | DECIMAL(precision, scale)  -- (Note: Available in Hive 0.13.0 and later)
      | VARCHAR     -- (Note: Available in Hive 0.12.0 and later)
      | CHAR        -- (Note: Available in Hive 0.13.0 and later)

    array_type
      : ARRAY < data_type >

    map_type
      : MAP < primitive_type, data_type >

    struct_type
      : STRUCT < col_name : data_type [COMMENT col_comment], ...>

    union_type
       : UNIONTYPE < data_type, data_type, ... >  -- (Note: Available in Hive 0.7.0 and later)

    row_format
      : DELIMITED [FIELDS TERMINATED BY char [ESCAPED BY char]] [COLLECTION ITEMS TERMINATED BY char]
            [MAP KEYS TERMINATED BY char] [LINES TERMINATED BY char]
            [NULL DEFINED AS char]   -- (Note: Available in Hive 0.13 and later)
      | SERDE serde_name [WITH SERDEPROPERTIES (property_name=property_value, property_name=property_value, ...)]

    file_format:
      : SEQUENCEFILE
      | TEXTFILE    -- (Default, depending on hive.default.fileformat configuration)
      | RCFILE      -- (Note: Available in Hive 0.6.0 and later)
      | ORC         -- (Note: Available in Hive 0.11.0 and later)
      | PARQUET     -- (Note: Available in Hive 0.13.0 and later)
      | AVRO        -- (Note: Available in Hive 0.14.0 and later)
      | INPUTFORMAT input_format_classname OUTPUTFORMAT output_format_classname

#### 更改表
    ALTER TABLE table_name RENAME TO new_table_name;

    ALTER TABLE table_name SET TBLPROPERTIES table_properties;

    table_properties:
      : (property_name = property_value, property_name = property_value, ... )

    ALTER TABLE table_name SET TBLPROPERTIES ('comment' = new_comment);
    ALTER TABLE table_name SET SERDEPROPERTIES ('field.delim' = ',');
    ALTER TABLE table_name CLUSTERED BY (col_name, col_name, ...) [SORTED BY (col_name, ...)]
      INTO num_buckets BUCKETS;
    ALTER TABLE table_name SKEWED BY (col_name1, col_name2, ...)
      ON ([(col_name1_value, col_name2_value, ...) [, (col_name1_value, col_name2_value), ...]
      [STORED AS DIRECTORIES];
    ALTER TABLE table_name NOT SKEWED;
    ALTER TABLE table_name NOT STORED AS DIRECTORIES;
    ALTER TABLE table_name SET SKEWED LOCATION (col_name1="location1" [, col_name2="location2", ...] );
    ALTER TABLE table_name ADD [IF NOT EXISTS] PARTITION partition_spec
      [LOCATION 'location1'] partition_spec [LOCATION 'location2'] ...;

    partition_spec:
      : (partition_column = partition_col_value, partition_column = partition_col_value, ...)

    ALTER TABLE table_name PARTITION partition_spec RENAME TO PARTITION partition_spec;
    ALTER TABLE table_name_1 EXCHANGE PARTITION (partition_spec) WITH TABLE table_name_2;
    MSCK REPAIR TABLE table_name;
    ALTER TABLE table_name DROP [IF EXISTS] PARTITION partition_spec[, PARTITION partition_spec, ...]
      [IGNORE PROTECTION] [PURGE];            -- (Note: PURGE available in Hive 1.2.0 and later)
    ALTER TABLE table_name DROP [IF EXISTS] PARTITION partition_spec IGNORE PROTECTION;
    ALTER TABLE table_name DROP [IF EXISTS] PARTITION partition_spec PURGE;
    ALTER TABLE table_name [PARTITION partition_spec] SET FILEFORMAT file_format;
    ALTER TABLE table_name [PARTITION partition_spec] SET LOCATION "new location";
    ALTER TABLE table_name TOUCH [PARTITION partition_spec];
    ALTER TABLE table_name [PARTITION partition_spec] ENABLE|DISABLE NO_DROP [CASCADE];
    ALTER TABLE table_name [PARTITION partition_spec] ENABLE|DISABLE OFFLINE;
    ALTER TABLE table_name [PARTITION (partition_key = 'partition_value' [, ...])]
      COMPACT 'compaction_type';
    ALTER TABLE table_name [PARTITION (partition_key = 'partition_value' [, ...])] CONCATENATE;

    ALTER TABLE table_name [PARTITION partition_spec] CHANGE [COLUMN] col_old_name col_new_name column_type
      [COMMENT col_comment] [FIRST|AFTER column_name] [CASCADE|RESTRICT];
    ALTER TABLE table_name [PARTITION partition_spec]
      ADD|REPLACE COLUMNS (col_name data_type [COMMENT col_comment], ...)
      [CASCADE|RESTRICT]

#### 删除与清空表
    TRUNCATE TABLE table_name [PARTITION partition_spec];

    partition_spec:
      : (partition_column = partition_col_value, partition_column = partition_col_value, ...)

    DROP TABLE [IF EXISTS] table_name [PURGE];

### 注意事项
* hive.support.quoted.identifiers=column (default) 设置为none,时hive列只支持字母数字与下划线,设置为column可以支持一切``的字符
* hive.default.fileformat 默认存储的文件格式
* hive.exec.drop.ignorenonexistent 删除表不存在时不报错
* TBLPROPERTIES ("auto.purge"="true") or ("auto.purge"="false") true 表示删除后不能恢复
* 外部表与内部表最大区别是在删除表时外部表存放的数据不被删除
* create table tablename like/as  (like 不复制数据，as要复制数据)，且as目标表不能是外部表，分区表，bucketing table
* hive.enforce.bucketing = true 创建bucket table 后在插入数据时需要设定为true

### 实例
    create external table ext_tb (key string, value string)
    row format delimited fields terminated by ',' lines terminated by '\n'
    stored as textfile
    location '/tmp/ext_tb';
    上传文件到/tmp/ext_tb上面，就可以查询相关数据

    create table testtb2(key string, value string)
    stored as SEQUENCEFILE
    TBLPROPERTIES ("auto.purge"="true","immutable"="true") ;

    insert overwrite table testtb2
    insert into testtb2 (报错)

    // bucket table example
    CREATE TABLE kv_bucketed(key BIGINT, value string)
    COMMENT 'A bucketed copy of kv'
    CLUSTERED BY(key) INTO 10 BUCKETS;

    set hive.enforce.bucketing = true;
    FROM testtb
    INSERT OVERWRITE TABLE kv_bucketed select *;

    ALTER TABLE table_name ADD PARTITION (partCol = 'value1') location 'loc1';
    ALTER TABLE table_name ADD PARTITION (partCol = 'value2') location 'loc2';
    ...
    ALTER TABLE table_name ADD PARTITION (partCol = 'valueN') location 'locN';


## 函数的相关操作
    CREATE TEMPORARY FUNCTION function_name AS class_name;
    DROP TEMPORARY FUNCTION [IF EXISTS] function_name;

    CREATE FUNCTION [db_name.]function_name AS class_name
      [USING JAR|FILE|ARCHIVE 'file_uri' [, JAR|FILE|ARCHIVE 'file_uri'] ];   （As of Hive 0.13.0 (HIVE-6047).）
    DROP FUNCTION [IF EXISTS] function_name;
## show 相关操作

    Show Databases
    Show Tables/Partitions/Indexes
        Show Tables
        Show Partitions
        Show Table/Partition Extended
        Show Table Properties
        Show Create Table
        Show Indexes
    Show Columns
    Show Functions
    Show Granted Roles and Privileges
    Show Locks
    Show Conf
    Show Transactions
    Show Compactions

### hive dml 操作
#### 基本语法
    LOAD DATA [LOCAL] INPATH 'filepath' [OVERWRITE] INTO TABLE tablename [PARTITION (partcol1=val1, partcol2=val2 ...)]

    Standard syntax:
    INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...) [IF NOT EXISTS]] select_statement1 FROM from_statement;
    INSERT INTO TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1 FROM from_statement;

    Hive extension (multiple inserts):
    FROM from_statement
    INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...) [IF NOT EXISTS]] select_statement1
    [INSERT OVERWRITE TABLE tablename2 [PARTITION ... [IF NOT EXISTS]] select_statement2]
    [INSERT INTO TABLE tablename2 [PARTITION ...] select_statement2] ...;
    FROM from_statement
    INSERT INTO TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1
    [INSERT INTO TABLE tablename2 [PARTITION ...] select_statement2]
    [INSERT OVERWRITE TABLE tablename2 [PARTITION ... [IF NOT EXISTS]] select_statement2] ...;

    Hive extension (dynamic partition inserts):
    INSERT OVERWRITE TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) select_statement FROM from_statement;
    INSERT INTO TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) select_statement FROM from_statement;

    INSERT OVERWRITE [LOCAL] DIRECTORY directory1
      [ROW FORMAT row_format] [STORED AS file_format] (Note: Only available starting with Hive 0.11.0)
      SELECT ... FROM ...

    Hive extension (multiple inserts):
    FROM from_statement
    INSERT OVERWRITE [LOCAL] DIRECTORY directory1 select_statement1
    [INSERT OVERWRITE [LOCAL] DIRECTORY directory2 select_statement2] ...


    row_format
      : DELIMITED [FIELDS TERMINATED BY char [ESCAPED BY char]] [COLLECTION ITEMS TERMINATED BY char]
            [MAP KEYS TERMINATED BY char] [LINES TERMINATED BY char]
            [NULL DEFINED AS char] (Note: Only available starting with Hive 0.13)

    Standard Syntax:
    INSERT INTO TABLE tablename [PARTITION (partcol1[=val1], partcol2[=val2] ...)] VALUES values_row [, values_row ...]

    Where values_row is:
    ( value [, value ...] )
    where a value is either null or any valid SQL literal

    Standard Syntax:
    UPDATE tablename SET column = value [, column = value ...] [WHERE expression]
#### 注意事项
* load 目前只是copy与move
* filepath 可以是目录（但是目录中不能包含下级目录)或单个文件
* Hive 目前在load的时候会最一些基本检查，比如检查sequence文件不能load到textfile中
* 如果没有制定overwrite，在hive 1.1.0 版本同样的文件名不会被覆盖
*  TBLPROPERTIES ("immutable"="true") 表不能insert into
* mult table insert 减少数据文件扫描
* Update is available starting in hive 0.14.0
* delete is available starting in hive 0.14.0
* In version 0.14 it is recommended that you set hive.optimize.sort.dynamic.partition=false when doing deletes, as this produces more efficient execution plans.

#### Dynamic partition
    hive.exec.dynamic.partition   false    Needs to be set to true to enable dynamic partition inserts
    hive.exec.dynamic.partition.mode strict    In strict mode, the user must specify at least one static partition
    in case the user accidentally overwrites all partitions, in nonstrict mode all partitions are allowed to be dynamic
    hive.exec.max.dynamic.partitions.pernode 100 Maximum number of dynamic partitions allowed to be created in each mapper/reducer node
    hive.exec.max.dynamic.partitions 1000   Maximum number of dynamic partitions allowed to be created in total
    hive.exec.max.created.files  100000  Maximum number of HDFS files created by all mappers/reducers in a MapReduce job
    hive.error.on.empty.partition false  Whether to throw an exception if dynamic partition insert generates empty results
#### Import/ Export
    EXPORT TABLE tablename [PARTITION (part_column="value"[, ...])] TO 'export_target_path'
    IMPORT [[EXTERNAL] TABLE new_or_original_tablename [PARTITION (part_column="value"[, ...])]]
      FROM 'source_path'
      [LOCATION 'import_target_path']

    可用于大规模数据迁移
## dml 特殊语句
* top K set mapred.reduce.tasks = 1 SELECT * FROM sales SORT BY amount DESC LIMIT 5
* 同一个group by 中distinct 只能用于同一列
* Multi-Group-By
* Map-side Aggregation set hive.map.aggr=true; better efficiency ,but more memory
* sort by order by distribute by cluster by
* Only equality joins, outer joins, and left semi joins are supported in Hive. Hive does not support join conditions that are not equality conditions
* Hive converts joins over multiple tables into a single map/reduce job if for every table the same column is used in the join clauses
* join largest tables appear last in the sequence
* SELECT /*+ STREAMTABLE(a) */ a.val, b.val, c.val FROM a JOIN b ON (a.key = b.key1) JOIN c ON (c.key = b.key1) ［If the STREAMTABLE hint is omitted, Hive streams the rightmost table in the join.］
* SELECT /*+ MAPJOIN(b) */ a.key, a.value   FROM a JOIN b ON a.key = b.key set hive.optimize.bucketmapjoin = true
* If all the inputs are bucketed or sorted, and the join should be converted to a bucketized map-side join or bucketized sort-merge join.
* 关于join更多信息请参考 [HiveJoins] [2]

### mapjoin  bucket
    set hive.input.format=org.apache.hadoop.hive.ql.io.BucketizedHiveInputFormat;
    set hive.optimize.bucketmapjoin = true;
    set hive.optimize.bucketmapjoin.sortedmerge = true;

## 参考
获取详细信息，请参考 [HiveDDL] [1]


[1]: https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL "Hive DDL"
[2]: https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Joins "Hive JOINS"







