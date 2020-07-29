package file;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.contentobjects.jnotify.JNotify;

import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.JNotifyException;



public  class JNotifyAdapterOut implements  JNotifyListener {
	private static final String REQUEST_BASE_PATH =ConfigMapUtil.getValueByKey("target.base.path");
	private static final String FTP_USER =ConfigMapUtil.getValueByKey("ftp.user");
	private static final String FTP_PWD =ConfigMapUtil.getValueByKey("ftp.pwd");
	private static final String FTP_IP_PORT =ConfigMapUtil.getValueByKey("ftp.ip.port");
	private static final String WIN_SCP_PATH =ConfigMapUtil.getValueByKey("win_scp_path");
	
	/** 被监视的目录 */
	String path = REQUEST_BASE_PATH;
	/** 关注目录的事件 */
	int mask = JNotify.FILE_CREATED ;
	/** 是否监视子目录，即级联监视 */
	boolean watchSubtree = true;
	/** 监听程序Id */
	public int watchID;
	
	static{
		File file=new File(REQUEST_BASE_PATH);
		showDirFisrt(file);
		  
	 }
	

 
	public static void main(String[] args) {
		new JNotifyAdapterOut().beginWatch();
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
	
	public static void showDirFisrt(File dir) {
        if(dir.exists()){
            //抽象路径名数组，这些路径名表示此抽象路径名表示的目录中的文件和目录。
            File[] files = dir.listFiles();
            if(null!=files){
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                    	showDirFisrt(files[i]);
                    } else {
                    	uplaodfile(files[i].toString(),lastsub(subpath(files[i].toString())));
                        System.out.println(files[i].toString());
                        System.out.println(lastsub(subpath(files[i].toString())));
    
                    }
                }
            }
        }else{
            System.out.println("文件不存在！");
        }
    }
	
	public static void uplaodfile(String sourceFile,String cdPath) {
		String cmd1="cd /d "+WIN_SCP_PATH;
		StringBuffer str=new StringBuffer();
		str.append("winscp.exe /console /command \"option batch continue\" \"option confirm off\" \"open ftp://")
		.append(FTP_USER).append(":")
		.append(FTP_PWD).append("@").append(FTP_IP_PORT).append("\"").append(" ")
		.append("\"mkdir ").append(cdPath).append("\"").append(" ")
		.append("\"cd ").append(cdPath).append("\"").append(" ")
		.append("\"put").append(" ").append(sourceFile+"\"").append(" ").append("\"exit\" /log=d:\\winscp.log");
		String cmd2="del "+sourceFile;
	    String execcmd = "cmd /c "+cmd1+"&"+str+"&"+cmd2;
	    System.err.print("命令："+execcmd);
	    try{
	        Runtime.getRuntime().exec(execcmd);
	    }catch(IOException e){
	        e.printStackTrace();
	    }
		
		
	}
	public static String subpath(String str){
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
	
    public static String lastsub(String str) {
    	int i=str.lastIndexOf("\\");
    	return str.substring(0, i);
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
		uplaodfile(sourceFile,lastsub(subpath(sourceFile)));
		System.err.println("文件被创建, 创建位置为： " + rootPath + "/" + name);

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
