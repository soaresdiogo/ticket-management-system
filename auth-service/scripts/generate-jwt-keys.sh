#!/usr/bin/env bash
# Generate RSA key pair for JWT signing (RS256). Use for file-based key configuration.
# Usage: run from auth-service directory or project root.
# Output: keys/private.pem and keys/public.pem (add keys/ to .gitignore)

set -e
KEY_DIR="${1:-keys}"
mkdir -p "$KEY_DIR"

openssl genrsa -out "$KEY_DIR/private.pem" 2048
openssl rsa -in "$KEY_DIR/private.pem" -pubout -out "$KEY_DIR/public.pem"

echo "Generated $KEY_DIR/private.pem and $KEY_DIR/public.pem"
echo "Configure auth-service with: auth.jwt.private-key-path=$KEY_DIR/private.pem and auth.jwt.public-key-path=$KEY_DIR/public.pem"
