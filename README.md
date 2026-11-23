[English](./README.en.md) | 中文

<p align="center">
    <img src="./src/main/resources/logo.png" alt="logo" width="200" title="None">
</p>
<hr>
<p align="center">Minecraft 跨维网络容器拾取模组</p>

<p align="center">
    <a href="https://modrinth.com/mod/cKx75yvx">
        <img src="https://img.shields.io/modrinth/dt/cKx75yvx?label=Modrinth&logo=Modrinth&style=flat-square" alt="Modrinth Downloads">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/1793969">
        <img src="https://img.shields.io/curseforge/dt/1793969?style=flat-square&logo=curseforge&label=CurseForge" alt="CurseForge Downloads">
    </a>
</p>

#### 🔹 模组介绍
一款懒人向模组，按下按钮即可将容器内全部物品存入跨维度网络。

#### 🔹 核心功能
- **按钮一键存入网络**：点击按钮，模组会把**容器内全部物品（不含玩家背包）**存入跨维网络。
- **自动存入网络**：开启自动模式后，打开容器就自动转移物品至网络。
- **白名单**：支持容器白名单，控制按钮显示、存入功能对哪些容器生效。

#### 🔹 调试与指令
- `/beyondhipu print`：开启调试模式，打开任意容器时，在聊天栏打印该容器注册ID
- `/beyondhipu add <容器ID>`：将容器ID加入白名单（Tab 自动补全原版所有容器）
- `/beyondhipu remove <容器ID>`：从白名单移除指定容器ID
- `/beyondhipu auto`：切换自动模式，开箱直接转移物品，无需手动点击按钮
- `/beyondhipu useVisualGrid <true/false>`：基于容器槽位矩形区域区分容器与玩家背包，防止误把背包物品存入网络

#### 🔹 引用与致谢
本模组基于 beyond_storage_addon 开发：
https://github.com/wwwqqq001/beyond_storage_addon
