# SearchCarefully资源包制作指南

## 概述

SearchCarefully 模组支持通过资源包来自定义搜索遮罩、旋转动画和音效。本文档将指导您如何创建和配置这些资源。

## 资源包结构

资源包应遵循标准的 Minecraft 资源包结构：

```
resource_pack/
├── assets/
│   └── searchcarefully/
│       ├── textures/
│       │   └── gui/
│       │       ├── search_mask.png
│       │       └── rotation_animation.png
│       └── sounds.json
└── pack.mcmeta
```

## 自定义纹理

### 搜索遮罩纹理

- **文件路径**：`assets/searchcarefully/textures/gui/search_mask.png`
- **推荐尺寸**：16x16 像素
- **用途**：覆盖在待搜索物品上的遮罩
- **建议**：使用不透明纹理以完全遮挡物品

### 旋转动画纹理

- **文件路径**：`assets/searchcarefully/textures/gui/rotation_animation.png`
- **推荐尺寸**：16x16 像素
- **用途**：在遮罩上方旋转的动画纹理
- **建议**：使用带有透明背景的纹理以显示下方内容

## 自定义音效

### 音效文件

- **文件路径**：`assets/searchcarefully/sounds/`
- **文件格式**：OGG 格式
- **文件名**：`search_completion_rarity_1.ogg` 到 `search_completion_rarity_7.ogg`
- **音频格式**：**必须为单声道（Mono）格式**，立体声（Stereo）格式无法应用3D空间定位和距离衰减效果

### 音效配置

在 `assets/searchcarefully/sounds.json` 中配置音效：

```json
{
  "search_completion_rarity_1": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_1",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_1"
  },
  "search_completion_rarity_2": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_2",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_2"
  },
  "search_completion_rarity_3": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_3",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_3"
  },
  "search_completion_rarity_4": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_4",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_4"
  },
  "search_completion_rarity_5": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_5",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_5"
  },
  "search_completion_rarity_6": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_6",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_6"
  },
  "search_completion_rarity_7": {
    "sounds": [
      {
        "name": "searchcarefully:search_completion_rarity_7",
        "stream": false,
        "attenuation_distance": 16
      }
    ],
    "subtitle": "sound.searchcarefully.search_completion_rarity_7"
  }
}
```

## 音效属性说明

- **name**：音效的资源位置，格式为 `namespace:sound_name`
- **stream**：是否流式播放，对于短音效设置为 false
- **attenuation_distance**：音效衰减距离，用于3D空间定位效果，数值表示音效有效范围（方块）
- **subtitle**：字幕文本的本地化键，用于在游戏字幕中显示音效描述
- **重要提示**：音频文件必须为单声道（Mono）格式才能实现3D空间定位和距离衰减效果