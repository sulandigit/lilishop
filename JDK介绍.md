# JDK 介绍

## 什么是 JDK?

JDK (Java Development Kit) 是 Java 开发工具包,是用于开发 Java 应用程序的核心软件包。它包含了编译、运行和调试 Java 程序所需的所有工具和库。

## JDK 的组成部分

### 1. JRE (Java Runtime Environment)
- Java 运行时环境
- 包含 JVM (Java Virtual Machine) 和核心类库
- 用于运行 Java 应用程序

### 2. 开发工具
- **javac**: Java 编译器,将 .java 源文件编译为 .class 字节码文件
- **java**: Java 应用程序启动器
- **javadoc**: 文档生成器
- **jar**: JAR 文件打包工具
- **jdb**: Java 调试器
- **jconsole**: Java 监控和管理控制台
- **jvisualvm**: 可视化性能分析工具

### 3. 类库
- Java SE API (标准版应用程序接口)
- 核心类库和工具类

## JDK 版本

### 主要版本历史
- **JDK 1.0** (1996): 首个版本
- **JDK 1.5** (2004): 引入泛型、注解、枚举
- **JDK 8** (2014): Lambda 表达式、Stream API、新日期时间 API
- **JDK 11** (2018): LTS 长期支持版本
- **JDK 17** (2021): LTS 长期支持版本
- **JDK 21** (2023): LTS 长期支持版本

### LTS 版本
长期支持版本 (Long-Term Support) 提供更长时间的更新和维护,适合生产环境使用。

## JDK vs JRE vs JVM

| 组件 | 说明 | 用途 |
|------|------|------|
| **JVM** | Java 虚拟机 | 执行 Java 字节码 |
| **JRE** | JVM + 类库 | 运行 Java 程序 |
| **JDK** | JRE + 开发工具 | 开发和运行 Java 程序 |

## 安装 JDK

### Windows
1. 从 Oracle 官网或 OpenJDK 下载安装包
2. 运行安装程序
3. 配置环境变量 `JAVA_HOME` 和 `PATH`

### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

### macOS
```bash
# 使用 Homebrew
brew install openjdk@17
```

## 验证安装

```bash
# 查看 Java 版本
java -version

# 查看编译器版本
javac -version
```

## 环境变量配置

### JAVA_HOME
指向 JDK 安装目录

### PATH
添加 `$JAVA_HOME/bin` 以便在命令行使用 Java 工具

### 示例 (Linux/macOS)
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### 示例 (Windows)
```
JAVA_HOME=C:\Program Files\Java\jdk-17
PATH=%JAVA_HOME%\bin;%PATH%
```

## JDK 发行版

### 主要发行版
- **Oracle JDK**: Oracle 官方版本
- **OpenJDK**: 开源版本
- **Amazon Corretto**: AWS 提供的免费发行版
- **Azul Zulu**: Azul Systems 提供的发行版
- **AdoptOpenJDK/Adoptium**: 社区维护的发行版

## 常用命令

```bash
# 编译 Java 文件
javac HelloWorld.java

# 运行 Java 程序
java HelloWorld

# 创建 JAR 包
jar cvf myapp.jar *.class

# 查看 JAR 包内容
jar tvf myapp.jar

# 生成文档
javadoc -d docs *.java
```

## 选择 JDK 版本的建议

1. **生产环境**: 选择 LTS 版本 (如 JDK 17 或 JDK 21)
2. **学习开发**: 可以使用最新稳定版本
3. **遗留系统**: 根据项目依赖选择对应版本
4. **企业应用**: 优先考虑长期支持和稳定性

## 资源链接

- [Oracle JDK 官网](https://www.oracle.com/java/technologies/downloads/)
- [OpenJDK 官网](https://openjdk.org/)
- [Java 官方文档](https://docs.oracle.com/en/java/)
- [Adoptium (AdoptOpenJDK)](https://adoptium.net/)
