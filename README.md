文件同步：
1.支持本地文件同步
2.支持远程文件同步
注意事项:windows环境运行需将jnotify依赖的dll文件复制到环境变量的path目录下，即jdk/bin、system32等目录

为测试方便：
修改了文件是否被占用 flag 值的判定方法，正式运行的时候需要改回去。
修改了被占用文件重新检测的最大次数：为1次（MAX_OCCUPIED_RETRIES = 0）。
修改了新建线程每隔时间T执行一次的时长（run()方法中的Thread.sleep()）。
