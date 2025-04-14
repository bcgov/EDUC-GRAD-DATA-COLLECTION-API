envValue=$1
APP_NAME=$2
OPENSHIFT_NAMESPACE=$3
COMMON_NAMESPACE=$4
APP_NAME_UPPER=${APP_NAME^^}
DB_JDBC_CONNECT_STRING=$5
DB_PWD=$6
DB_USER=$7
SPLUNK_TOKEN=$8
CHES_CLIENT_ID=$9
CHES_CLIENT_SECRET="${10}"
CHES_TOKEN_URL="${11}"
CHES_ENDPOINT_URL="${12}"
GRAD_NAMESPACE="${13}"
EDX_URL="${14}"
TZVALUE="America/Vancouver"
SOAM_KC_REALM_ID="master"
SOAM_KC=soam-$envValue.apps.silver.devops.gov.bc.ca


SOAM_KC_LOAD_USER_ADMIN=$(oc -n $COMMON_NAMESPACE-$envValue -o json get secret sso-admin-${envValue} | sed -n 's/.*"username": "\(.*\)"/\1/p' | base64 --decode)
SOAM_KC_LOAD_USER_PASS=$(oc -n $COMMON_NAMESPACE-$envValue -o json get secret sso-admin-${envValue} | sed -n 's/.*"password": "\(.*\)",/\1/p' | base64 --decode)
NATS_URL="nats://nats.${COMMON_NAMESPACE}-${envValue}.svc.cluster.local:4222"

echo Fetching SOAM token
TKN=$(curl -s \
  -d "client_id=admin-cli" \
  -d "username=$SOAM_KC_LOAD_USER_ADMIN" \
  -d "password=$SOAM_KC_LOAD_USER_PASS" \
  -d "grant_type=password" \
  "https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID/protocol/openid-connect/token" | jq -r '.access_token')

echo
echo Retrieving client ID for grad-data-collection-api-service
GRAD_APIServiceClientID=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" |
  jq '.[] | select(.clientId=="grad-data-collection-api-service")' | jq -r '.id')

echo
echo Retrieving client secret for grad-data-collection-api-service
GRAD_APIServiceClientSecret=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$GRAD_APIServiceClientID/client-secret" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" |
  jq -r '.value')

echo
echo Removing GRAD DATA COLLECTION API client if exists
curl -sX DELETE "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$GRAD_APIServiceClientID" \
  -H "Authorization: Bearer $TKN"

echo
echo Writing scope READ_INCOMING_FILESET
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read incoming fileset\",\"id\": \"READ_INCOMING_FILESET\",\"name\": \"READ_INCOMING_FILESET\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_REPORTING_PERIOD
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read reporting period\",\"id\": \"READ_REPORTING_PERIOD\",\"name\": \"READ_REPORTING_PERIOD\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_REPORTING_PERIOD
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write reporting period\",\"id\": \"WRITE_REPORTING_PERIOD\",\"name\": \"WRITE_REPORTING_PERIOD\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_GRAD_COLLECTION
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read Grad Data Collection\",\"id\": \"READ_GRAD_COLLECTION\",\"name\": \"READ_GRAD_COLLECTION\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope WRITE_GRAD_COLLECTION
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Write Grad Collection Data\",\"id\": \"WRITE_GRAD_COLLECTION\",\"name\": \"WRITE_GRAD_COLLECTION\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_FILESET_STUDENT_ERROR
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read Fileset Student Error\",\"id\": \"READ_FILESET_STUDENT_ERROR\",\"name\": \"READ_FILESET_STUDENT_ERROR\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"

echo
echo Writing scope READ_GRAD_COLLECTION_CODES
curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/client-scopes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  -d "{\"description\": \"Read Grad Data Collection Codes\",\"id\": \"READ_GRAD_COLLECTION_CODES\",\"name\": \"READ_GRAD_COLLECTION_CODES\",\"protocol\": \"openid-connect\",\"attributes\" : {\"include.in.token.scope\" : \"true\",\"display.on.consent.screen\" : \"false\"}}"


