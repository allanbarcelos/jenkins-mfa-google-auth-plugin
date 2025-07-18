# MFA Google Auth Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/mfa-google-auth.svg)](https://plugins.jenkins.io/mfa-google-auth)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/mfa-google-auth.svg?color=blue)](https://plugins.jenkins.io/mfa-google-auth)
[![MIT License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

A Jenkins plugin that enforces **multi‑factor authentication (MFA)** using **TOTP Authenticator**.
This plugin adds an additional layer of security by requiring users to provide a time‑based one‑time password (TOTP) after entering their username and password.

---

## ✨ Features

✅ Enforces MFA for Jenkins users

✅ Integrates with [Google Authenticator](https://github.com/google/google-authenticator)

✅ Configurable enforcement policies

✅ Easy to set up and manage through Jenkins UI

---

## 📦 Installation

1. Build the plugin:

   ```bash
   mvn clean package
   ```

   After a successful build, you will find the `.hpi` file in the `target/` directory.

2. Upload to Jenkins:

   * Go to **Manage Jenkins → Manage Plugins → Advanced**
   * Use the **Upload Plugin** section to upload the `mfa-google-auth.hpi`.

3. Restart Jenkins if required.

---

## 🔧 Configuration

1. In Jenkins, go to **Manage Jenkins → Configure Global Security**.
2. Enable **MFA Google Auth Plugin** under the security realm or configure it as instructed in your environment.
3. Each user should scan the generated QR code with Google Authenticator (or a compatible TOTP app) and enter their verification code.

---

# 🔐 MFA TOTP Plugin - Global Configuration Guide

## 🛠 System Configuration

### 1. Enable Global MFA Enforcement
1. Navigate to **"Manage Jenkins"** > **"Configure System"**
2. Scroll to the **"Global MFA Settings"** section
3. Check **"Enforce MFA for all users"**:
   - ✅ Enabled: All users must set up MFA on next login
   - ❌ Disabled: MFA remains optional per user

### 2. Advanced Settings
| Setting | Description | Recommended Value |
|---------|-------------|-------------------|
| **Exclude API tokens** | Allows API access without MFA | Enable for CI/CD pipelines | 
| **MFA session duration (minutes)** | Time before re-verification | 480 (8 hours) |
| **Force reconfiguration** | Require periodic MFA reset | 90 days |

![Settings screenshot](path/to/screenshot.png)

## 👤 User Experience Flow
When global MFA is enabled:
1. At first login, users will be redirected to setup
2. They must:
   - Scan QR code with authenticator app
   - Verify with a valid TOTP code

## ⚙️ API Configuration (JSON)
```bash
curl -X POST http://jenkins/configure \
  -u admin:api_token \
  -H "Content-Type: application/json" \
  -d '{
    "mfa": {
      "enforceForAllUsers": true,
      "excludeApiTokens": true,
      "sessionDuration": 480
    }
  }'
```

## 🔐 Security Best Practices
1. **For Admins**:
   - Enable global MFA for all privileged accounts
   - Maintain one emergency break-glass account

2. **For Users**:
   - Use trusted authenticator apps (Google/Microsoft Authenticator)
   - Store recovery codes securely

## 🚨 Troubleshooting
### Issue: User cannot configure MFA
**Solution**:
1. Verify Jenkins server time synchronization
2. Check logs at `/var/log/jenkins/mfa.log`

### Issue: API returns 403 errors
**Solution**:
1. Enable "Exclude API tokens" in global settings  
2. Generate new API tokens after MFA activation

---

## 📌 Requirements

* **Jenkins Core**: `2.479.3` or newer
* **Java**: 17 or newer
* A TOTP app such as [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2).

---

## 🛠 Development

This plugin is built with the [Jenkins Plugin Parent POM](https://github.com/jenkinsci/plugin-pom).

To work on the plugin locally:

```bash
# Build and run tests
mvn clean verify

# Run Jenkins with the plugin
mvn hpi:run
```

Then open Jenkins at [http://localhost:8080/jenkins/](http://localhost:8080/jenkins/).


---

## Security

Security vulnerabilities should be reported following the [Jenkins vulnerability reporting guidelines](https://www.jenkins.io/security/#reporting-vulnerabilities). 

Please do not report security issues through GitHub issues or public discussions.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
