#!/usr/bin/env bash

# Docker 部署统一管理脚本。
# 用法：./manage.sh <list|validate|config|up|down|restart|logs|pull> [部署单元]

set -euo pipefail

docker_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
action="${1:-list}"
service="${2:-}"

list_units() {
  find "${docker_root}" -name docker-compose.yml -type f -print |
    sed "s#^${docker_root}/##; s#/docker-compose.yml\$##" |
    sort
}

if [[ "${action}" == "list" ]]; then
  list_units
  exit 0
fi

if [[ "${action}" == "validate" ]]; then
  command -v docker >/dev/null 2>&1 || {
    echo "未找到 docker 命令，请先安装 Docker Engine。" >&2
    exit 127
  }

  failed=0
  while IFS= read -r unit; do
    compose_file="${docker_root}/${unit}/docker-compose.yml"
    if ! docker compose \
      --project-directory "$(dirname "${compose_file}")" \
      -f "${compose_file}" \
      config --quiet; then
      echo "校验失败：${unit}" >&2
      failed=1
    fi
  done < <(list_units)

  if [[ "${failed}" -ne 0 ]]; then
    exit 1
  fi

  echo "全部部署单元校验通过。"
  exit 0
fi

case "${action}" in
  config|up|down|restart|logs|pull) ;;
  *)
    echo "不支持的操作：${action}" >&2
    exit 2
    ;;
esac

if [[ -z "${service}" ]]; then
  echo "请指定部署单元，例如：./manage.sh up mysql" >&2
  exit 2
fi

compose_file="${docker_root}/${service}/docker-compose.yml"
if [[ ! -f "${compose_file}" ]]; then
  echo "未找到部署单元：${service}" >&2
  exit 2
fi

command -v docker >/dev/null 2>&1 || {
  echo "未找到 docker 命令，请先安装 Docker Engine。" >&2
  exit 127
}

compose_args=(
  compose
  --project-directory "$(dirname "${compose_file}")"
  -f "${compose_file}"
)

case "${action}" in
  config) docker "${compose_args[@]}" config ;;
  up) docker "${compose_args[@]}" up -d ;;
  down) docker "${compose_args[@]}" down ;;
  restart) docker "${compose_args[@]}" restart ;;
  logs) docker "${compose_args[@]}" logs --tail 200 -f ;;
  pull) docker "${compose_args[@]}" pull ;;
esac
