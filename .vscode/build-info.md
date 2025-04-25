cd /usr/dev/mi-test/test1
mvn clean install
cp target/test1_1.0.0.car /usr/dev/wso2mi-4.3.0/repository/deployment/server/carbonapps/

sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --stop
sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh

curl localhost:8290/newdbconnector/insert -d '{"name":"John","age":30}' -H "Content-Type: application/json"
curl localhost:8290/new

\\wsl.localhost\Ubuntu\usr\dev\mi-test\test1\src\main\wso2mi\resources\connectors

cd /mnt/c/work/connectors/db-connector-pr && \
  mvn clean install && \
    cp target/db-connector-1.0.0.zip  /usr/dev/mi-test/test1/src/main/wso2mi/resources/connectors && \
      cd /usr/dev/mi-test/test1 && \
        mvn clean install && \
         cp target/test1_1.0.0.car /usr/dev/wso2mi-4.3.0/repository/deployment/server/carbonapps/ && \
           sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --stop && \
             sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --debug 5005

cd /mnt/c/work/connectors/db-connector-pr && \
  mvn clean install && \
    cp target/db-connector-1.0.0.zip  /usr/dev/mi-test/test1/src/main/wso2mi/resources/connectors && \
      cd /usr/dev/mi-test/test1 && \
        mvn clean install && \
         cp target/test1_1.0.0.car /usr/dev/wso2mi-4.3.0/repository/deployment/server/carbonapps/ && \
           sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --stop && \
             sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh
    

cd /mnt/c/work/connectors/db-connector-pr && \
  mvn clean install && \
    cd /usr/dev/mi-test/test1 && \
     mvn clean install && \
      cp target/test1_1.0.0.car /usr/dev/wso2mi-4.3.0/repository/deployment/server/carbonapps/ && \
        sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --stop && \
          sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh

cd /usr/dev/mi-test/test1 && \
 mvn clean install && \
  cp target/test1_1.0.0.car /usr/dev/wso2mi-4.3.0/repository/deployment/server/carbonapps/ && \
  sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --stop && \
  sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh

cd /usr/dev/mi-test/test1 && \
 mvn clean install && \
  cp target/test1_1.0.0.car /usr/dev/wso2mi-4.3.0/repository/deployment/server/carbonapps/ && \
  sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --stop && \
  sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh --debug 5005

  /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/bin/micro-integrator.sh --debug 5005
  sudo /usr/dev/wso2mi-4.3.0/bin/micro-integrator.sh

  /Dev/mi-test/test1

cd /Dev/mi-test/test1 && \
  mvn clean install && \
  cp target/test1_1.0.0.car /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/repository/deployment/server/carbonapps/ && \
  sudo /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/bin/micro-integrator.sh --stop && \
  sudo /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/bin/micro-integrator.sh --debug 5005

  cd /Dev/mi-test/test1 && \
  mvn clean install && \
  cp target/test1_1.0.0.car /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/repository/deployment/server/carbonapps/ && \
  /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/bin/micro-integrator.sh --stop && \
  /home/saliya/.wso2-mi/micro-integrator/wso2mi-4.4.0/bin/micro-integrator.sh --debug 5005

  curl -X 'POST' \
  'http://localhost:8290/new/' \
  -H 'Content-Type: application/json' \
  -d '{"searchQuery":"rabbit"}'


set java home
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
./home/saliya/.wso2-mi/java/jdk-21.0.6+7/bin/javac

export JAVA_HOME=/home/saliya/.wso2-mi/java/jdk-21.0.6+7
export JAVA_HOME=/usr/lib/jvm/openjdk-21
source /etc/environment
echo $JAVA_HOME

# remove symlink
rm /Dev/mi-test/test1/src/main/wso2mi/resources/connectors/db-connector-1.0.0.zip
rm /Dev/mi-test/test1/src/main/wso2mi/resources/connectors/dbconnector-connector-1.0.0.zip

# create symlink
ln -s /mnt/c/work/connectors/db-connector-pr/target/db-connector-1.0.0.zip /Dev/mi-test/test1/src/main/wso2mi/resources/connectors/db-connector-1.0.0.zip


ln -s /mnt/c/work/connectors/db-connector-pr/target/db-connector-1.0.0.zip /home/saliya/wso2mi/Projects/test2/src/main/wso2mi/resources/connectors/db-connector-1.0.0.zip


/home/saliya/wso2mi/Projects/test2/deployment/libs
rm /home/saliya/wso2mi/Projects/test2/src/main/wso2mi/resources/connectors/db-connector-1.0.0.zip

/Dev/mi-test/test2/src/main/wso2mi/artifacts/local-entries/MSSQLCON1.xml
rm /Dev/mi-test/test2/src/main/wso2mi/resources/connectors/db-connector-1.0.0.zip
ln -s /mnt/c/work/connectors/db-connector-pr/target/db-connector-1.0.0.zip /Dev/mi-test/test2/src/main/wso2mi/resources/connectors/db-connector-1.0.0.zip