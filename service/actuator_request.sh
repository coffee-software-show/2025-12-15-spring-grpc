#!/usr/bin/env bash

grpcurl \
  -H "Authorization: Basic $(echo -n 'josh:pw' | base64)" \
 -plaintext localhost:9090 list grpc.health.v1.Health

grpcurl \
  -H "Authorization: Basic $(echo -n 'josh:pw' | base64)" \
 -plaintext localhost:9090   grpc.health.v1.Health/Check


grpcurl \
  -H "Authorization: Basic $(echo -n 'josh:pw' | base64)" \
 -plaintext localhost:9090   grpc.health.v1.Health/Watch

