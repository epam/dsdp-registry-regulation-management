# registry-regulation-management

![Version: 1.9.10](https://img.shields.io/badge/Version-1.9.10-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.9.10-SNAPSHOT](https://img.shields.io/badge/AppVersion-1.9.10--SNAPSHOT-informational?style=flat-square)

Helm chart for Java application/service deploying

**Homepage:** <https://www.epam.com>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| OSD-DDM |  |  |

## Source Code

* <https://github>

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` |  |
| automountServiceAccountToken | bool | `true` |  |
| clusterName | string | `""` |  |
| config.cache.names | string | `"dates,conflicts,latestRebase,catalog"` |  |
| config.cache.spec | string | `"expireAfterAccess=1d"` |  |
| config.redash.url | string | `"http://mock:5000"` |  |
| config.registryRegulationManagement.dataModel.tablesFilePath | string | `"data-model/createTables.xml"` |  |
| config.registryRegulationManagement.dataSource.connectionTimeoutInMillis | int | `10000` |  |
| config.registryRegulationManagement.dataSource.publicSchema | string | `"public"` |  |
| config.registryRegulationManagement.dataSource.registryDataBase | string | `"registry"` |  |
| config.registryRegulationManagement.dataSource.registryDevDataBasePrefix | string | `"registry_dev_"` |  |
| config.registryRegulationManagement.retry.dataModelContextCreatingDelay | int | `300000` |  |
| config.registryRegulationManagement.retry.headBranchCloningDelay | int | `300000` |  |
| config.registryRegulationManagement.scheduled.cleanObsoleteVersionContexts.cron | string | `"0 0 20 ? * *"` |  |
| config.registryRegulationManagement.scheduled.cleanObsoleteVersionContexts.timezone | string | `"UTC"` |  |
| config.registryRegulationManagement.scheduled.masterRepoRefresh.cron | string | `"0 */5 * ? * *"` |  |
| config.registryRegulationManagement.scheduled.masterRepoRefresh.timezone | string | `"UTC"` |  |
| config.registryRegulationManagement.subjectTableName | string | `"subject"` |  |
| config.server.max-http-request-header-size | string | `"32KB"` |  |
| config.socket.message-size | int | `30000` |  |
| container.extraEnvVar | string | `""` |  |
| container.extraVolumeMounts | string | `""` |  |
| container.extraVolumes | string | `""` |  |
| container.livenessProbe | string | `"httpGet:\n  path: /actuator/health/liveness\n  port: {{ .Values.container.port }}\n  httpHeaders:\n    - name: X-B3-Sampled\n      value: \"0\"\nfailureThreshold: 1\ninitialDelaySeconds: 180\nperiodSeconds: 20\nsuccessThreshold: 1\ntimeoutSeconds: 5\n"` |  |
| container.port | int | `8080` |  |
| container.readinessProbe | string | `"httpGet:\n  path: /actuator/health/readiness\n  port: {{ .Values.container.port }}\n  httpHeaders:\n    - name: X-B3-Sampled\n      value: \"0\"\nfailureThreshold: 30\ninitialDelaySeconds: 25\nperiodSeconds: 10\nsuccessThreshold: 1\ntimeoutSeconds: 1\n"` |  |
| container.resources.limits | object | `{}` |  |
| container.resources.requests | object | `{}` |  |
| container.securityContext.runAsUser | int | `1001` |  |
| container.startupProbe | string | `""` |  |
| dnsPolicy | string | `""` |  |
| dnsWildcard | string | `""` |  |
| extraTrafficExcludeOutboundPorts | string | `"5432,9000"` |  |
| global.cicdTool.name | string | `"tekton"` |  |
| global.deploymentMode | string | `nil` |  |
| global.deploymentStrategy | string | `"Recreate"` |  |
| global.imagePullSecrets | list | `[]` |  |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.pullSecrets[0] | string | `"regcred"` |  |
| image.repository | string | `"registry-regulation-management"` |  |
| image.tag | string | `"latest"` |  |
| ingress.enabled | bool | `true` |  |
| ingress.ingressClassName | string | `""` |  |
| initContainers | string | `""` |  |
| istio.sidecar.enabled | bool | `true` |  |
| istio.sidecar.requestsLimitsEnabled | bool | `true` |  |
| istio.sidecar.resources.limits | object | `{}` |  |
| istio.sidecar.resources.requests | object | `{}` |  |
| keycloak.certificatesEndpoint | string | `"/protocol/openid-connect/certs"` |  |
| keycloak.officerRealmName | string | `"officer-portal"` |  |
| keycloak.realms.admin | string | `"admin"` |  |
| keycloak.realms.officer | string | `"officer-portal"` |  |
| keycloak.url | string | `""` |  |
| lifecycleHooks | object | `{}` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` |  |
| otelExporter.endpoint | string | `"http://jaeger-collector.opentelemetry-operator:4318/v1/traces"` |  |
| persistence.storageClass | string | `""` |  |
| persistence.volume.accessModes[0] | string | `"ReadWriteOnce"` |  |
| persistence.volume.size | string | `"5Gi"` |  |
| podAnnotations | object | `{}` |  |
| podSecurityContext.fsGroup | int | `1001` |  |
| postgres.appSecretName | string | `"postgresql-app-secrets"` |  |
| postgres.database | string | `"registry"` |  |
| postgres.databaseOverride | string | `""` |  |
| postgres.host | string | `""` |  |
| postgres.integrationSecretName | string | `"postgresql-connection-details"` |  |
| postgres.port | string | `""` |  |
| postgres.schemaName | string | `"public"` |  |
| replicas | string | `""` |  |
| s3.bucketName | string | `"user-import"` |  |
| s3.bucketNameOverride | string | `""` |  |
| s3.certSecretKey | string | `""` |  |
| s3.certSecretName | string | `""` |  |
| s3.host | string | `""` |  |
| s3.transportProtocol | string | `"http"` |  |
| scheduled.repositoryRefreshCron | string | `"0 */5 * ? * *"` |  |
| scheduled.repositoryRefreshTimezone | string | `"UTC"` |  |
| schedulerName | string | `"default-scheduler"` |  |
| service.nodePort | string | `""` |  |
| service.port | int | `8080` |  |
| service.type | string | `"ClusterIP"` |  |
| serviceAccount.create | bool | `true` |  |
| serviceAccount.name | string | `"registry-regulation-management"` |  |
| serviceMonitor.enabled | bool | `false` |  |
| serviceMonitor.interval | string | `"15s"` |  |
| serviceMonitor.kiali.enabled | bool | `false` |  |
| serviceMonitor.kiali.interval | string | `"15s"` |  |
| serviceMonitor.kiali.port | int | `15090` |  |
| serviceMonitor.kiali.scrapePath | string | `"/stats/prometheus"` |  |
| serviceMonitor.scrapePath | string | `"/actuator/prometheus"` |  |
| terminationGracePeriodSeconds | int | `30` |  |
| tolerations | object | `{}` |  |
| userImport.accessTokenSecretName | string | `"user-import-access-token"` |  |
| userImport.jobName | string | `"publish-users-job"` |  |
| vault.enabled | bool | `false` |  |
| vault.encryptionRole | string | `"registry-regulation-management-encryption-only-role"` |  |
| vault.key | string | `"registry-regulation-management-encryption-key"` |  |
| vault.url | string | `"http://mock:8200"` |  |
| vcs.gerrit.basePath | string | `"/gerrit"` |  |
| vcs.gerrit.repository | string | `"registry-regulations"` |  |
| vcs.gerrit.url | string | `"http://gerrit:8080"` |  |
| vcs.gitlab.project | string | `""` |  |
| vcs.gitlab.url | string | `""` |  |
| vcs.headBranch | string | `"main"` |  |
| vcs.provider | string | `"gitlab"` |  |
| vcs.repositoryDirectory | string | `"/var/lib/repos-data"` |  |
| vcs.secretName | string | `"ci-gitlab"` |  |