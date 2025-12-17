#!/usr/bin/env bash

grpcurl -d '{ "name":"jane" }' \
  -H "Authorization: Basic $(echo -n 'josh:pw' | base64)" \
 -plaintext localhost:9090 GreetingService.Greet

#grpcurl -plaintext localhost:9090  grpc.health.v1.Health/Check