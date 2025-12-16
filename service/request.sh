#!/usr/bin/env bash


grpcurl -d '{ "name":"josh" }' \
  -H "Authorization: Basic $(echo -n 'josh:pw' | base64)" \
 -plaintext localhost:9090  GreetingsService.Greetings