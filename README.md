# DynamicPack
A mod that will monitor the current version of your resource pack and download automatically!

![https://img.shields.io/badge/Enviroment-Client-purple](https://img.shields.io/badge/Enviroment-Client-purple)  
[![Static Badge](https://img.shields.io/badge/Github-gray?logo=github)
](https://github.com/AdamCalculator/DynamicPack)

## Documentation
[**Available here**](https://github.com/AdamCalculator/DynamicPack/wiki)


## How it works
Resource pack developers need to create a `dynamicmcpack.json` file inside the resource pack, which will save some information, and the mod will update when the game starts if resource pack files are required.

## For users
Install and it will automatically update trendy resource packs.

⚠️ Since the mod is being actively developed, it is not yet possible to check resource packs for updates, but this will be added in the future.


## For developers
If you want your package to update itself from **Modrinth**, you need to add the `dynamicmcpack.json` file to the following content:
```json5
{
    "current": {
      "version_number": "7.1" // version of the current pack
    },
    "remote": {
      "game_version": "1.20.1", // game version
      "modrinth_project_id": "better-leaves", // your project identifier
      "type": "modrinth"
    },
    "formatVersion": 1
}
```
 
For other features (dynamic repos) visit [github wiki!](https://github.com/AdamCalculator/DynamicPack/wiki)
