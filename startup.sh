#! /bin/bash
mvn clean package
java -Xmx2048m -cp third-server/target/third-server.jar ServerMain &>third-server.log &
java -Xmx2048m -cp scala-demo/target/scala-demo.jar DemoMain &>scala-demo.log &
java -Xmx2048m -jar jetty-runner-9.1.1.v20140108.jar --port 9888 java-demo/target/java-demo.war &>java-demo.log &
