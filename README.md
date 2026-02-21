<a name="readme-top"></a>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]
[![Discord][discord-shield]][discord-url]
[![Modrinth][modrinth-shield]][modrinth-url]

<br />
<div align="center">
  <a href="https://github.com/syorito-hatsuki/ducky-updater-rework">
    <img src="https://github.com/syorito-hatsuki/ducky-updater-rework/blob/1.20/src/main/resources/assets/duckyupdaterrework/icon.png?raw=true" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Ducky Updater: ReWork</h3>

  <p align="center">
    Simple utility for update mods using Modrinth API
    <br />
    <a href="https://discord.gg/pbwnMwnUD6">Support</a>
    ·
    <a href="https://github.com/syorito-hatsuki/ducky-updater-rework/issues">Report Bug</a>
    ·
    <a href="https://github.com/syorito-hatsuki/ducky-updater-rework/issues">Request Feature</a>
  </p>
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#commands-and-permissions">Commands and permissions</a></li>
        <li><a href="#config">Config</a></li>
        </ul>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

## About The Project

![In-Game ScreenShot][screenshot]

Mod that give possibility to update mods without using third-party launchers

> ReWorked version of old Ducky Updater that has many issues, legacy code and bad performance

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* ![Fabric][fabric]
* ![Fabric-Language-Kotlin][fabric-language-kotlin]
* ![ModMenu Badges Lib][modmenu-badges-lib]
* ![fStats][fstats]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Usage

### Commands and permissions

<details>
  <summary>Client</summary>

