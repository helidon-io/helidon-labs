# 1. Setting Up the Environment

Before starting the hands-on lab, ensure you have the required tools installed.

In This Section, we will:

- Install JDK 24 and set up the required environment variables.
- Install Apache Maven for building and running the project.
- Set up a code editor (optional) to work efficiently with the project.
- Verify that all installations are working correctly.

---

## 1. Install JDK 24

Download and install **JDK 24** for your platform:

1. Open the following URL in your browser:  
   [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/)
2. Download the **correct JDK 24 version** for your operating system.
3. Move the downloaded file to your **home directory**.

Extract the JDK:

```sh
cd ~
tar -xvf jdk-24_linux-x64_bin.tar.gz
```

Set up the **JAVA_HOME** environment variable:

```sh
echo 'export JAVA_HOME=~/jdk-24' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

Verify the installation:

```sh
java -version
```

Expected output (example):
```
java version "24" 2024-XX-XX LTS
```

## 2. Install Apache Maven

Download and install **Maven 3.9.9**:

```sh
wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar -xvf apache-maven-3.9.9-bin.tar.gz
```

Set up the **MAVEN_HOME** environment variable:

```sh
echo 'export MAVEN_HOME=~/apache-maven-3.9.9' >> ~/.bashrc
echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

Verify the installation:

```sh
mvn -version
```

Expected output (example):
```
Apache Maven 3.9.9
Maven home: /home/user/apache-maven-3.9.9
Java version: 24, vendor: Oracle Corporation
```

## 3. Install a Code Editor (Optional)

You can use any code editor of your choice. Recommended options:

- [Visual Studio Code](https://code.visualstudio.com/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Eclipse](https://www.eclipse.org/downloads/)

---

### Next Step → [Setting Up the Project](02_setting_up_the_project.md)