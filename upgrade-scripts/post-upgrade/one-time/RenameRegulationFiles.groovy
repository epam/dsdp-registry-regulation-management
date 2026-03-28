void call() {
    String gerritSecretName = "gerrit-ciuser-password"
    String registryRepoName = "registry-regulations"
    String gerritUser = sh(script: "oc get secret $gerritSecretName -o jsonpath={.data.user} " +
            "-n $NAMESPACE | base64 --decode", returnStdout: true)
    String gerritPass = sh(script: "oc get secret $gerritSecretName -o jsonpath={.data.password} " +
            "-n $NAMESPACE | base64 --decode", returnStdout: true)
    String gerritPath = sh(script: "oc get route gerrit -o jsonpath={.spec.path} -n $NAMESPACE", returnStdout: true).replaceAll("/\\z", "")
    String gerritHost = "gerrit.${NAMESPACE}.svc.cluster.local:8080$gerritPath"
    String repoUrl = "http://$gerritUser:$gerritPass@$gerritHost/$registryRepoName"
    String isRepoExists = sh(script: "set +x; git ls-remote $repoUrl | grep master > /dev/null", returnStatus: true)
    String newDirName = "ext-integrations"
    String oldDirName = "bp-trembita"

    if (isRepoExists == '0') {
        sh(script: "set +x; rm -rf $registryRepoName; git clone $repoUrl")
        if (!fileExists("$registryRepoName/$newDirName")) {
            String JENKINS_ADMIN_USERNAME = sh(script: "oc get secret jenkins-admin-token -o jsonpath={.data.username} " +
                    "-n ${NAMESPACE} | base64 --decode", returnStdout: true)
            String JENKINS_ADMIN_PASSWORD = sh(script: "oc get secret jenkins-admin-token -o jsonpath={.data.password} " +
                    "-n ${NAMESPACE} | base64 --decode", returnStdout: true)
            String JENKINS_PATH = sh(script: "oc get route jenkins -o jsonpath={.spec.path} -n $NAMESPACE", returnStdout: true).replaceAll("/\\z", "")
            String JENKINS_URL_WITH_CREDS = "http://$JENKINS_ADMIN_USERNAME:$JENKINS_ADMIN_PASSWORD@jenkins.${NAMESPACE}.svc:8080$JENKINS_PATH"
            sh(script: "mkdir $registryRepoName/$newDirName; " +
                    "mv $registryRepoName/$oldDirName/external-system.yml $registryRepoName/$newDirName/inbound.yml;" +
                    "mv $registryRepoName/$oldDirName/configuration.yml $registryRepoName/$newDirName/outbound.yml")
            LinkedHashMap oldStructure = readYaml file: "$registryRepoName/$newDirName/inbound.yml"
            if (oldStructure.containsKey("trembita")) {
                LinkedHashMap newStructure = ["inbound": oldStructure["trembita"]]
                writeYaml file: "$registryRepoName/$newDirName/inbound.yml", data: newStructure, overwrite: true
            }
            sh(script: "set +x; cd $registryRepoName " +
                    "&& git config user.name \"$gerritUser\" " +
                    "&& git config user.email \"jenkins@example.com\" " +
                    "&& git config user.password \"$gerritPass\" " +
                    "&& git add . && git commit -m 'Update registry-regulations files' " +
                    "&& git push origin master " +
                    "&& cd .. " +
                    "&& rm -rf $registryRepoName")
        String stages = "\\[\\{\\\"stages\\\":\\[\\{\\\"name\\\":\\\"checkout\\\"\\},\\{\\\"name\\\":\\\"init-registry\\\"\\},\\{\\\"name\\\":\\\"registry-regulations-validation\\\"\\}," +
                "\\{\\\"name\\\":\\\"shutdown-services\\\"\\},\\{\\\"name\\\":\\\"create-backup\\\"\\},\\{\\\"name\\\":\\\"create-redash-roles\\\"\\}\\]\\}," +
                "\\{\\\"parallelStages\\\":\\[\\{\\\"name\\\":\\\"deploy-data-model\\\"\\},\\[\\{\\\"name\\\":\\\"restore-redash-admin-state\\\"\\}," +
                "\\{\\\"name\\\":\\\"update-registry-logos\\\"\\},\\{\\\"name\\\":\\\"deploy-bp-webservice-gateway\\\"\\},\\{\\\"name\\\":\\\"upload-global-vars-changes\\\"\\}," +
                "\\{\\\"name\\\":\\\"configure-business-process-ext-integrations\\\"\\},\\{\\\"name\\\":\\\"update-registry-settings\\\"\\}," +
                "\\{\\\"name\\\":\\\"update-theme-login-page\\\"\\},\\{\\\"name\\\":\\\"create-keycloak-roles\\\"\\},\\{\\\"name\\\":\\\"update-bp-grouping\\\"\\}," +
                "\\{\\\"name\\\":\\\"upload-business-process-changes\\\"\\},\\{\\\"name\\\":\\\"create-permissions-business-process\\\"\\}," +
                "\\{\\\"name\\\":\\\"upload-form-changes\\\"\\},\\{\\\"name\\\":\\\"create-reports\\\"\\},\\{\\\"name\\\":\\\"import-excerpts\\\"\\}," +
                "\\{\\\"name\\\":\\\"import-mock-integrations\\\"\\},\\{\\\"name\\\":\\\"publish-notification-templates\\\"\\}\\]\\]\\}," +
                "\\{\\\"stages\\\":\\[\\{\\\"name\\\":\\\"bpms-rollout\\\"\\},\\{\\\"name\\\":\\\"publish-geoserver-configuration\\\"\\},\\{\\\"name\\\":\\\"run-autotests\\\"\\}\\]\\}\\]"
        sh(script: "set +x; curl -XPOST \"${JENKINS_URL_WITH_CREDS}/job/${registryRepoName}/job/MASTER-Build-${registryRepoName}/buildWithParameters?" +
                "STAGES=$stages\"")
        }
    }
}

return this;
