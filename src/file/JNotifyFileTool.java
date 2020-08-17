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
import java.util.*;

public class JNotifyFileTool implements JNotifyListener {

	private static final String SOURCE_DIR = ConfigMapUtil.getValueByKey("source.dir");
	private static final String DEST_DIR = ConfigMapUtil.getValueByKey("dest.dir");
	private static final String FTP_IP = ConfigMapUtil.getValueByKey("ftp.ip");
	private static final String FTP_PORT = ConfigMapUtil.getValueByKey("ftp.port");
	private static final String FTP_USER = ConfigMapUtil.getValueByKey("ftp.user");
	private static final String FTP_PWD = ConfigMapUtil.getValueByKey("ftp.pwd");
	private static final String SOURCE_FILE_SUFFIX = ConfigMapUtil.getValueByKey("source.file.suffix");

	/*
	 * private static final String REMOTE_IP
	 * =ConfigMapUtil.getValueByKey("remote.ip"); private static final String
	 * REMOTE_PWD =ConfigMapUtil.getValueByKey("remote.pwd"); private static final
	 * String REMOTE_USER =ConfigMapUtil.getValueByKey("remote.user"); private
	 * static final String REMOTE_PORT =ConfigMapUtil.getValueByKey("remote.port");
	 */
	private static final boolean IS_LOCAL = "0".equals(ConfigMapUtil.getValueByKey("isLocal"));
	private static final boolean IS_SOURCE_DEL = "0".equals(ConfigMapUtil.getValueByKey("source.delete"));
	private static final boolean IS_INITIALIZE = "0".equals(ConfigMapUtil.getValueByKey("isInitialize"));
	private static final String EXCLUDE_DIR = ConfigMapUtil.getValueByKey("source.dir.exclude");
	private static final String OS = System.getProperty("os.name").toLowerCase();

	static FTPClient ftpClient;

	// public static String sourceFtpFileName="";
	// public static String sourceFtpFileName2="";
	/** 监控目录 */
	String localPath = SOURCE_DIR;
	/** 监控事件 */
	int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;

	/** 是否监控子目录 */
	boolean watchSubtree = true;
	/** 监听器Id */
	public int watchID;
	/** 最大重连次数 */
	static int MAX_RETRIES = 3;
	/** 文件被占用的最大检测次数 */
	static int MAX_OCCUPIED_RETRIES = 30;

	static {
		if(IS_INITIALIZE) {
			// 本地复制和源删除
			if (IS_LOCAL && IS_SOURCE_DEL) {
				List<File> list = getFileSort(SOURCE_DIR);
				for (File file : list) {
					// startFirstLocal(file.getParent(), file.getPath());
					localCopy(SOURCE_DIR, file.getPath().replace("\\", "/").replace(SOURCE_DIR + "/", ""));
				}
			} else if (!IS_LOCAL) {
				List<File> list = getFileSort(SOURCE_DIR);
				for (File file : list) {
					// String getParent=file.getParent().replace("\\", "/");
					// String localPath = file.getPath().replace("\\", "/");
					ftpRemote(SOURCE_DIR, file.getPath().replace("\\", "/").replace(SOURCE_DIR + "/", ""), 0);
				}
			}
		}	
	}

	public static void main(String[] args) {
		new JNotifyFileTool().beginWatch();
	}

