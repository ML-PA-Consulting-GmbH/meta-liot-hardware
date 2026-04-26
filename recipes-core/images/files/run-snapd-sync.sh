#!/bin/bash
set -euo pipefail

LOCAL_DIR="/etc/systemd"
REMOTE_DIR=/mnt/snapdata/etc/systemd #"${REMOTE_DIR:-}"
LOCK_FILE="/tmp/run-snapd-sync.lock"
SYNC_INTERVAL="${SYNC_INTERVAL:-20}"
RSYNC_BIN="${RSYNC_BIN:-rsync}"

log() { echo "[$1] ${2:-}"; }

parse_args() {
  local rootfs=""
  while [ $# -gt 0 ]; do
    case "$1" in
      --rootfs|-r)
        rootfs="$2"; shift 2 ;;
      --)
        shift; break ;;
      *)
        shift ;;
    esac
  done
  if [ -z "$rootfs" ]; then
    log error "--rootfs is required"
    exit 1
  fi
  #REMOTE_DIR="$rootfs/etc/systemd"
  log setup "Using ROOTFS=$rootfs"
  log setup "Using LOCAL_DIR=$LOCAL_DIR"
  log setup "Using REMOTE_DIR=$REMOTE_DIR"
}

require_tools() {
  if ! command -v "$RSYNC_BIN" >/dev/null 2>&1; then
    log error "rsync not found"
    exit 1
  fi
}

ensure_dirs() {
  mkdir -p "$LOCAL_DIR"
  mkdir -p "$REMOTE_DIR"
}

rsync_push() {
  "$RSYNC_BIN" -aHAX --delete --no-o --no-g "$LOCAL_DIR/" "$REMOTE_DIR/"
}

push_once() {
  exec 9>"$LOCK_FILE"
  if ! flock -n 9; then
    log sync "Another sync is in progress"
    return 0
  fi
  log sync "push start"
  rsync_push
  log sync "push done"
}

loop_sync() {
  while true; do
    push_once || true
    sleep "$SYNC_INTERVAL"
  done
}

main() {
  #parse_args "$@"
  require_tools
  ensure_dirs
  log loop "Pushing every $SYNC_INTERVAL seconds"
  loop_sync
}

main "$@"
