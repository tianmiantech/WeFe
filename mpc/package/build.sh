# bin/bash
cd ..
rm -rf libs/
mkdir libs
mvn clean package install -Dmaven.test.skip=true

cp mpc-pir/mpc-pir-server/target/mpc-pir-server-1.0.0-shaded.jar libs/mpc-pir-server-1.0.0.jar
cp mpc-pir/mpc-pir-sdk/target/mpc-pir-sdk-1.0.0-shaded.jar libs/mpc-pir-sdk-1.0.0.jar
cp mpc-psi/mpc-psi-sdk/target/mpc-psi-sdk-1.0.0-shaded.jar libs/mpc-psi-sdk-1.0.0.jar
cp mpc-sa/mpc-sa-server/target/mpc-sa-server-1.0.0-shaded.jar libs/mpc-sa-server-1.0.0.jar
cp mpc-sa/mpc-sa-sdk/target/mpc-sa-sdk-1.0.0-shaded.jar libs/mpc-sa-sdk-1.0.0.jar