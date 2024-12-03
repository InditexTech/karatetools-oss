@inditex-oss-karate
@generate-jwt
Feature: generate-jwt

Background:

Given def expected = "eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.eyJpZCI6IjEyMzQiLCJzdWIiOiJ1c2VybmFtZSIsImlzcyI6Imh0dHBzOi8vd3d3LmluZGl0ZXguY29tL2p3dC10b2tlbiIsImV4cCI6MjE0NzQ4MzY0NywiaWF0IjoxNzA0MDY3MjAwfQ.qISp3r1-VIuz6pfjKqryBqhtbiIAYx7qhLDKg0pn7HM"

Scenario: generate-jwt

Given def headers = 
"""
{
  "alg": "HS256",
  "kid": "test",
  "typ": "JWT"
}
""" 
Given def payloads = 
"""
{
  "id": "1234",
  "sub": "username",
  "iss": "https://www.inditex.com/jwt-token",
  "exp": 2147483647,
  "iat": 1704067200
}
"""
Given def secret = "aaaa1111-bb22-cc33-dd44-eeeeee555555"
Given def jwtData = 
"""
{
  headers: '#(headers)',
  payloads: '#(payloads)',
  secret: '#(secret)'
}
"""
When def jwt = Java.type('dev.inditex.karate.jwt.JWTGenerator').generateToken(jwtData)

Then match jwt == expected

Scenario: generate-jwt from default jwt file

Given def defaultJwt = karate.read('classpath:jwt/default-jwt.yml')
Given def jwtData = 
"""
{ 
  secret: '#(defaultJwt.secret)', 
  headers: '#(defaultJwt.headers)', 
  payloads: '#(defaultJwt.payloads)'
}
""" 

When def jwt = Java.type('dev.inditex.karate.jwt.JWTGenerator').generateToken(jwtData)

Then match jwt == expected