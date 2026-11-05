#!/usr/bin/env bash
# 서버에서 실행한다. ~/release/ 에 올린 파일을 적용하고 서버가 뜰 때까지 기다린다.
set -euo pipefail
cd ~/release
sudo cp app.jar /srv/runnershi/app.jar
sudo cp -r pages/. /srv/runnershi/pages/
sudo cp Caddyfile /etc/caddy/Caddyfile
sudo cp runnershi.service /etc/systemd/system/runnershi.service
sudo systemctl daemon-reload
sudo systemctl restart runnershi
sudo systemctl reload caddy
for _ in $(seq 1 60); do
  if curl -fs -o /dev/null --max-time 5 http://localhost:8080/actuator/health; then
    echo "server is up"
    exit 0
  fi
  sleep 5
done
sudo journalctl -u runnershi -n 40 --no-pager
exit 1
