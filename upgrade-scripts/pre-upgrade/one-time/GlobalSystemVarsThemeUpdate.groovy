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
    String filePath = "$registryRepoName/global-vars/camunda-global-system-vars.yml"

    if (isRepoExists == '0') {
        sh(script: "set +x; rm -rf $registryRepoName; git clone $repoUrl")
        LinkedHashMap camundaGlobalSystemVars = readYaml file: filePath
        if (camundaGlobalSystemVars.containsKey("themeFile")) {
            println("[INFO] Update 'themeFile' property.")
            String themeFileValue = camundaGlobalSystemVars.remove("themeFile")
            String themeValue = themeFileValue.replace(".js", "")
            camundaGlobalSystemVars["theme"] = themeValue
            writeYaml file: filePath, data: camundaGlobalSystemVars, overwrite: true

            String JENKINS_ADMIN_USERNAME = sh(script: "oc get secret jenkins-admin-token -o jsonpath={.data.username} " +
                    "-n ${NAMESPACE} | base64 --decode", returnStdout: true)
            String JENKINS_ADMIN_PASSWORD = sh(script: "oc get secret jenkins-admin-token -o jsonpath={.data.password} " +
                    "-n ${NAMESPACE} | base64 --decode", returnStdout: true)
            String JENKINS_PATH = sh(script: "oc get route jenkins -o jsonpath={.spec.path} -n $NAMESPACE", returnStdout: true).replaceAll("/\\z", "")
            String JENKINS_URL_WITH_CREDS = "http://$JENKINS_ADMIN_USERNAME:$JENKINS_ADMIN_PASSWORD@jenkins.${NAMESPACE}.svc:8080$JENKINS_PATH"

            sh(script: "set +x; cd $registryRepoName " +
                    "&& git config user.name \"$gerritUser\" " +
                    "&& git config user.email \"jenkins@example.com\" " +
                    "&& git config user.password \"$gerritPass\" " +
                    "&& git add . && git commit -m 'Update themeFile property to theme in global-vars/camunda-global-system-vars.yml' " +
                    "&& git push origin master" +
                    "&& cd .. " +
                    "&& rm -rf $registryRepoName")

            sh(script: "curl -XPOST \"${JENKINS_URL_WITH_CREDS}/job/${registryRepoName}/job/MASTER-Build-${registryRepoName}/buildWithParameters\"")
        } else {
            println("[INFO] No update needed as 'themeFile' property does not exist.")
        }
    }
}

return this;