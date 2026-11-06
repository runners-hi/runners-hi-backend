#!/usr/bin/env bash
# 서버에서 실행한다. ~/release/ 에 올린 파일을 적용하고 서버가 뜰 때까지 기다린다.
# 상용(runnershi, 8080)을 먼저 올리고, 뜬 다음에 개발(runnershi-dev, 8081)을 올린다.
# 개발 서버가 실패해도 상용 배포는 실패로 치지 않는다.
set -euo pipefail
cd ~/release
sudo cp app.jar /srv/runnershi/app.jar
sudo cp -r pages/. /srv/runnershi/pages/
sudo cp Caddyfile /etc/caddy/Caddyfile
sudo cp runnershi.service /etc/systemd/system/runnershi.service
sudo cp runnershi-dev.service /etc/systemd/system/runnershi-dev.service
sudo systemctl daemon-reload
sudo systemctl restart runnershi
sudo systemctl reload caddy

wait_up() {
  for _ in $(seq 1 60); do
    if curl -fs -o /dev/null --max-time 5 "http://localhost:$1/actuator/health"; then
      return 0
    fi
    sleep 5
  done
  return 1
}

if ! wait_up 8080; then
  sudo journalctl -u runnershi -n 40 --no-pager
  exit 1
fi
echo "server is up"

# 개발 서버: 환경 파일이 있을 때만 올린다
if [ -f /srv/runnershi-dev/env ]; then
  sudo systemctl enable runnershi-dev >/dev/null 2>&1 || true
  sudo systemctl restart runnershi-dev
  if wait_up 8081; then
    echo "dev server is up"
  else
    echo "WARN: dev server did not come up" >&2
    sudo journalctl -u runnershi-dev -n 40 --no-pager || true
  fi
fi
