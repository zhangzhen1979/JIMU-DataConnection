```shell
|-  thinkdifferent-data-all              # 外部系统引用
|-  thinkdifferent-data-datasource       # 数据源管理
|-  thinkdifferent-data-pom              # 本项目依赖管理
|-  thinkdifferent-data-process          # 数据处理
|-  thinkdifferent-data-schedule         # 定时调度、配置文件监控
```

## 未完成部分

1. 目标库支持文件、接口等格式


## thinkdifferent-datasource

管理维护数据源，操作数据库

### xml 配置模板

启动时扫描配置目录下`.xml`结尾的文件, 文件修改、删除时重新加载。间隔时间：10s.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<task cron="0 0/1 * * * ? " blnRunning="true">
    <from name="localhost">
        <properties>
            <driver-class-name>com.mysql.cj.jdbc.Driver</driver-class-name>
            <username>root</username>
            <password>123456</password>
            <url>jdbc:mysql://localhost:3306/wdb1?serverTimezone=UTC</url>
            <name>fromDataSource</name>
        </properties>
        <dicts/>
    </from>
    <to name="250" blnCheckRepeat="true">
        <properties>
            <driver-class-name>com.mysql.cj.jdbc.Driver</driver-class-name>
            <password>Weaver_2021</password>
            <url>jdbc:mysql://192.168.2.250:3306/ec?serverTimezone=UTC</url>
            <name>toDataSource</name>
            <username>ecology</username>
        </properties>
    </to>
    <tables>
        <table name="adminuser_system" toName="systemadminuser_1" whereCondition=" 1=1" id="aid" parentTable="parent" parentId="id">
            <fields>
                <field name="aid" type="int" targetName="pid" targetType="int"/>
                <field name="account" type="string" targetName="account" targetType="string"/>
                <field name="username" type="varchar" targetName="username" targetType="varchar"/>
                <field targetName="password" targetType="varchar" handleType="" handleExpress="111111"/>
                <field name="roleid" type="int" targetName="roleid" targetType="int" handleType="QL_EXPRESS"
                       handleExpress="roleid + 100"/>
                <field targetName="addtime" targetType="int" handleType="CONSTANT" handleExpress="NOW_SECOND_INT"/>
                <field targetName="logintime" targetType="int" handleType="CONSTANT" handleExpress="NOW_SECOND_INT"/>

                <field targetName="created" targetType="datetime" handleType="CONSTANT"
                       handleExpress="NORM_DATETIME_FORMAT"/>
                <field targetName="creater" targetType="int" handleExpress="1"/>
                <field targetName="modified" targetType="datetime" handleType="CONSTANT"
                       handleExpress="NORM_DATETIME_FORMAT"/>
                <field targetName="modifier" targetType="int" handleExpress="1"/>
                <field targetName="uuid" targetType="varchar" handleType="CONSTANT" handleExpress="SIMPLE_UUID"/>
            </fields>
        </table>
        <table name="adminlog" toName="adminlog" whereCondition=" ">
            <fields>
                <field name="id" type="int" targetName="id" targetType="integer"/>
                <field name="oper" type="int" targetName="oper" targetType="int"/>
                <field name="addtime" type="int" targetName="addtime" targetType="int"/>
                <field name="delname" type="varchar" targetName="delname" targetType="varchar"/>
                <field name="delid" type="int" targetName="delid" targetType="int"/>
                <field name="adminname" type="varchar" targetName="adminname" targetType="varchar"/>
            </fields>
        </table>
    </tables>
</task>
```

## thinkdifferent-data-schedule

定时任务相关

```yaml
# 优先级， 配置文件配置 > jar 运行目录
# xml配置文件目录, 不配置的话默认取当前运行目录，获取该目录下所有 xml 结尾的文件
xml:
  path: /app/xml/

# 同步日志记录目录,不配置的话默认取当前运行目录
logging:
  file:
    path: D:\temp\
```

## thinkdifferent-data-process
数据加工,支持的功能如下：

| 类型         | 处理类               | 说明                       |配置说明| 是否验证| 示例配置                                                                                                                                                                            |
|------------|-------------------|--------------------------|----|----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NONE       | StringHandler     | 不处理, 固定的内容               |handleType可不配置| 是 | `<field targetName="password" targetType="varchar" handleExpress="123456"/>`<br/>`<field targetName="password" targetType="varchar" handleType="NONE" handleExpress="123456"/>` |
| CONSTANT   | ConstantHandler   | 系统常量，具体见 ConstantEnum    | | 是 | `<field targetName="createDate" targetType="varchar" handleType="CONSTANT" handleExpress="SIMPLE_DATE"/>`                                                                       |
| JSON       | JsonDataHandler   | jsonPath匹配信息，匹配到多值抛出异常    | |  |
| XML        | XmlDataHandler    | XPath匹配信息，匹配到多值抛出异常      | |  |
| REGEXP     | RegExpDataHandler | 正则表达式获取数据                |  |  |
| QL_EXPRESS | QLExpressHandler  | QL表达式计算                  |handleExpress必须配置，所用字段取目标表表名 | 是 | `<field targetName="percentage" targetType="varchar" handleType="QL_EXPRESS" handleExpress="num / 100 + '%' "/>`                                                                |
| DICT       | DictHandler       | 字典项配置【表名.targetKeyField】 | handleExpress必须配置， 格式：【表名.targetKeyField】   |  | `<field targetName="companyName" targetType="varchar" handleType="DICT" handleExpress="dict1.companyId"/>`                                                                         |






