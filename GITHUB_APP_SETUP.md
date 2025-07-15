# GitHub App Setup Guide

This guide explains how to convert from GitHub OAuth Apps to GitHub Apps and configure your application.

## Key Differences: OAuth Apps vs GitHub Apps

| Feature | OAuth Apps (Current) | GitHub Apps (New) |
|---------|---------------------|-------------------|
| **Authentication** | `client_id` + `client_secret` | `app_id` + `private_key` |
| **Tokens** | User access tokens | Installation access tokens |
| **Scope** | User-to-server | App-to-server + Installation-based |
| **Rate Limits** | 5,000/hour per user | 5,000/hour per installation |
| **Installation** | Per-user OAuth | Per-repository/organization |
| **Webhooks** | Manual setup | Built-in webhook management |

## Step 1: Create a GitHub App

1. **Go to GitHub Developer Settings**:
   - Navigate to [GitHub Developer Settings](https://github.com/settings/developers)
   - Click "GitHub Apps" → "New GitHub App"

2. **Configure Basic Information**:
   ```
   GitHub App name: Your-App-Name
   Homepage URL: https://yourapp.com
   Description: Your app description
   ```

3. **Set Webhook Configuration**:
   ```
   Webhook URL: https://yourapp.com/api/users/github-app/installation
   Webhook secret: (generate a secure secret)
   ```

4. **Configure Permissions**:
   - **Repository permissions**:
     - Contents: Read & Write (for file operations)
     - Metadata: Read (for repository info)
     - Pull requests: Read & Write (if needed)
   - **Account permissions**:
     - Email addresses: Read (if needed)

5. **Subscribe to Events**:
   - Installation
   - Installation repositories

6. **Generate Private Key**:
   - Scroll down and click "Generate a private key"
   - Download the `.pem` file

## Step 2: Configure Application Properties

Add the following to your `application.properties` or `application.yml`:

```properties
# GitHub App Configuration
github.app.id=YOUR_APP_ID
github.app.private-key=-----BEGIN PRIVATE KEY-----\nYOUR_PRIVATE_KEY_CONTENT\n-----END PRIVATE KEY-----

# Optional: Keep OAuth configuration for backward compatibility
spring.security.oauth2.client.registration.github.client-id=YOUR_OAUTH_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_OAUTH_CLIENT_SECRET
```

**Note**: Replace `\n` with actual newlines in the private key, or store it in a file and reference it.

### Alternative: Store Private Key in File

```properties
github.app.id=YOUR_APP_ID
github.app.private-key-file=classpath:github-app-private-key.pem
```

## Step 3: Database Migration

Run the following SQL to create the GitHub App installations table:

```sql
CREATE TABLE github_app_installations (
    id BIGSERIAL PRIMARY KEY,
    installation_id BIGINT NOT NULL UNIQUE,
    installation_token TEXT,
    token_expires_at TIMESTAMP,
    account_login VARCHAR(100),
    account_id BIGINT,
    account_type VARCHAR(20),
    repository_selection VARCHAR(20),
    permissions TEXT,
    suspended_at TIMESTAMP,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_github_installations_user_id ON github_app_installations(user_id);
CREATE INDEX idx_github_installations_installation_id ON github_app_installations(installation_id);
```

## Step 4: Testing the GitHub App Setup

### 1. Test Configuration

```bash
curl http://localhost:8080/api/users/github-app/test-config
```

Expected response:
```json
{
  "configurationValid": true,
  "appName": "Your-App-Name",
  "appSlug": "your-app-name",
  "message": "GitHub App configuration is valid"
}
```

### 2. Get Installation URL

```bash
curl http://localhost:8080/api/users/github-app/install
```

Expected response:
```json
{
  "installUrl": "https://github.com/apps/your-app-name/installations/new",
  "message": "Redirect user to this URL to install the GitHub App"
}
```

### 3. Manual Installation Setup

After installing the GitHub App on a repository:

```bash
curl -X POST "http://localhost:8080/api/users/github-app/setup" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d "installationId=12345&accountLogin=your-username"
```

### 4. List User Installations

```bash
curl http://localhost:8080/api/users/github-app/installations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Get Installation Token

```bash
curl http://localhost:8080/api/users/github-app/installation/12345/token \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Step 5: Update Your Frontend

### OAuth Flow (Existing)
```javascript
// Initiate OAuth
const response = await fetch('/api/users/github/login');
const { authUrl } = await response.json();
window.location.href = authUrl;
```

### GitHub App Flow (New)
```javascript
// Get installation URL
const response = await fetch('/api/users/github-app/install');
const { installUrl } = await response.json();
window.location.href = installUrl;

// After installation, user returns to your app
// Check installations
const installations = await fetch('/api/users/github-app/installations', {
  headers: { 'Authorization': `Bearer ${userToken}` }
}).then(r => r.json());
```

## Step 6: Webhook Handling

Your app will receive webhooks at `/api/users/github-app/installation` when:

- **Installation created**: User installs your app
- **Installation deleted**: User uninstalls your app  
- **Installation suspended**: App access is suspended

The webhook handler automatically:
- Creates/updates installation records
- Associates installations with authenticated users
- Manages installation lifecycle

## Step 7: Using GitHub App in Code

### Get Installation Token for API Calls

```java
@Autowired
private GithubAppService githubAppService;

@Autowired
private GithubClient githubClient;

// Get installation token
String token = githubAppService.getInstallationAccessToken(installationId);

// Use GitHub App methods in GithubClient
ResponseEntity<GithubRepoContents> repo = githubClient.createRepoWithInstallation(
    installationId, "my-new-repo"
);

ResponseEntity<GithubFile> file = githubClient.addFileToRepoWithInstallation(
    installationId, "owner", "repo", "README.md", "main", 
    "# Hello World", "Initial commit"
);
```

## Migration Strategy

### Option 1: Parallel Support (Recommended)
- Keep existing OAuth endpoints functional
- Add new GitHub App endpoints
- Let users choose their preferred method
- Gradually migrate users to GitHub Apps

### Option 2: Complete Migration
- Deprecate OAuth endpoints
- Migrate existing OAuth tokens to GitHub App installations
- Force all users to reinstall via GitHub Apps

## Troubleshooting

### Common Issues

1. **"GitHub App configuration is invalid"**
   - Check that `github.app.id` is correct
   - Verify private key format (PEM with proper newlines)
   - Ensure private key matches the GitHub App

2. **"Installation not found"**
   - User hasn't installed the GitHub App yet
   - Installation ID is incorrect
   - App was uninstalled

3. **"Token expired"**
   - Installation tokens expire after 1 hour
   - Service automatically refreshes tokens
   - Check database for token expiration times

4. **Webhook not receiving events**
   - Verify webhook URL is accessible
   - Check webhook secret configuration
   - Ensure ngrok/tunnel for local development

### Debug Commands

```bash
# Check app installations
curl -H "Authorization: Bearer $(echo -n 'your-jwt' | base64)" \
  https://api.github.com/app/installations

# Verify JWT generation
curl -H "Authorization: Bearer YOUR_APP_JWT" \
  https://api.github.com/app
```

## Security Considerations

1. **Private Key Storage**: Store securely, never commit to version control
2. **Webhook Secrets**: Validate webhook signatures
3. **Token Encryption**: Installation tokens are encrypted in database
4. **Rate Limiting**: GitHub Apps have higher rate limits per installation
5. **Permissions**: Request only necessary permissions

## Benefits of GitHub Apps

1. **Better Rate Limits**: 5,000 requests/hour per installation
2. **Granular Permissions**: Repository-level access control
3. **Built-in Webhooks**: Automatic event handling
4. **Organization Support**: Easier organization-wide installations
5. **User Attribution**: Actions appear as the app, not individual users

Your GitHub App integration is now ready! Users can install your app on their repositories and your application will have secure, token-based access to perform GitHub operations. 