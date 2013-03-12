Please visit: https://www.projectforge.org/pf-en/Developersarewelcome

Quickstart (since version 4.*)

1. Checkout:
https://github.com/micromata/projectforge-webapp.git

2. Set the JVM memory in MAVEN_OPTS or JAVA_OPTS:
-Xmx1024m -Xms512m -XX:PermSize=96m -XX:MaxPermSize=192m

3. Build ProjectForge:
mvn -DskipTests=true install

4. Run ProjectForge:
mvn exec:java -Dexec.mainClass="org.projectforge.web.MyStart" -Dexec.classpathScope=test

5. Your browser will be opened after start-up automatically:
http://localhost:8080/ProjectForge

6. Enjoy it.