if [[ -n "$GRAD_APIServiceClientID" && -n "$GRAD_APIServiceClientSecret" && ("$envValue" = "dev" || "$envValue" = "test") ]]; then
  echo
  echo Creating client grad-data-collection-api-service with secret
  curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "{\"clientId\" : \"grad-data-collection-api-service\",\"secret\" : \"$GRAD_APIServiceClientSecret\",\"surrogateAuthRequired\" : false,\"enabled\" : true,\"clientAuthenticatorType\" : \"client-secret\",\"redirectUris\" : [ ],\"webOrigins\" : [ ],\"notBefore\" : 0,\"bearerOnly\" : false,\"consentRequired\" : false,\"standardFlowEnabled\" : false,\"implicitFlowEnabled\" : false,\"directAccessGrantsEnabled\" : false,\"serviceAccountsEnabled\" : true,\"publicClient\" : false,\"frontchannelLogout\" : false,\"protocol\" : \"openid-connect\",\"attributes\" : {\"saml.assertion.signature\" : \"false\",\"saml.multivalued.roles\" : \"false\",\"saml.force.post.binding\" : \"false\",\"saml.encrypt\" : \"false\",\"saml.server.signature\" : \"false\",\"saml.server.signature.keyinfo.ext\" : \"false\",\"exclude.session.state.from.auth.response\" : \"false\",\"saml_force_name_id_format\" : \"false\",\"saml.client.signature\" : \"false\",\"tls.client.certificate.bound.access.tokens\" : \"false\",\"saml.authnstatement\" : \"false\",\"display.on.consent.screen\" : \"false\",\"saml.onetimeuse.condition\" : \"false\"},\"authenticationFlowBindingOverrides\" : { },\"fullScopeAllowed\" : true,\"nodeReRegistrationTimeout\" : -1,\"protocolMappers\" : [ {\"name\" : \"Client ID\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientId\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientId\",\"jsonType.label\" : \"String\"}}, {\"name\" : \"Client Host\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientHost\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientHost\",\"jsonType.label\" : \"String\"}}, {\"name\" : \"Client IP Address\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientAddress\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientAddress\",\"jsonType.label\" : \"String\"}} ],\"defaultClientScopes\" : [ \"READ_INSTITUTE_CODES\", \"READ_SCHOOL\", \"READ_DISTRICT\", \"READ_SCHOLARSHIPS_CODES\", \"READ_EQUIVALENT_OR_CHALLENGE_CODE\", \"READ_GRAD_STUDENT_GRADE_CODES\", \"READ_GRAD_PROGRAM_RULES_DATA\", \"READ_GRAD_CAREER_PROGRAM_CODE_DATA\", \"READ_GRAD_SPECIAL_PROGRAM_CODE_DATA\", \"READ_GRAD_PROGRAM_CODE_DATA\", \"READ_GRAD_LETTER_GRADE_DATA\", \"READ_INDEPENDENT_AUTHORITY\", \"READ_EDX_USERS\",\"COREG_READ_COURSE\",\"web-origins\", \"role_list\", \"profile\", \"roles\", \"email\"],\"optionalClientScopes\" : [ \"address\", \"phone\", \"offline_access\" ],\"access\" : {\"view\" : true,\"configure\" : true,\"manage\" : true}}"
else
  echo
    echo Creating client grad-data-collection-api-service without secret
    curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TKN" \
      -d "{\"clientId\" : \"grad-data-collection-api-service\",\"surrogateAuthRequired\" : false,\"enabled\" : true,\"clientAuthenticatorType\" : \"client-secret\",\"redirectUris\" : [ ],\"webOrigins\" : [ ],\"notBefore\" : 0,\"bearerOnly\" : false,\"consentRequired\" : false,\"standardFlowEnabled\" : false,\"implicitFlowEnabled\" : false,\"directAccessGrantsEnabled\" : false,\"serviceAccountsEnabled\" : true,\"publicClient\" : false,\"frontchannelLogout\" : false,\"protocol\" : \"openid-connect\",\"attributes\" : {\"saml.assertion.signature\" : \"false\",\"saml.multivalued.roles\" : \"false\",\"saml.force.post.binding\" : \"false\",\"saml.encrypt\" : \"false\",\"saml.server.signature\" : \"false\",\"saml.server.signature.keyinfo.ext\" : \"false\",\"exclude.session.state.from.auth.response\" : \"false\",\"saml_force_name_id_format\" : \"false\",\"saml.client.signature\" : \"false\",\"tls.client.certificate.bound.access.tokens\" : \"false\",\"saml.authnstatement\" : \"false\",\"display.on.consent.screen\" : \"false\",\"saml.onetimeuse.condition\" : \"false\"},\"authenticationFlowBindingOverrides\" : { },\"fullScopeAllowed\" : true,\"nodeReRegistrationTimeout\" : -1,\"protocolMappers\" : [ {\"name\" : \"Client ID\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientId\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientId\",\"jsonType.label\" : \"String\"}}, {\"name\" : \"Client Host\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientHost\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientHost\",\"jsonType.label\" : \"String\"}}, {\"name\" : \"Client IP Address\",\"protocol\" : \"openid-connect\",\"protocolMapper\" : \"oidc-usersessionmodel-note-mapper\",\"consentRequired\" : false,\"config\" : {\"user.session.note\" : \"clientAddress\",\"id.token.claim\" : \"true\",\"access.token.claim\" : \"true\",\"claim.name\" : \"clientAddress\",\"jsonType.label\" : \"String\"}} ],\"defaultClientScopes\" : [ \"READ_INSTITUTE_CODES\", \"READ_SCHOOL\", \"READ_DISTRICT\", \"READ_SCHOLARSHIPS_CODES\", \"READ_EQUIVALENT_OR_CHALLENGE_CODE\", \"READ_GRAD_STUDENT_GRADE_CODES\", \"READ_GRAD_PROGRAM_RULES_DATA\", \"READ_GRAD_CAREER_PROGRAM_CODE_DATA\", \"READ_GRAD_SPECIAL_PROGRAM_CODE_DATA\", \"READ_GRAD_PROGRAM_CODE_DATA\", \"READ_GRAD_LETTER_GRADE_DATA\", \"READ_INDEPENDENT_AUTHORITY\", \"READ_EDX_USERS\", \"COREG_READ_COURSE\", \"web-origins\", \"role_list\", \"profile\", \"roles\", \"email\"],\"optionalClientScopes\" : [ \"address\", \"phone\", \"offline_access\" ],\"access\" : {\"view\" : true,\"configure\" : true,\"manage\" : true}}"
fi

echo
echo Retrieving client ID for grad-data-collection-api-service
GRAD_APIServiceClientID=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" |
  jq '.[] | select(.clientId=="grad-data-collection-api-service")' | jq -r '.id')

