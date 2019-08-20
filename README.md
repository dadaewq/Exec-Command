# Exec-Command
快速地以Root执行执行自定义的指令
例如 busybox pkill com.android.systemui (重启用户界面)等

## 首先您要清楚它没有界面，完全不能代替Termux等终端，只是执行您自定义的指令。
## 其次Root运行指令就有一定的危险性，下载使用前请悉知。

您可以通过分享纯文本或文本文件给它，来快速执行命令。浏览器的快捷菜单也可以执行选中文字。
文本文件支持多行，一行为一条命令。执行的时候会以Toast显示执行中。(只支持编码为UTF-8的文本文件)

应用第一次运行的时候内置存储的 Android/data/com.modosa.execcommand/files/CommandList.txt 生成指令文件
内置的为[exit]，以后运行的时候就是执行这个文件里的命令，您可以自行编辑它。
桌面有两个静态的shortcuts，一个是以文本分享本地指令文件的内容，一个是查看指令文件。
