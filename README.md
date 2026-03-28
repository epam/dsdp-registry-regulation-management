# registry-regulation-management

This service provides API for batch-import process: import/export files with user data, retrieving
files info

## Related components:

* `ddm-ceph-client` - service, which provides methods for working with storage

## Local development:

### Prerequisites:

* Ceph/S3-like storage is configured and running
* Vault service is configured and running

### Configuration:

Check `src/main/resources/application-local.yaml` and replace if needed:

* *-ceph properties with your ceph storage values
* vault properties with your Vault values
  Check `src/main/resources/bootstrap.yaml` and replace if needed:
* vault properties with your Vault values for 'local' profile

### Steps:

1. (Optional) Package application into jar file with `mvn clean package`
2. Add `--spring.profiles.active=local` to application run arguments
3. Run application with your favourite IDE or via `java -jar ...` with jar file, created above

### Installation on Minikube

#### Prerequisites

1. Installed [Java OpenJDK 17](https://openjdk.org/install/), [Maven](https://maven.apache.org/)
   and [Minikube](https://minikube.sigs.k8s.io/docs/).
2. Maven is configured to use Nexus repository with all needed dependencies.

#### Configuring

* Configuration can be changed in registry-regulation-management config-map that is
  described [here](minikube-local/deploy-templates/templates/registry-regulation-management.yml) or
  in runtime in minikube dashboard with pod restarting.
* Any jvm attributes can be added to registry-regulation-management deployment in JAVA_OPTS env
  variable [here](minikube-local/deploy-templates/templates/registry-regulation-management.yml) or
  in runtime in minikube dashboard.

#### Registry-regulation-management installing

1. Build the service
    ```shell
    mvn package -P local
    ```
2. Build Docker image:
   ```shell
   minikube image build -t registry-regulation-management .
   ```
   You can check if image is built and exists in Minikube by:
   ```shell
   minikube image ls --format=table
   ```
3. Install registry-regulation-management using Helm:
   ```shell
   helm install registry-regulation-management-local minikube-local/deploy-templates
   ```
   Or if registry-regulation-management-local already installed:
   ```shell
   helm upgrade registry-regulation-management-local minikube-local/deploy-templates 
   ```
   In order to uninstall registry-regulation-management-local use:
   ```shell
   helm uninstall registry-regulation-management-local
   ```
   If registry-regulation-management-local is installed and registry-regulation-management image is
   rebuilt just delete registry-regulation-management pod.
4. Deployment health and service logs can be found in Minikube Dashboard:
   ```shell
   minikube dashboard
   ```
   Managing cluster can also be performed here.
5. Go to gerrit and add Verified label and label access in ```All Projects``` and then create
   repository named ```registry-regulations```:
   ```shell
   minikube service gerrit-service --url
   ```
6. After service deploying run next:
   ```shell
   minikube service registry-regulation-management-service --url
   ```
   In the terminal you'll see the url in format _http://${NODE_IP}:{NODE_PORT}_. Use this url to
   access the application from browser e.g. _http://{NODE_IP}:{NODE_PORT}/openapi_.

### RestAPI documentation generation

To generate document describing RestAPI definition separate maven profile should be used called
_generate-rest-api-docs_. Call `mvn -Pgenerate-rest-api-docs clean install` to generate rest api
documentation from scratch. After this generated documentation should be commited and pushed into
git together with potential other changes about RestAPI.

### Test execution

* Tests could be run via maven command:
   * `mvn verify` OR using appropriate functions of your IDE. To
     avoid `The filename or extension is too long` error on Windows, please
     uncomment `<fork>false</fork>` in `spring-boot-maven-plugin` configuration

### License

registry-regulation-management is Open Source software released under the Apache 2.0 license.
