{{- define "registry-regulation-management.name" }}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "registry-regulation-management.chart" }}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "registry-regulation-management.labels" }}
helm.sh/chart: {{ include "registry-regulation-management.chart" . }}
{{ include "registry-regulation-management.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "registry-regulation-management.selectorLabels" }}
app.kubernetes.io/name: {{ include "registry-regulation-management.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "registry-regulation-management.serviceAccountName" }}
{{- if .Values.serviceAccount.create }}
{{- default (include "registry-regulation-management.name" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{- define "admin-tools.url" }}
{{- printf "%s%s-%s.%s" "https://" "admin-tools" .Release.Namespace .Values.dnsWildcard }}
{{- end }}

{{- define "keycloak.urlPrefix" }}
{{- printf "%s%s%s" .Values.keycloak.url "/realms/" .Release.Namespace }}
{{- end }}

{{- define "issuer.admin" }}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.admin }}
{{- end }}

{{- define "jwksUri.admin" }}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.admin .Values.keycloak.certificatesEndpoint }}
{{- end }}

{{- define "keycloak.officerRealm" }}
{{- printf "%s-%s" .Release.Namespace .Values.keycloak.officerRealmName }}
{{- end }}

{{- define "gerrit.url" }}
{{- printf "%s%s" .Values.vcs.gerrit.url .Values.vcs.gerrit.basePath }}
{{- end }}

{{- define "registry-regulation-management.istioResources" }}
{{- if .Values.istio.sidecar.resources.limits.cpu }}
sidecar.istio.io/proxyCPULimit: {{ .Values.istio.sidecar.resources.limits.cpu | quote }}
{{- end }}
{{- if .Values.istio.sidecar.resources.limits.memory }}
sidecar.istio.io/proxyMemoryLimit: {{ .Values.istio.sidecar.resources.limits.memory | quote }}
{{- end }}
{{- if .Values.istio.sidecar.resources.requests.cpu }}
sidecar.istio.io/proxyCPU: {{ .Values.istio.sidecar.resources.requests.cpu | quote }}
{{- end }}
{{- if .Values.istio.sidecar.resources.requests.memory }}
sidecar.istio.io/proxyMemory: {{ .Values.istio.sidecar.resources.requests.memory | quote }}
{{- end }}
{{- end }}

{{- define "s3.bucketName" }}
{{- if .Values.s3.bucketNameOverride }}
{{- .Values.s3.bucketNameOverride }}
{{- else }}
{{- printf "%s-%s-%s" .Values.clusterName .Release.Namespace .Values.s3.bucketName }}
{{- end }}
{{- end }}

{{- define  "database.name" -}}
{{- if .Values.postgres.databaseOverride -}}
{{- .Values.postgres.databaseOverride -}}
{{- else -}}
{{- $clusterName := .Values.clusterName | replace "-" "_" -}}
{{- $releaseNameSpace := .Release.Namespace | replace "-" "_" -}}
{{- printf "%s_%s_%s" $clusterName $releaseNameSpace .Values.postgres.database -}}
{{- end -}}
{{- end -}}

{{- define "common.storage.class" }}
{{- $storageClass := .Values.persistence.storageClass | default "" }}
{{- if $storageClass }}
{{- if eq $storageClass "-" }}
storageClassName: ""
{{- else }}
storageClassName: {{ $storageClass | quote }}
{{- end }}
{{- end }}
{{- end }}
