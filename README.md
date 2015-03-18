学习研究Hive相关技术
======================


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
### hive 常用命令
#### system command
! <command>
dfs <dfs command>
<query string>
source FILE <filepath>

set [-v]
reset
exit/quit
#### Hive Resources
add [FILE|JAR|ARCHIVE] <value> [<value>]*
list [FILE|JAR|ARCHIVE] [<value> [<value>]*]
delete [FILE|JAR|ARCHIVE] <value> [<value>]*
#### logging
hive --hiveconf hive.root.logger=INFO,console
#### Hive Command Line Options

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
Examples
$HIVE_HOME/bin/hive -e 'select a.col from tab1 a' --hiveconf hive.exec.scratchdir=/home/my/hive_scratch  --hiveconf mapred.reduce.tasks=32

### HiveServer2 Clients（beeline)
!connect jdbc:hive2://host29:10000 hadoop hadoop org.apache.hive.jdbc.HiveDriver
beeline -u jdbc:hive2://host29:10000 hadoop hadoop org.apache.hive.jdbc.HiveDriver
