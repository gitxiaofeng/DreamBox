## 命令行工具使用说明

### 安装

详见[环境准备](environment.md)

### 使用

> 假设你的DSL命名为`demo.xml`

#### 0. 帮助

```
dmb-cli -h
```
具体参数细节以上述命令的输出为准。

#### 1. 用于调试

```
dmb-cli demo.xml
```
如果需要在终端中展示详细的信息可以加参数`--verbose`，此时，命令行默认认为是在调试状态下：
- 会发布本地的http server告知相关调试信息（包含编译码及中间态的Json码）
- 会发布本地websocket server，并将二维码发布在上一项提供的html中，开发者可以通过Playground App或调试入口扫码绑定手机上的DBView
- 会监听`demo.xml`，当内容发生变化时重新编译并在Playground App或你的接入App中使DBView效果实时更新

#### 2. 用于发布

```
dmb-cli demo.xml --release
```

在终端中会直接产出`编译码`，默认的会进行混淆和强制检查

- `nocheck` 可以强制关闭检查
- `noproguard` 可以强制关闭混淆