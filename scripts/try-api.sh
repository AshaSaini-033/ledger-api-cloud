#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${EMAIL:-asha@example.com}"
PASSWORD="${PASSWORD:-change-this-password-123}"

echo "Registering demo user..."
REGISTER_RESPONSE=$(curl -fsS -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" || true)

if [[ -z "$REGISTER_RESPONSE" ]]; then
  echo "Registration did not return a response. The user may already exist."
fi

echo "Logging in..."
LOGIN_RESPONSE=$(curl -fsS -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN_RESPONSE" | python -c 'import json,sys; print(json.load(sys.stdin)["token"])')

echo "Creating INR account..."
ACCOUNT_RESPONSE=$(curl -fsS -X POST "$BASE_URL/api/accounts" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"currency":"INR"}')
ACCOUNT_ID=$(printf '%s' "$ACCOUNT_RESPONSE" | python -c 'import json,sys; print(json.load(sys.stdin)["id"])')

echo "Depositing 1000.00..."
curl -fsS -X POST "$BASE_URL/api/accounts/$ACCOUNT_ID/deposit" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"amount":1000.00,"description":"Initial demo funding"}'
echo
echo "Account $ACCOUNT_ID is ready. Check transactions at:"
echo "$BASE_URL/api/accounts/$ACCOUNT_ID/transactions"
