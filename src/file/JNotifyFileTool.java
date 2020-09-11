package file;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class JNotifyFileTool implements JNotifyListener {
	// 本地源文件路径
	String SOURCE_DIR = null;
	// 本地目标文件路径
	String DEST_DIR = null;
	// IP地址
	String FTP_IP = null;
	// 端口
	String FTP_PORT = null;
	// 用户名
	String FTP_USER = null;
	// 密码
	String FTP_PWD = null;
	// 文件后缀
	String SOURCE_FILE_SUFFIX = null;
	// 是否为本地
	boolean IS_LOCAL = true;
	// 源文件是否删除
	boolean IS_SOURCE_DEL = true;
	// 是否初始化
	boolean IS_INITIALIZE = true;
	// 同步排除目录
	String EXCLUDE_DIR = null;
	// 系统类型检测
	String OS = null;
	// 最大重试次数
	Integer MAX_RETRIES = null;
	// FTPClient对象
	FTPClient ftpClient;

//	Lock lock = new ReentrantLock();

	// 测试被占用状态
	int ii;

	// public static String sourceFtpFileName="";
	// public static String sourceFtpFileName2="";

	// 监控事件
	int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
	// 是否监控子目录
	boolean watchSubtree = true;
	// 监听器Id
	public int watchID;
	// sqlite数据库连接对象
	private static Connection conn;

	public static void main(String[] args) {
		JNotifyFileTool jNotifyFileTool = new JNotifyFileTool();
		// 初始化sqlite数据库，建立连接、创建表，用于存放被占用文件名
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:data.db");
			Statement stat = conn.createStatement();
			stat.executeUpdate("create table if not exists tbl1(rootPath varchar(80),relativePath varchar(200));");
			stat.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 初始化
		jNotifyFileTool.initialize();
		// 开启监听
		jNotifyFileTool.beginWatch();
		// 启动线程检测被占用文件
		jNotifyFileTool.startThread();
	}

	/**
	 * 启动监听
	 * 
	 * @return
	 */
	public void beginWatch() {
		try {
			String SOURCE_DIR = ConfigMapUtil.getValueByKey("source.dir");
			this.watchID = JNotify.addWatch(SOURCE_DIR, mask, watchSubtree, (JNotifyListener) this);
			System.out.println("jnotify -----------已启动-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
	}

	public void initialize() {

		SOURCE_DIR = ConfigMapUtil.getValueByKey("source.dir");
		IS_LOCAL = "0".equals(ConfigMapUtil.getValueByKey("isLocal"));
		IS_INITIALIZE = "0".equals(ConfigMapUtil.getValueByKey("isInitialize"));
		SOURCE_FILE_SUFFIX = ConfigMapUtil.getValueByKey("source.file.suffix");
		EXCLUDE_DIR = ConfigMapUtil.getValueByKey("source.dir.exclude");
		IS_SOURCE_DEL = "0".equals(ConfigMapUtil.getValueByKey("source.delete"));
		DEST_DIR = ConfigMapUtil.getValueByKey("dest.dir");

		FTP_IP = ConfigMapUtil.getValueByKey("ftp.ip");
		FTP_PORT = ConfigMapUtil.getValueByKey("ftp.port");
		FTP_USER = ConfigMapUtil.getValueByKey("ftp.user");
		FTP_PWD = ConfigMapUtil.getValueByKey("ftp.pwd");
		MAX_RETRIES = Integer.parseInt(ConfigMapUtil.getValueByKey("maxReties"));
		OS = System.getProperty("os.name").toLowerCase();

		if (IS_INITIALIZE) {
			// 本地复制和源删除
			System.out.println("----------------------------初始化同步文件 开始---");
			List<File> list = getFileSort(SOURCE_DIR);
			for (File file : list) {				
				String relativePath = file.getPath().replace("\\", "/").replace(SOURCE_DIR + "/", "");
				Boolean result = fileCopy(SOURCE_DIR, relativePath);
				if (result != null && result) {
					Statement stat = null;
					try {
						 stat = conn.createStatement();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sqliteDelete(stat, SOURCE_DIR, relativePath);
				}				
			}
			System.out.println("----------------------------初始化同步文件 结束---");
		}
	}

	/**
	 * 监听文件创建方法重写
	 * 
	 * @param wd       监听器ID
	 * @param rootPath 源文件根路径
	 * @param name     源文件相对路径
	 */
	@Override
	public void fileCreated(int wd, String rootPath, String name) {
		System.out.println("----------------------------file Created--rootPath " + rootPath + ",name=" + name);
		String reletivePath = name.replace("\\", "/");
		fileCopy(rootPath, reletivePath);
	}

	@Override
	public void fileDeleted(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileModified(int arg0, String rootPath, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
		System.out.println("----------------------------file Created--rootPath " + rootPath + ",newName=" + newName
				+ ",oldName=" + oldName);
		String reletivePath = newName.replace("\\", "/");
		fileCopy(rootPath, reletivePath);
	}

	public Boolean fileCopy(String rootPath, String relativePath) {
		// 文件后缀名检测
		if (!adaptSubffix(relativePath)) {
			return null;
		}
		String absolutePath = rootPath + "/" + relativePath;
		File file = new File(absolutePath);
		// 如果file对象是目录或者包含排除目录，则返回
		if (file.isDirectory() || relativePath.contains(EXCLUDE_DIR)) {
			System.out.println("空文件夹或是包含排除目录" + EXCLUDE_DIR);
			return null;
		}
		if (IS_LOCAL) {
			return localCopy(rootPath, relativePath);
		} else {
			return ftpRemote(rootPath, relativePath, 0);
		}
	}

	/**
	 * 本地复制
	 * 
	 * @param rootPath     检测文件夹目录
	 * @param reletivePath 要复制文件的相对路径（含文件名），分隔符为 "/"
	 */
	public boolean localCopy(String rootPath, String relativePath) {
		String absolutePath = rootPath + "/" + relativePath;
		String destFile = DEST_DIR + "/" + relativePath;
		System.out.println("source file----" + absolutePath);
		System.out.println("destination file------" + destFile);
		File file1 = new File(absolutePath);
		File file2 = new File(destFile);
		try {
			// 如果目标文件的上层目录（多层）不存在，则创建目录
			if (!file2.getParentFile().exists()) {
				file2.getParentFile().mkdirs();
				System.out.println("创建目录----" + file2.getParent());
			}
			int occupiedRetries = 0;
			while (true) {
				// 判断文件是否被占用，如果没有被占用则执行文件复制，如果被占用则在最高检测次数内持续检测
				// boolean flag = fileIsOccupied(absolutePath);
				boolean flag = true;
				ii++;
				if (ii % 3 == 0)
					flag = false;
				// 如果没有被占用，执行复制、删除操作
				if (!flag) {
					JcopyFile(file1, file2);
					if (IS_SOURCE_DEL) {
						deleteFile(absolutePath);
					}
					return true;
					// 如果文件被占用，判断重复检测被占用次数
				} else {
					// 超过规定检测次数，将文件名写入sqlite数据库中
					if (occupiedRetries >= MAX_RETRIES) {
						Statement stat = conn.createStatement();
						// 已经写进数据库的文件名不能重复插入
						ResultSet rSet = sqliteSelectAll(stat, rootPath, relativePath);
						if (rSet.next()) {
							return false;
						}
						if (sqliteInsert(stat, rootPath, relativePath)) {
							System.out.println("-----------将被占用的文件名" + absolutePath + "写入sqlite数据库中-----------");
							return false;
						}
					}
					System.out.println(absolutePath + "---被占用继续等待---");
					Thread.sleep(3000);
					occupiedRetries++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * ftp传输
	 * 
	 * @param 源文件根路径
	 * @param relativePath 相对路径
	 * @param retries      服务器重连次数
	 */
	public boolean ftpRemote(String rootPath, String relativePath, int retries) {
		String absolutePath = rootPath + "/" + relativePath;
		if (retries > MAX_RETRIES) {
			System.err.println("连接失败：重试次数超过限制！");
			Statement stat = null;
			try {
				stat = conn.createStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(sqliteInsert(stat, rootPath, relativePath)) {
				System.out.println("-----------将连接失败的文件名" + absolutePath + "写入sqlite数据库中-----------");
			}
			return false;
		} else if (retries > 0) {
			System.out.println("ftp重新连接。。。。。。第" + retries + "次");
		}
		if (ftpClient == null) {
			ftpClient = new FTPClient();
			// ftp中文编码设置
			ftpClient.setControlEncoding("UTF8");
		}
		try {
			if (!ftpClient.isConnected()) {
				System.out.println("connecting...ftp服务器:" + FTP_IP + ":" + FTP_PORT);
				ftpClient.connect(FTP_IP, Integer.valueOf(FTP_PORT).intValue()); // 连接ftp服务器
				ftpClient.login(FTP_USER, FTP_PWD); // 登录ftp服务器
				ftpClient.setKeepAlive(true);
				int replyCode = ftpClient.getReplyCode(); // 是否成功登录服务器
				if (!FTPReply.isPositiveCompletion(replyCode)) {
					System.err.println("connect failed...ftp服务器:" + FTP_IP + ":" + FTP_PORT);
					// 断开ftp连接，重新进行ftp传输
					ftpClient.disconnect();
					ftpRemote(rootPath, relativePath, ++retries);
					return false;
				}
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				// 将客户端设置为被动模式
				ftpClient.enterLocalPassiveMode();
			}
			int replyCode = ftpClient.getReplyCode(); // 是否成功登录服务器
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				// 服务器replyCode值返回异常
				System.err.println("connect failed...ftp服务器:" + FTP_IP + ":" + FTP_PORT + ",replyCode=" + replyCode);
				// 断开ftp连接，重新进行ftp传输
				ftpClient.disconnect();
				ftpRemote(rootPath, relativePath, ++retries);
				return false;
			}
			ftpClient.setControlEncoding("utf-8");
			System.out.println("相对路径：" + relativePath);
			if (!ftpClient.changeWorkingDirectory("/")) {
				System.err.println("切换根目录失败，根目录：" + ftpClient.printWorkingDirectory());
			}
			System.out.println("ftp根目录：" + ftpClient.printWorkingDirectory());
			// 分层创建目录
			String[] childPathlist = relativePath.split("/");
			for (int i = 0; i < childPathlist.length - 1; i++) {
				String dir = childPathlist[i];
				if (!ftpClient.changeWorkingDirectory(dir)) {// 判断目录是否存在
					ftpClient.makeDirectory(dir);
					// 切到到对应目录
					ftpClient.changeWorkingDirectory(dir);
				}
			}
			System.out.println("ftp当前目录:" + ftpClient.printWorkingDirectory());
			// 判断文件是否被占用，如果没有被占用则执行文件复制，如果被占用则在最高检测次数内持续检测			
			int occupiedRetries = 0;
			while (true) {
				// boolean flag = fileIsOccupied(absolutePath);
				boolean flag = true;
				ii++;
				if (ii % 3 == 0)
					flag = false;
				// 如果没有被占用，执行远程同步、删除操作
				if (!flag) {
					File file = new File(absolutePath);
					FileInputStream fis = new FileInputStream(file);
					ftpClient.storeFile(file.getName(), fis);
					fis.close();
					if (IS_SOURCE_DEL) {
						deleteFile(absolutePath);
					}
					break;
					// 如果文件被占用，判断重复检测被占用次数
				} else {
					// 超过规定检测次数，将文件名写入sqlite数据库中
					if (occupiedRetries >= MAX_RETRIES) {
						Statement stat = conn.createStatement();
						// 已经写进数据库的文件名不能重复插入
						ResultSet rSet = sqliteSelectAll(stat, rootPath, relativePath);
						if (rSet.next()) {
							return false;
						}
						if (sqliteInsert(stat, rootPath, relativePath)) {
							System.out.println("-----------将被占用的文件名" + absolutePath + "写入sqlite数据库中-----------");							
							return false;
						}
					}
					System.out.println(absolutePath + "---被占用继续等待---");
					Thread.sleep(3000);
					occupiedRetries++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ftpClient = new FTPClient();
			ftpRemote(rootPath, relativePath, ++retries);
		}
		return true;
	}

	// 启动线程的方法
	public void startThread() {
		// 每隔10分钟循环调用线程
		Thread thread = new Thread(new HandlerIfOccupied());
		thread.start();
	}

	// 线程每隔10分钟遍历sqlite数据表中被占用的文件名，试图重新复制、传输其中解除占用的文件
	class HandlerIfOccupied implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Statement stat = conn.createStatement();
					// 搜索数据库，将结果放入数据集ResultSet中
					ResultSet rSet = stat.executeQuery("select * from tbl1");
					// 遍历这个结果集
					while (rSet.next()) {
						String rootPath = rSet.getString("rootPath");
						String relativePath = rSet.getString("relativePath");
						String absolutePath = rootPath + "/" + relativePath;
						// 创建被占用文件对象
						File file = new File(absolutePath);

						// 如果不存在，则退出
						if (!file.exists()) {
							break;
						}
						if (fileCopy(rootPath, relativePath)) {
							if (sqliteDelete(stat, rootPath, relativePath)) {
								System.out.println(
										"-----------文件" + absolutePath + "已经解除占用，其文件名从sqlite数据库中删除------------");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	// 查询
	public ResultSet sqliteSelectAll(Statement stat, String rootPath, String relativePath) {
		ResultSet rSet = null;
		try {
			rSet = stat.executeQuery("select*from tbl1 where rootPath in ('" + rootPath + "') and relativePath in ('"
					+ relativePath + "');");
			stat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rSet;
	}

	// 删除
	public boolean sqliteDelete(Statement stat, String rootPath, String relativePath) {
		try {
			stat.executeUpdate("delete from tbl1 where rootPath in ('" + rootPath + "') and relativePath in ('"
					+ relativePath + "');");
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	// 插入
	public boolean sqliteInsert(Statement stat, String rootPath, String relativePath) {
		try {
			stat.executeUpdate("insert into tbl1 values('" + rootPath + "', '" + relativePath + "');");
			stat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 检测是否为Windows系统
	 * 
	 * @return
	 */
	public boolean isWindows() {
		return OS.indexOf("windows") >= 0;
	}

	/**
	 * 文件后缀检测
	 * 
	 * @param reletivePath 文件相对路径
	 * @return
	 */
	public boolean adaptSubffix(String reletivePath) {
		if (SOURCE_FILE_SUFFIX.equals(""))
			return true;
		String[] sourceFileSuffixList = SOURCE_FILE_SUFFIX.split(",");
		for (int i = 0; i < sourceFileSuffixList.length; i++) {
			if (reletivePath.endsWith("." + sourceFileSuffixList[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 文件占用判断
	 * 
	 * @param filepath 文件绝对路径
	 * @return
	 */
	public boolean fileIsOccupied(String filePath) {
		boolean flag = true;
		try {
			if (isWindows()) {
				File file = new File(filePath);
				if (file.renameTo(file)) {
					flag = false;
				}
			} else {
				String[] cmd = new String[] { "/bin/sh", "-c", " lsof  " + filePath };
				Process ps = Runtime.getRuntime().exec(cmd);
				BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				String result = sb.toString();
				if (result == null || result.length() <= 0) {
					flag = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 删除文件
	 * 
	 * @param filePath 文件绝对路径
	 */
	public void deleteFile(String filePath) {
		try {
			if (isWindows()) {
				String cmd = "cmd /c " + "del \"" + filePath.replace("/", "\\") + "\"";
				Runtime.getRuntime().exec(cmd);
				System.out.println("删除源文件-----" + cmd);
			} else {
				String cmd = "rm -f '" + filePath + "'";
				ShellUtil.runShell(cmd);
				System.out.println("删除源文件-----" + cmd);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 文件复制
	 *
	 * @param sourceFile 源文件路径
	 * @param destFile   目标文件路径
	 */
	public static void JcopyFile(File sourceFile, File destFile) throws IOException {
		// TODO transferTo
		if (sourceFile.isDirectory()) {
			return;
		}
		FileChannel inChannel = FileChannel.open(Paths.get(sourceFile.getPath()), StandardOpenOption.READ);
		FileChannel outChannel = FileChannel.open(Paths.get(destFile.getPath()), StandardOpenOption.WRITE,
				StandardOpenOption.CREATE, StandardOpenOption.READ);
		inChannel.transferTo(0, inChannel.size(), outChannel);
		outChannel.close();
		inChannel.close();
	}

	/**
	 * 文件排序：根据修改时间，保证文件配对的正确性
	 *
	 * @param path 文件路径
	 */
	public static List<File> getFileSort(String path) {
		List<File> list = getFiles(path, new ArrayList<File>());
		if (list != null && list.size() > 0) {
			Collections.sort(list, new Comparator<File>() {
				public int compare(File file, File newFile) {
					if (file.lastModified() < newFile.lastModified()) {
						return 1;
					} else if (file.lastModified() == newFile.lastModified()) {
						return 0;
					} else {
						return -1;
					}
				}
			});
		}
		return list;
	}

	/**
	 * 获取文件列表
	 *
	 * @param realpath 文件路径
	 * @param files    文件列表
	 */
	public static List<File> getFiles(String realpath, List<File> files) {
		File realFile = new File(realpath);
		if (realFile.isDirectory()) {
			File[] subfiles = realFile.listFiles();
			for (File file : subfiles) {
				if (file.isDirectory()) {
					getFiles(file.getAbsolutePath(), files);
				} else {
					files.add(file);
				}
			}
		}
		return files;
	}
}