echo
echo Retrieving client secret for grad-data-collection-api-service
GRAD_APIServiceClientSecret=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$GRAD_APIServiceClientID/client-secret" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" |
  jq -r '.value')
###########################################################
#Setup for config-map
###########################################################
SPLUNK_URL="gww.splunk.educ.gov.bc.ca"
FLB_CONFIG="[SERVICE]
   Flush        1
   Daemon       Off
   Log_Level    debug
   HTTP_Server   On
   HTTP_Listen   0.0.0.0
   Parsers_File parsers.conf
[INPUT]
   Name   tail
   Path   /mnt/log/*
   Exclude_Path *.gz,*.zip
   Parser docker
   Mem_Buf_Limit 20MB
[FILTER]
   Name record_modifier
   Match *
   Record hostname \${HOSTNAME}
[OUTPUT]
   Name   stdout
   Match  *
[OUTPUT]
   Name  splunk
   Match *
   Host  $SPLUNK_URL
   Port  443
   TLS         On
   TLS.Verify  Off
   Message_Key $APP_NAME
   Splunk_Token $SPLUNK_TOKEN
"
PARSER_CONFIG="
[PARSER]
    Name        docker
    Format      json
"


THREADS_MIN_SUBSCRIBER=8
THREADS_MAX_SUBSCRIBER=12
SAGAS_MAX_PENDING=150
SAGAS_MAX_PARALLEL=150
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON="0 0/2 * * * *"
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR="PT4M"
SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR="PT4M"
SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON="0/5 * * * * *"
SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON_LOCK_AT_LEAST_FOR="4700ms"
SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON_LOCK_AT_MOST_FOR="4900ms"
INCOMING_FILESET_STALE_IN_HOURS="3"
SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON="0 */10 * * * *"
SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON_LOCK_AT_LEAST_FOR="55s"
SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON_LOCK_AT_MOST_FOR="57s"

SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON="0 0 0 * OCT ?"
SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON_LOCK_AT_LEAST_FOR="1700ms"
SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON_LOCK_AT_MOST_FOR="1900ms"

MAXIMUM_DB_POOL_SIZE=25
MINIMUM_IDLE_DB_POOL_SIZE=15
NUMBER_OF_STUDENTS_TO_PROCESS_SAGA=300

