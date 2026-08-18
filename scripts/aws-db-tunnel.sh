#!/usr/bin/env bash
# Open a local SSM tunnel to the private Paw RDS instance through the
# existing Elastic Beanstalk EC2 (private subnet + NAT). Extra AWS cost: $0.
#
# Usage:
#   ./scripts/aws-db-tunnel.sh
#
# Then connect with DBeaver / TablePlus / psql:
#   Host: 127.0.0.1
#   Port: 15432
#   Database / user / password: from Secrets Manager (printed below; password is not echoed)
set -euo pipefail

REGION="${AWS_REGION:-ap-northeast-2}"
EB_ENVIRONMENT_NAME="${EB_ENVIRONMENT_NAME:-Paw-env}"
LOCAL_PORT="${LOCAL_PORT:-15432}"
RDS_PORT="${RDS_PORT:-5432}"

# Prefer a user-local plugin install (no sudo) over Homebrew/system paths.
export PATH="${HOME}/.local/bin:${PATH}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "error: '$1' is not installed." >&2
    if [[ "$1" == "session-manager-plugin" ]]; then
      echo "Install without sudo:" >&2
      echo "  curl -fsSL https://s3.amazonaws.com/session-manager-downloads/plugin/latest/mac_arm64/sessionmanager-bundle.zip -o /tmp/sessionmanager-bundle.zip" >&2
      echo "  unzip -o /tmp/sessionmanager-bundle.zip -d /tmp" >&2
      echo "  python3 /tmp/sessionmanager-bundle/install -i \"\$HOME/.local/sessionmanagerplugin\" -b \"\$HOME/.local/bin/session-manager-plugin\"" >&2
    fi
    exit 1
  fi
}

require_cmd aws
require_cmd session-manager-plugin

aws sts get-caller-identity --region "$REGION" >/dev/null

echo "Resolving Elastic Beanstalk instance in ${EB_ENVIRONMENT_NAME}..."
INSTANCE_ID="$(
  aws elasticbeanstalk describe-environment-resources \
    --environment-name "$EB_ENVIRONMENT_NAME" \
    --region "$REGION" \
    --query 'EnvironmentResources.Instances[0].Id' \
    --output text
)"

if [[ -z "$INSTANCE_ID" || "$INSTANCE_ID" == "None" ]]; then
  echo "error: no EC2 instance found for environment ${EB_ENVIRONMENT_NAME}" >&2
  exit 1
fi

PING_STATUS="$(
  aws ssm describe-instance-information \
    --region "$REGION" \
    --filters "Key=InstanceIds,Values=${INSTANCE_ID}" \
    --query 'InstanceInformationList[0].PingStatus' \
    --output text
)"

if [[ "$PING_STATUS" != "Online" ]]; then
  echo "error: instance ${INSTANCE_ID} is not registered with SSM (PingStatus=${PING_STATUS})." >&2
  echo "The EB instance role needs AmazonSSMManagedInstanceCore, and the agent must have refreshed credentials." >&2
  exit 1
fi

SECRET_PREFIX="paw/eb/${EB_ENVIRONMENT_NAME}"
JDBC_URL="$(
  aws secretsmanager get-secret-value \
    --secret-id "${SECRET_PREFIX}/AWS_DB_URL" \
    --region "$REGION" \
    --query SecretString \
    --output text
)"

# jdbc:postgresql://host:5432/dbname -> host, dbname
RDS_HOST="$(sed -E 's|^jdbc:postgresql://([^:/]+).*|\1|' <<<"$JDBC_URL")"
DB_NAME="$(sed -E 's|^jdbc:postgresql://[^/]+/([^?]+).*|\1|' <<<"$JDBC_URL")"

if [[ -z "$RDS_HOST" || "$RDS_HOST" == "$JDBC_URL" ]]; then
  echo "error: could not parse RDS host from ${SECRET_PREFIX}/AWS_DB_URL" >&2
  exit 1
fi

echo
echo "Tunnel: 127.0.0.1:${LOCAL_PORT} -> ${RDS_HOST}:${RDS_PORT} via ${INSTANCE_ID}"
echo "Database: ${DB_NAME}"
echo "Username secret: ${SECRET_PREFIX}/AWS_DB_USERNAME"
echo "Password secret: ${SECRET_PREFIX}/AWS_DB_PASSWORD"
echo
echo "Fetch credentials (password will print once):"
echo "  aws secretsmanager get-secret-value --secret-id ${SECRET_PREFIX}/AWS_DB_USERNAME --region ${REGION} --query SecretString --output text"
echo "  aws secretsmanager get-secret-value --secret-id ${SECRET_PREFIX}/AWS_DB_PASSWORD --region ${REGION} --query SecretString --output text"
echo
echo "psql example:"
echo "  psql \"host=127.0.0.1 port=${LOCAL_PORT} dbname=${DB_NAME} user=<username> sslmode=require\""
echo
echo "Keep this terminal open. Ctrl-C to close the tunnel."
echo

exec aws ssm start-session \
  --region "$REGION" \
  --target "$INSTANCE_ID" \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters "{\"host\":[\"${RDS_HOST}\"],\"portNumber\":[\"${RDS_PORT}\"],\"localPortNumber\":[\"${LOCAL_PORT}\"]}"
