package file;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.contentobjects.jnotify.JNotify;

import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.JNotifyException;



public class JNotifyAdapter implements  JNotifyListener {
	private static final String REQUEST_BASE_PATH =ConfigMapUtil.getValueByKey("base.path");
	private static final String TARGET_BASE_PATH =ConfigMapUtil.getValueByKey("target.base.path");
	/** 被监视的目录 */
	String path = REQUEST_BASE_PATH;
	/** 关注目录的事件 */
	int mask = JNotify.FILE_CREATED ;
	/** 是否监视子目录，即级联监视 */
	boolean watchSubtree = true;
	/** 监听程序Id */
	public int watchID;

 
	public static void main(String[] args) {
		new JNotifyAdapter().beginWatch();
	}
 
	/**
	 * 容器启动时启动监视程序
	 * 
	 * @return
	 */
	public void beginWatch() {
		/** 添加到监视队列中 */
		try {
			this.watchID = JNotify.addWatch(REQUEST_BASE_PATH, mask, watchSubtree, (JNotifyListener) this);
			System.err.println("jnotify -----------启动成功-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
		// 死循环，线程一直执行，休眠一分钟后继续执行，主要是为了让主线程一直执行
		// 休眠时间和监测文件发生的效率无关（就是说不是监视目录文件改变一分钟后才监测到，监测几乎是实时的，调用本地系统库）
		while (true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {// ignore it
			}
		}
	}
	
	public void copyfile(String sourceFile,String targetFile){
	    String cmd = "cmd /c echo F|xcopy "+sourceFile+" "+targetFile+" /D/Y";
	    System.err.print("拷贝命令："+cmd);
	    try{
	        Runtime.getRuntime().exec(cmd);
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	}
	
	public String subpath(String str){
		if(str.split("\\\\").length>2){
			//这里是获取"\\"符号的位置 
	        Matcher slashMatcher = Pattern.compile("\\\\").matcher(str);
	        int mIdx = 0;
			while(slashMatcher.find()) {
			    mIdx++;
			    //当"\\"符号第三次出现的位置  
			    if(mIdx == 2){
			        break;
			    }
			}
	        return str.substring(slashMatcher.start());
		}
		return str;
	}



 
	/**
	 * 当监听目录下一旦有新的文件被创建，则即触发该事件
	 * 
	 * @param wd
	 *            监听线程id
	 * @param rootPath
	 *            监听目录
	 * @param name
	 *            文件名称
	 */
	@Override
	public void fileCreated(int wd, String rootPath, String name) {
		String sourePath= rootPath.replaceAll("/", "\\");
		String sourceFile= sourePath + "\\" + name;
		String tagetFile=TARGET_BASE_PATH+subpath(sourceFile);
		copyfile(sourceFile, tagetFile);
		System.out.println("源文件路径："+sourceFile);
		System.out.println("目标文件路径："+tagetFile);
		System.err.println("文件被创建, 创建位置为： " + sourePath + "\\" + name);
		

	}

	@Override
	public void fileDeleted(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileModified(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileRenamed(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}



}
