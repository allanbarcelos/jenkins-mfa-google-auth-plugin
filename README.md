# MFA Google Auth Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/mfa-google-auth.svg)](https://plugins.jenkins.io/mfa-google-auth)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/mfa-google-auth.svg?color=blue)](https://plugins.jenkins.io/mfa-google-auth)
[![MIT License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

A Jenkins plugin that enforces **multi‑factor authentication (MFA)** using **Google Authenticator**.
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

## 📜 License

This project is licensed under the [MIT License](LICENSE).