| Command                                                   | OP | Permission | Description                                  |
|-----------------------------------------------------------|----|------------|----------------------------------------------|
| `/durw-client check datapack`                             | ❌  | `none`     | Check for datapack updates                   |
| `/durw-client check fabric`                               | ❌  | `none`     | Check for fabirc updates                     |
| `/durw-client clear-cache`                                | ❌  | `none`     | Clear database aka cache                     |
| `/durw-client ignore by datapack-list`                    | ❌  | `none`     | List ignored datapacks                       |
| `/durw-client ignore by fabric-list`                      | ❌  | `none`     | List ignored fabric mods                     |
| `/durw-client ignore by mod-id <modId> <ignore>`          | ❌  | `none`     | Ignore mod updates by mod id                 |
| `/durw-client ignore by fabric-id <projectId> <ignore>`   | ❌  | `none`     | Ignore mod updates by fabric id              |
| `/durw-client ignore by datapack-id <projectId> <ignore>` | ❌  | `none`     | Ignore mod updates by datapack file name     |
| `/durw-client update fabric-all`                          | ❌  | `none`     | Update all fabric mods                       |
| `/durw-client update datapack-all`                        | ❌  | `none`     | Update all datapacks                         |
| `/durw-client update by mod-ids <modIds>`                 | ❌  | `none`     | Update specific fabric mod(s) by mod ids     |
| `/durw-client update by fabric-ids <projectIds>`          | ❌  | `none`     | Update specific fabric mod(s) by project ids |
| `/durw-client update by datapack-ids <projectIds>`        | ❌  | `none`     | Update specific datapack(s) by project ids   |
| `/durw-client config download-mode <mode>`                | ❌  | `none`     | Setup [downloading mod](#config)             |
| `/durw-client config file-action <action>`                | ❌  | `none`     | Setup [file action](#config)                 |
| `/durw-client config check-update-on-boot <check>`        | ❌  | `none`     | Setup [update checking on boot](#config)     |

</details>

<details>
  <summary>Server</summary>

| Command                                                   | OP | Permission | Description                                  |
|-----------------------------------------------------------|----|------------|----------------------------------------------|
| `/durw-server check datapack`                             | ✅  | `none`     | Check for datapack updates                   |
| `/durw-server check fabric`                               | ✅  | `none`     | Check for fabric updates                     |
| `/durw-server clear-cache`                                | ✅  | `none`     | Clear database aka cache                     |
| `/durw-server ignore by datapack-list`                    | ✅  | `none`     | List ignored datapacks                       |
| `/durw-server ignore by fabric-list`                      | ✅  | `none`     | List ignored fabric mods                     |
| `/durw-server ignore by mod-id <modId> <ignore>`          | ✅  | `none`     | Ignore mod updates by mod id                 |
| `/durw-server ignore by fabric-id <projectId> <ignore>`   | ✅  | `none`     | Ignore mod updates by fabric id              |
| `/durw-server ignore by datapack-id <projectId> <ignore>` | ✅  | `none`     | Ignore mod updates by datapack file name     |
| `/durw-server update fabric-all`                          | ✅  | `none`     | Update all fabric mods                       |
| `/durw-server update datapack-all`                        | ✅  | `none`     | Update all datapacks                         |
| `/durw-server update by mod-ids <modIds>`                 | ✅  | `none`     | Update specific fabric mod(s) by mod ids     |
| `/durw-server update by fabric-ids <projectIds>`          | ✅  | `none`     | Update specific fabric mod(s) by project ids |
| `/durw-server update by datapack-ids <projectIds>`        | ✅  | `none`     | Update specific datapack(s) by project ids   |
| `/durw-server config download-mode <mode>`                | ✅  | `none`     | Setup [downloading mod](#config)             |
| `/durw-server config file-action <action>`                | ✅  | `none`     | Setup [file action](#config)                 |
| `/durw-server config check-update-on-boot <check>`        | ✅  | `none`     | Setup [update checking on boot](#config)     |

</details>

<details>
<summary>Before 2025.4.1</summary>


<details>
  <summary>Client</summary>

| Command                                                  | OP | Permission | Description                              |
|----------------------------------------------------------|----|------------|------------------------------------------|
| `/durw-client check`                                     | ❌  | `none`     | Check for updates                        |
| `/durw-client ignore by mod-id <modId> <ignore>`         | ❌  | `none`     | Ignore mod updates by mod id             |
| `/durw-client ignore by project-id <projectId> <ignore>` | ❌  | `none`     | Ignore mod updates by project id         |
| `/durw-client update all`                                | ❌  | `none`     | Update all mods                          |
| `/durw-client update by mod-ids <modIds>`                | ❌  | `none`     | Update specific mod(s) by mod ids        |
| `/durw-client update by project-ids <projectIds>`        | ❌  | `none`     | Update specific mod(s) by project ids    |
| `/durw-client config download-mode <mode>`               | ❌  | `none`     | Setup [downloading mod](#config)         |
| `/durw-client config file-action <action>`               | ❌  | `none`     | Setup [file action](#config)             |
| `/durw-client config check-update-on-boot <check>`       | ❌  | `none`     | Setup [update checking on boot](#config) |

</details>

<details>
  <summary>Server</summary>

| Command                                                  | OP | Permission | Description                              |
|----------------------------------------------------------|----|------------|------------------------------------------|
| `/durw-server check`                                     | ✅  | `none`     | Check for updates                        |
| `/durw-server ignore by mod-id <modId> <ignore>`         | ✅  | `none`     | Ignore mod updates by mod id             |
| `/durw-server ignore by project-id <projectId> <ignore>` | ✅  | `none`     | Ignore mod updates by project id         |
| `/durw-server update all`                                | ✅  | `none`     | Update all mods                          |
| `/durw-server update by mod-ids <modIds>`                | ✅  | `none`     | Update specific mod(s) by mod ids        |
| `/durw-server update by project-ids <projectIds>`        | ✅  | `none`     | Update specific mod(s) by project ids    |
| `/durw-server config download-mode <mode>`               | ✅  | `none`     | Setup [downloading mod](#config)         |
| `/durw-server config file-action <action>`               | ✅  | `none`     | Setup [file action](#config)             |
| `/durw-server config check-update-on-boot <check>`       | ✅  | `none`     | Setup [update checking on boot](#config) |

</details>

</details>

### Config

```json5
{
  // Check updates on every server/client boot
  "checkUpdatesOnBoot": true,
  // Download Modes
  // PARALLEL -> Download and work with multiplied files at the same time (Default)
  // SEQUENTIALLY -> Download and work with file one by one (Recommended for bad network) 
  "downloadMode": "PARALLEL",
  // File Action
  // ARCHIVE -> Making a ZIP file with the old version of all updated mods (Default)
  // DELETE -> Delete all old files (Best for server's that have small disk, use on own risk)
  // DISABLE -> Add to end of old files suffix .disable
  "fileAction": "ARCHIVE"
}
```

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## License

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

[contributors-shield]: https://img.shields.io/github/contributors/syorito-hatsuki/ducky-updater-rework.svg?style=for-the-badge

[contributors-url]: https://github.com/syorito-hatsuki/ducky-updater-rework/graphs/contributors

[forks-shield]: https://img.shields.io/github/forks/syorito-hatsuki/ducky-updater-rework.svg?style=for-the-badge

[forks-url]: https://github.com/syorito-hatsuki/ducky-updater-rework/network/members

[stars-shield]: https://img.shields.io/github/stars/syorito-hatsuki/ducky-updater-rework.svg?style=for-the-badge

[stars-url]: https://github.com/syorito-hatsuki/ducky-updater-rework/stargazers

[issues-shield]: https://img.shields.io/github/issues/syorito-hatsuki/ducky-updater-rework.svg?style=for-the-badge

[issues-url]: https://github.com/syorito-hatsuki/ducky-updater-rework/issues

[license-shield]: https://img.shields.io/github/license/syorito-hatsuki/ducky-updater-rework.svg?style=for-the-badge

[license-url]: https://github.com/syorito-hatsuki/ducky-updater-rework/blob/master/LICENSE

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555

[linkedin-url]: https://linkedin.com/in/kit-lehto

[screenshot]: https://cdn-raw.modrinth.com/data/Ex3jKEPK/images/96b83a31e02c2c618594034e31c91cb30db2c12c.png

[fabric]: https://img.shields.io/badge/fabric%20api-DBD0B4?style=for-the-badge

[fabric-language-kotlin]: https://img.shields.io/badge/fabric%20language%20kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white

[modmenu-badges-lib]: https://img.shields.io/badge/modmenu%20badges%20lib-434956?style=for-the-badge

[fstats]: https://img.shields.io/badge/fStats-111111?style=for-the-badge

[discord-shield]: https://img.shields.io/discord/1032138561618726952?logo=discord&logoColor=white&style=for-the-badge&label=Discord

[discord-url]: https://discord.gg/pbwnMwnUD6

[modrinth-shield]: https://img.shields.io/modrinth/v/ducky-updater-rework?label=Modrinth&style=for-the-badge

[modrinth-url]: https://modrinth.com/mod/ducky-updater-rework
