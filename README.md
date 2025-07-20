# 🔐 MFA TOTP Authentication Plugin for Jenkins

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/mfa-totp-plugin.svg)](https://plugins.jenkins.io/mfa-google-auth)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/mfa-totp-plugin.svg?color=blue)](https://plugins.jenkins.io/mfa-google-auth)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/mfa-totp-plugin/master)](https://ci.jenkins.io/job/Plugins/job/mfa-totp-plugin/)
[![MIT License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

Enhance your Jenkins security with Time-based One-Time Password (TOTP) multi-factor authentication. This plugin integrates seamlessly with authenticator apps like Google Authenticator to provide an additional layer of protection beyond passwords.

## 🌟 Key Features

- **🔒 Stronger Security**: Mandates MFA for all Jenkins access
- **📱 TOTP Support**: Works with Google/Microsoft Authenticator and other TOTP apps
- **⚙️ Flexible Configuration**: Global enforcement or per-user setup
- **⏱️ Session Management**: Configurable authentication duration
- **🤖 CI/CD Friendly**: Optional API token exclusion for automation

## 📥 Installation

### Prerequisites
- Jenkins 2.387.3 or later
- Java 17+

### Installation Methods

#### Method 1: Jenkins Plugin Manager
1. Navigate to **Manage Jenkins** → **Plugins** → **Available plugins**
2. Search for "MFA TOTP Plugin"
3. Install and restart Jenkins

#### Method 2: Manual Installation
```bash
# Build the plugin
mvn clean package

# Then upload the .hpi file from target/ directory via:
# Manage Jenkins → Plugins → Advanced → Upload Plugin
```

## 🛠 Configuration Guide

### Global Security Settings
1. Go to **Manage Jenkins** → **Configure Global Security**
2. Under *Security Realm*, enable **MFA TOTP Authentication**
3. Configure enforcement policies:
   - Enforce for all users
   - Session duration (default: 8 hours)
   - API token exclusion

### User Setup Flow
1. Users access their account settings
2. Scan the QR code with an authenticator app
3. Verify initial code
4. Save backup codes securely

![Configuration Screenshot](docs/images/config-screen.png)

## ⚙️ Advanced Configuration

### JSON API Configuration
```bash
curl -X POST http://your-jenkins/configure \
  -u admin:api_token \
  -H "Content-Type: application/json" \
  -d '{
    "mfa": {
      "enforceForAllUsers": true,
      "excludeApiTokens": false,
      "sessionDuration": 480
    }
  }'
```

### Security Best Practices
1. **For Admins**:
   - Enable MFA for all administrative accounts
   - Maintain emergency break-glass credentials
   - Regularly review MFA configurations

2. **For Users**:
   - Use trusted authenticator apps
   - Store recovery codes securely
   - Rotate MFA secrets annually

## 🚨 Troubleshooting

| Issue | Solution |
|-------|----------|
| Invalid TOTP codes | Verify server time synchronization |
| API access denied | Enable "Exclude API tokens" in settings |
| QR code not appearing | Check browser console for JavaScript errors |

For additional help, check the plugin logs at:
`$JENKINS_HOME/logs/mfa-totp-plugin.log`

## 🛠 Development

### Build Environment
```bash
mvn clean verify       # Run full build with tests
mvn hpi:run            # Launch test Jenkins instance
```

### Contribution Guidelines
1. Fork the repository
2. Create feature branches (`feature/your-feature`)
3. Submit pull requests with clear descriptions

## 🔒 Security Policy

Vulnerabilities should be reported following [Jenkins security guidelines](https://www.jenkins.io/security/#reporting-vulnerabilities). Please do not disclose security issues publicly.

## 📜 License

Licensed under [MIT License](LICENSE). 

---

✉️ **Contact**: For support questions, please use the [Jenkins community forums](https://community.jenkins.io/).

This version improves:
1. Better visual hierarchy with emoji categorization
2. More detailed configuration instructions
3. Clearer tables for troubleshooting
4. Improved contribution guidelines
5. Better separation of admin vs user instructions
6. More professional tone throughout
7. Added missing sections (contact, development)
8. Consistent formatting