echo
echo Creating config map "$APP_NAME"-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-config-map --from-literal=TZ=$TZVALUE --from-literal=EDX_URL="$EDX_URL" --from-literal=GRAD_COURSE_API_URL="http://educ-grad-course-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/course" --from-literal=GRAD_PROGRAM_API_URL="http://educ-grad-program-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/program" --from-literal=GRAD_STUDENT_API_URL="http://educ-grad-student-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/student" --from-literal=GRAD_STUDENT_GRADUATION_API_URL="http://educ-grad-student-graduation-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/studentgraduation/lgSc" --from-literal=SCHOLARSHIPS_API_URL="http://scholarships-api-master.$OPENSHIFT_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/scholarships" --from-literal=STUDENT_API_URL="http://student-api-master.$COMMON_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/student" --from-literal=CHES_CLIENT_ID="$CHES_CLIENT_ID" --from-literal=CHES_CLIENT_SECRET="$CHES_CLIENT_SECRET" --from-literal=CHES_TOKEN_URL="$CHES_TOKEN_URL" --from-literal=CHES_ENDPOINT_URL="$CHES_ENDPOINT_URL" --from-literal=JDBC_URL="$DB_JDBC_CONNECT_STRING" --from-literal=PURGE_RECORDS_SAGA_AFTER_DAYS=400 --from-literal=SCHEDULED_JOBS_PURGE_OLD_SAGA_RECORDS_CRON="@midnight" --from-literal=DB_USERNAME="$DB_USER" --from-literal=DB_PASSWORD="$DB_PWD" --from-literal=KEYCLOAK_PUBLIC_KEY="$soamFullPublicKey" --from-literal=SPRING_SECURITY_LOG_LEVEL=INFO --from-literal=SPRING_WEB_LOG_LEVEL=INFO --from-literal=APP_LOG_LEVEL=INFO --from-literal=SPRING_BOOT_AUTOCONFIG_LOG_LEVEL=INFO --from-literal=SPRING_SHOW_REQUEST_DETAILS=false --from-literal=SPRING_JPA_SHOW_SQL="false" --from-literal=TOKEN_ISSUER_URL="https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID" --from-literal=NATS_MAX_RECONNECT=60 --from-literal=NATS_URL=$NATS_URL --from-literal=CLIENT_ID="grad-data-collection-api-service" --from-literal=CLIENT_SECRET="$GRAD_APIServiceClientSecret" --from-literal=THREADS_MIN_SUBSCRIBER="$THREADS_MIN_SUBSCRIBER" --from-literal=THREADS_MAX_SUBSCRIBER="$THREADS_MAX_SUBSCRIBER" --from-literal=SAGAS_MAX_PENDING="$SAGAS_MAX_PENDING" --from-literal=SAGAS_MAX_PARALLEL="$SAGAS_MAX_PARALLEL" --from-literal=TOKEN_URL="https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID/protocol/openid-connect/token" --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON" --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_LEAST_FOR" --from-literal=SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR="$SCHEDULED_JOBS_EXTRACT_UNCOMPLETED_SAGAS_CRON_LOCK_AT_MOST_FOR" --from-literal=SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON="$SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON" --from-literal=SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON_LOCK_AT_LEAST_FOR="$SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON_LOCK_AT_LEAST_FOR" --from-literal=SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON_LOCK_AT_MOST_FOR="$SCHEDULED_JOBS_PROCESS_LOADED_GRAD_STUDENTS_CRON_LOCK_AT_MOST_FOR" --from-literal=INCOMING_FILESET_STALE_IN_HOURS="$INCOMING_FILESET_STALE_IN_HOURS" --from-literal=SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON="$SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON" --from-literal=SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON_LOCK_AT_LEAST_FOR="$SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON_LOCK_AT_LEAST_FOR" --from-literal=SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON_LOCK_AT_MOST_FOR="$SCHEDULED_JOBS_PURGE_STALE_INCOMING_FILESETS_CRON_LOCK_AT_MOST_FOR" --from-literal=INSTITUTE_API_URL="http://institute-api-master.$COMMON_NAMESPACE-$envValue.svc.cluster.local:8080/api/v1/institute" --from-literal=NUMBER_OF_STUDENTS_TO_PROCESS_SAGA="$NUMBER_OF_STUDENTS_TO_PROCESS_SAGA" --from-literal=MAXIMUM_DB_POOL_SIZE="$MAXIMUM_DB_POOL_SIZE" --from-literal=MINIMUM_IDLE_DB_POOL_SIZE="$MINIMUM_IDLE_DB_POOL_SIZE" --from-literal=SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON="$SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON" --from-literal=SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON_LOCK_AT_LEAST_FOR="$SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON_LOCK_AT_LEAST_FOR" --from-literal=SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON_LOCK_AT_MOST_FOR="$SCHEDULED_JOBS_SETUP_REPORTING_PERIOD_CRON_LOCK_AT_MOST_FOR" --dry-run -o yaml | oc apply -f -

echo
echo Setting environment variables for $APP_NAME-$SOAM_KC_REALM_ID application
oc -n "$OPENSHIFT_NAMESPACE"-"$envValue" set env --from=configmap/$APP_NAME-config-map dc/$APP_NAME-$SOAM_KC_REALM_ID

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$OPENSHIFT_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map --from-literal=fluent-bit.conf="$FLB_CONFIG" --from-literal=parsers.conf="$PARSER_CONFIG" --dry-run -o yaml | oc apply -f -