	/**
	 * 启动监听
	 * 
	 * @return
	 */
	public void beginWatch() {
		try {
			this.watchID = JNotify.addWatch(SOURCE_DIR, mask, watchSubtree, (JNotifyListener) this);
			System.out.println("jnotify -----------已启动-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
		try {
			while (true) {// jnotify监控线程为守护线程，因此主线程不能终止，否则会导致进程退出
				Thread.sleep(5000);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 监听文件创建方法重写
	 * 
	 * @param wd 监听器ID
	 * @param rootPath 源文件根路径
	 * @param name 源文件相对路径
	 */
	@Override
	public void fileCreated(int wd, String rootPath, String name) {
		System.out.println("file Created--rootPath " + rootPath + ",name=" + name);
		String reletivePath = name.replace("\\", "/");
		// 文件后缀名检测
		if (!adaptSubffix(reletivePath))
			return;
		String localPath = rootPath + "/" + reletivePath;
		File file = new File(localPath);
		// 如果file对象是目录或者包含排除目录，则返回
		if (file.isDirectory() || reletivePath.contains(EXCLUDE_DIR)) {
			System.out.println("空文件夹或是包含排除目录" + EXCLUDE_DIR);
			return;
		}
		if (IS_LOCAL) {
			localCopy(rootPath, reletivePath);
		} else if (!IS_LOCAL) {
			ftpRemote(rootPath, reletivePath, 0);
		}
	}

	@Override
	public void fileDeleted(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fileModified(int arg0, String rootPath, String name) {
		// TODO Auto-generated method stub

		// System.out.println("fileModified, the modified file path is " + rootPath +
		// "/" + name);

		// copyfile(sourceFile, tagetFile);
		// renamePic(name,sourePath);
		// System.out.println("????·????"+sourceFile);
		// System.out.println("?????????, ????λ????? " +name);
	}

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
		System.out.println("file Created--rootPath " + rootPath + ",newName=" + newName + ",oldName=" + oldName);
		String reletivePath = newName.replace("\\", "/");
		// 文件后缀名检测
		if (!adaptSubffix(reletivePath))
			return;
		String localPath = rootPath + "/" + reletivePath;
		File file = new File(localPath);
		// 如果file对象是目录或者包含排除目录，则返回
		if (file.isDirectory() || reletivePath.contains(EXCLUDE_DIR)) {
			System.out.println("空文件夹或是包含排除目录" + EXCLUDE_DIR);
			return;
		}
		if (IS_LOCAL) {
			localCopy(rootPath, reletivePath);
		} else {
			ftpRemote(rootPath, reletivePath, 0);
		}
	}

	/**
	 * 本地复制
	 * 
	 * @param rootPath     检测文件夹目录
	 * @param reletivePath 要复制文件的相对路径（含文件名），分隔符为 "/"
	 */
	public static void localCopy(String rootPath, String reletivePath) {
		if (!adaptSubffix(reletivePath))
			return;
		String absolutePath = rootPath + "/" + reletivePath;
		String destFile = DEST_DIR + "/" + reletivePath;
		System.out.println("source file----" + absolutePath);
		System.out.println("destination file------" + destFile);
		File file1 = new File(absolutePath);
		File file2 = new File(destFile);
		try {
			if (!file2.getParentFile().exists()) {// 如果目标文件的上层目录（多层）不存在，则创建目录
				file2.getParentFile().mkdirs();
				System.out.println("创建目录----" + file2.getParent());
			}
			int occupiedRetries = 0;
			while (true) {
				// 判断文件是否被占用，如果没有被占用则执行文件复制，如果被占用则在最高检测次数内持续检测
				boolean flag = fileIsOccupied(absolutePath);
				if (!flag) {
					JcopyFile(file1, file2);
					if (IS_SOURCE_DEL) {
						deleteFile(absolutePath);
					}
					break;
				} else {
					if (occupiedRetries > MAX_OCCUPIED_RETRIES) {
						break;
					}
					System.out.println(absolutePath + "---被占用继续等待---");
					Thread.sleep(1000);
					occupiedRetries++;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ftp传输
	 * 
	 * @param 源文件根路径 
	 * @param relativePath 相对路径
	 * @param retries 服务器重连次数
	 */
	public static void ftpRemote(String rootPath, String relativePath, int retries) {
		if (retries > MAX_RETRIES) {
			System.err.println("连接失败：重试次数3次！");
			return;
		} else if (retries > 0) {
			System.out.println("ftp重新连接。。。。。。第" + retries + "次");
		}
		if (ftpClient == null) {
			ftpClient = new FTPClient();
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
					return;
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
				return;
			}
			if (!adaptSubffix(relativePath)) {// 文件后缀检测
				return;
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
			String absolutePath = rootPath + "/" + relativePath;
			int occupiedRetries = 0;
			while (true) {
				boolean flag = fileIsOccupied(absolutePath);
				if (!flag) {
					File file = new File(absolutePath);
					FileInputStream fis = new FileInputStream(file);
					ftpClient.storeFile(file.getName(), fis);
					fis.close();
					deleteFile(absolutePath);					
					break;
				} else {
					if (occupiedRetries > MAX_OCCUPIED_RETRIES) {
						break;
					}
					System.out.println(absolutePath + "---被占用继续等待---");
					Thread.sleep(1000);
					occupiedRetries++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ftpClient = new FTPClient();
			ftpRemote(rootPath, relativePath, ++retries);
		}
	}

	/**
	 * 检测是否为Windows系统
	 * 
	 * @return
	 */
	public static boolean isWindows() {
		return OS.indexOf("windows") >= 0;
	}

	/**
	 * 文件后缀检测
	 * 
	 * @param reletivePath 文件相对路径
	 * @return
	 */
	public static boolean adaptSubffix(String reletivePath) {
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
	public static boolean fileIsOccupied(String filePath) {
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
	public static void deleteFile(String filePath) {
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
	 * @param files 文件列表
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
