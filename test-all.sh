#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if command -v docker >/dev/null 2>&1; then
  echo "Running backend tests in Maven container..."
  docker run --rm \
    -v "$SCRIPT_DIR/backend:/workspace" \
    -v /workspace/target \
    -w /workspace \
    maven:3.9.9-eclipse-temurin-21 \
    mvn -q test

  echo "Running frontend tests and production build in Node container..."
  docker run --rm \
    -v "$SCRIPT_DIR/frontend:/workspace" \
    -v /workspace/node_modules \
    -w /workspace \
    node:22-alpine \
    sh -c "npm install --no-audit --no-fund && npm test && npm run build"
else
  echo "Docker not found; using local Java, Maven wrapper, Node, and npm."
  (cd "$SCRIPT_DIR/backend" && ./mvnw -q test)
  (cd "$SCRIPT_DIR/frontend" && npm install --no-audit --no-fund && npm test && npm run build)
fi

printf '\nAll tests and builds passed.\n'
