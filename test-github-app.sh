#!/bin/bash

# Test script for GitHub App endpoints
# Usage: ./test-github-app.sh [JWT_TOKEN]

BASE_URL="http://localhost:8080"
JWT_TOKEN=$1

echo "🚀 Testing GitHub App Integration"
echo "=================================="

# Test 1: Configuration Test
echo ""
echo "📋 Test 1: Testing GitHub App Configuration"
response=$(curl -s -w "HTTP_STATUS:%{http_code}" "$BASE_URL/api/users/github-app/test-config")
http_status=$(echo $response | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
content=$(echo $response | sed 's/HTTP_STATUS:[0-9]*$//')

if [ "$http_status" -eq 200 ]; then
    echo "✅ Configuration Test: PASSED"
    echo "Response: $content"
else
    echo "❌ Configuration Test: FAILED (HTTP $http_status)"
    echo "Response: $content"
fi

# Test 2: Installation URL
echo ""
echo "🔗 Test 2: Getting GitHub App Installation URL"
response=$(curl -s -w "HTTP_STATUS:%{http_code}" "$BASE_URL/api/users/github-app/install")
http_status=$(echo $response | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
content=$(echo $response | sed 's/HTTP_STATUS:[0-9]*$//')

if [ "$http_status" -eq 200 ]; then
    echo "✅ Installation URL Test: PASSED"
    echo "Response: $content"
else
    echo "❌ Installation URL Test: FAILED (HTTP $http_status)"
    echo "Response: $content"
fi

if [ -z "$JWT_TOKEN" ]; then
    echo ""
    echo "⚠️  Skipping authenticated tests - no JWT token provided"
    echo "💡 Usage: $0 YOUR_JWT_TOKEN"
    exit 0
fi

# Test 3: List User Installations (requires authentication)
echo ""
echo "📦 Test 3: Listing User GitHub App Installations"
response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    "$BASE_URL/api/users/github-app/installations")
http_status=$(echo $response | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
content=$(echo $response | sed 's/HTTP_STATUS:[0-9]*$//')

if [ "$http_status" -eq 200 ]; then
    echo "✅ User Installations Test: PASSED"
    echo "Response: $content"
else
    echo "❌ User Installations Test: FAILED (HTTP $http_status)"
    echo "Response: $content"
fi

# Test 4: Manual Installation Setup (requires installation ID)
echo ""
echo "🔧 Test 4: Manual Installation Setup"
echo "💡 This test requires an actual GitHub App installation"
echo "   To test manually:"
echo "   curl -X POST '$BASE_URL/api/users/github-app/setup' \\"
echo "     -H 'Authorization: Bearer $JWT_TOKEN' \\"
echo "     -d 'installationId=YOUR_INSTALLATION_ID&accountLogin=YOUR_GITHUB_USERNAME'"

# Test 5: Installation Token Retrieval (requires installation ID)
echo ""
echo "🎫 Test 5: Installation Token Retrieval"
echo "💡 This test requires an active installation"
echo "   To test manually:"
echo "   curl '$BASE_URL/api/users/github-app/installation/INSTALLATION_ID/token' \\"
echo "     -H 'Authorization: Bearer $JWT_TOKEN'"

# Test 6: Webhook Simulation
echo ""
echo "🪝 Test 6: Webhook Simulation"
echo "💡 Testing webhook endpoint with mock data"

webhook_payload='{
  "action": "created",
  "installation": {
    "id": 12345,
    "account": {
      "login": "test-user",
      "id": 67890,
      "type": "User"
    },
    "repository_selection": "selected"
  }
}'

response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
    -X POST \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$webhook_payload" \
    "$BASE_URL/api/users/github-app/installation")
http_status=$(echo $response | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
content=$(echo $response | sed 's/HTTP_STATUS:[0-9]*$//')

if [ "$http_status" -eq 200 ]; then
    echo "✅ Webhook Simulation Test: PASSED"
    echo "Response: $content"
else
    echo "❌ Webhook Simulation Test: FAILED (HTTP $http_status)"
    echo "Response: $content"
fi

echo ""
echo "🏁 Testing Complete!"
echo ""
echo "📚 Next Steps:"
echo "1. Create a GitHub App at https://github.com/settings/apps"
echo "2. Configure webhook URL: $BASE_URL/api/users/github-app/installation"
echo "3. Set up environment variables with App ID and private key"
echo "4. Install the app on a test repository"
echo "5. Use the installation setup endpoint to link installations to users"
echo ""
echo "📖 See GITHUB_APP_SETUP.md for detailed setup instructions" 