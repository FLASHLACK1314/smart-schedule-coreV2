# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

智能排课系统（Smart Schedule Core V2）是一个基于 Spring Boot 3.5.9 的智能排课核心服务，使用 PostgreSQL 数据库。系统采用 MyBatis Plus 进行数据访问，使用 JSONB 字段存储复杂数据关系。

## 构建和运行命令

### Maven 命令
```bash
# 清理并编译项目
mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassName

# 打包项目
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 运行应用
mvn spring-boot:run

# 或者运行打包后的 JAR
java -jar target/smart-schedule-coreV2-0.0.1-SNAPSHOT.jar
```

### 开发环境
- Java 版本：17
- Maven：自动管理依赖
- 数据库：PostgreSQL
- Redis：用于缓存（spring-boot-starter-data-redis）

## 核心架构

### 三层架构设计
项目采用 MyBatis Plus 标准三层架构：

1. **DO 层（Model）**：数据对象
   - 位置：`src/main/java/io/github/flashlack1314/smartschedulecorev2/model/`
   - 命名：`*DO.java`
   - 使用 `@TableName` 指定表名，`@TableId` 标识主键，`@TableField` 映射字段
   - 所有 DO 使用 `@Data` 和 `@Accessors(chain = true)` 支持链式调用

2. **Mapper 层**：数据访问接口
   - 位置：`src/main/java/io/github/flashlack1314/smartschedulecorev2/mapper/`
   - 命名：`*Mapper.java`
   - 继承 `BaseMapper<*DO>`
   - 使用 `@Mapper` 注解

3. **DAO 层**：数据访问服务
   - 位置：`src/main/java/io/github/flashlack1314/smartschedulecorev2/dao/`
   - 命名：`*DAO.java`
   - 继承 `ServiceImpl<*Mapper, *DO>` 并实现 `IService<*DO>`
   - 使用 `@Slf4j` 和 `@Repository` 注解

### 数据库设计理念

1. **UUID 主键**：所有表使用 32 位 varchar 类型的 UUID 作为主键
2. **JSONB 灵活存储**：对于复杂关系使用 JSONB 类型
   - `sc_course.qualified_teacher_uuids`：教师 UUID 数组
   - `sc_teacher.like_time`：教师时间偏好
   - `sc_teaching_class.class_uuids`：行政班 UUID 数组
   - `sc_schedule.weeks_json`：上课周次数组
3. **外键约束**：严格的关系完整性约束
4. **冗余设计**：`sc_schedule` 表冗余存储课程、教师、教室信息以减少 JOIN

### JSONB 字段处理

对于 JSONB 字段，使用 `com.x_lf.utility.mybatis.handler.type.JsonTypeHandler` 进行类型转换：

```java
@TableField(value = "like_time", typeHandler = com.x_lf.utility.mybatis.handler.type.JsonTypeHandler.class)
private String likeTime;
```

**注意**：在实际使用中，已发现某些 JSONB 字段不需要显式指定 typeHandler（如 `TeacherDO.like_time` 和 `TeachingClassDO.class_uuids`），这些已被用户移除。

### 核心表结构

#### 基础组织架构
- `sc_semester`（学期）→ `sc_department`（学院）→ `sc_major`（专业）→ `sc_class`（行政班级）

#### 人员管理
- `sc_student`（学生）- 关联到行政班级
- `sc_teacher`（教师）- 包含时间偏好和工作量限制
- `sc_academic_admin`（教务管理）- 关联到学院

#### 教学资源
- `sc_building`（教学楼）→ `sc_classroom`（教室）

#### 课程与教学组织
- `sc_course`（课程）- 包含授课教师资格列表
- `sc_teaching_class`（教学班）- 排课的业务主体，关联课程、教师、学期、行政班级

#### 排课核心
- `sc_schedule`（排课记录）- 存储最终课表，包含时间、地点冗余字段
- `sc_schedule_conflict`（冲突记录）- 记录排课冲突

### 核心业务概念

**行政班级 vs 教学班**：
- **行政班级**（`sc_class`）：学生的固定组织单位（如"计科2101班"）
- **教学班**（`sc_teaching_class`）：临时的上课组织单位（如"高等数学-张老师-计科2101+2102"）

### 数据库初始化

项目包含自动数据库初始化功能：

- 配置类：`DatabaseInitProperties`
- 实现：`DatabaseInitializationConfig`
- SQL 文件位置：`src/main/resources/sql/*.sql`
- 表依赖顺序在 `DatabaseInitProperties.initializeDefaultTables()` 中定义

配置前缀：`database.init`
- `enabled`：是否启用数据库初始化
- `dropAndCreate`：是否强制重新创建表（生产环境请勿开启）
- `dropAllOnMissing`：是否在发现缺失表时删除所有表重建
- `failFast`：是否初始化失败时终止应用启动

### 依赖说明

项目使用 `com.x-lf.utility:general-utils:1.0.9-beta.2.5` 库，该库已包含：
- MyBatis Plus 功能
- JSONB 类型处理器
- 其他通用工具类

因此不需要单独添加 MyBatis Plus 依赖。

## 表创建顺序

表必须按依赖顺序创建（在 `DatabaseInitProperties` 中定义）：

1. **基础表**（无外键）：department, building, semester, teacher, course, system_admin
2. **二级依赖**：major, classroom, academic_admin
3. **三级依赖**：class, student
4. **四级依赖**：teaching_class, schedule
5. **五级依赖**：schedule_conflict

## 开发注意事项

1. **所有 DO 类必须包含中文注释**
2. **JSONB 字段的 typeHandler 使用需谨慎**：某些字段已移除 typeHandler 声明
3. **主键统一使用 String 类型存储 32 位 UUID**
4. **链式调用**：所有 DO 类支持链式调用（`@Accessors(chain = true)`）
5. **日志使用**：DAO 层统一使用 `@Slf4j` 注解