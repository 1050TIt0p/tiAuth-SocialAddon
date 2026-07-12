# tiAuth-SocialAddon

An addon for **[tiAuth](https://github.com/1050TIt0p/tiAuth)** that adds social network integration (Discord, Telegram) to enhance your players' security.
The addon is in active development; functionality will be expanded. In the future, it may merge with **[tiAuth](https://github.com/1050TIt0p/tiAuth)** itself (but that's not certain).

**Platforms:** Velocity, BungeeCord (including forks such as NullCordX).

---

### Features
1. **Notifications:** Alerts when players join the server.
2. **Two-Factor Authentication (2FA):** Confirm login via social network.
3. **Access Control:** Change password and kick players through social network commands.

---

### Player Guide

1. **Linking an Account:**
   Run the command `/link <telegram|discord>` (if only one social network is enabled on the server, specifying it is optional).
   > *The `/link` command can be configured in `config.yml`*

2. **Activation:**
   Send the generated code to your bot.

3. **Done!**

#### Management:
* **Telegram:** Main management via buttons and keyboard in the bot interface. Commands: `/unlink <nick>`
* **Discord:** Main management via commands (will be changed soon to reduce the gap with Telegram). Commands (`/alert <nick>`, `/2fa <nick>`, `/unlink <nick>`).

---

### Warnings
* Discord and Telegram bots *may* not work if your hosting is in Russia. To fix this, use a SOCKS5 proxy (specified in the social network config).
* Possible issues with BossBar display on BungeeCord (adventure issue!!!)
