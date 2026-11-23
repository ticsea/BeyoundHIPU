[中文](./README.md) | English

<p align="center">
    <img src="./src/main/resources/logo.png" alt="logo" width="200" title="None">
</p>
<hr>
<p align="center">A Minecraft mod for moving container items to a dimensional network.</p>

<p align="center">
    <a href="https://modrinth.com/mod/cKx75yvx">
        <img src="https://img.shields.io/modrinth/dt/cKx75yvx?label=Modrinth&logo=Modrinth&style=flat-square" alt="Modrinth Downloads">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/1793969">
        <img src="https://img.shields.io/curseforge/dt/1793969?style=flat-square&logo=curseforge&label=CurseForge" alt="CurseForge Downloads">
    </a>
</p>

#### 🔹 About
A lazy-friendly mod. Press the button to transfer all items inside containers into a cross-dimensional network.

#### 🔹 Features
- **Manual Transfer Button**: Press the button to send all items from the container (player inventory excluded) into the dimensional network.
- **Auto Transfer**: Enable auto mode, items will be moved to the network immediately once you open the container.
- **Whitelist**: Container whitelist to control button visibility and whether the transfer function works on target containers.

#### 🔹 Debug & Commands
- `/beyondhipu print`: Enable debug mode. Print the registered container ID to chat when opening any container.
- `/beyondhipu add <container-id>`: Add container ID to whitelist (Tab completion for all vanilla containers).
- `/beyondhipu remove <container-id>`: Remove container ID from the whitelist.
- `/beyondhipu auto`: Toggle auto mode. Automatically transfer items on opening container without clicking button.
- `/beyondhipu useVisualGrid <true/false>`: Use slot rectangle bounds to separate container content from player inventory, preventing accidentally moving your inventory items into the network.

#### 🔹 Credits
This mod is built upon beyond_storage_addon:
https://github.com/wwwqqq001/beyond_storage_addon
