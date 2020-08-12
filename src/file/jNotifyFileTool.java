package file;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.*;



public class jNotifyFileTool implements  JNotifyListener {
	private static final String SOURCE_DIR =ConfigMapUtil.getValueByKey("source.dir");
	private static final String DEST_DIR =ConfigMapUtil.getValueByKey("dest.dir");
	private static final String FTP_IP =ConfigMapUtil.getValueByKey("ftp.ip");
	private static final String FTP_PORT =ConfigMapUtil.getValueByKey("ftp.port");
	private static final String FTP_USER =ConfigMapUtil.getValueByKey("ftp.user");
	private static final String FTP_PWD =ConfigMapUtil.getValueByKey("ftp.pwd");
	/*
	 * private static final String REMOTE_IP
	 * =ConfigMapUtil.getValueByKey("remote.ip"); private static final String
	 * REMOTE_PWD =ConfigMapUtil.getValueByKey("remote.pwd"); private static final
	 * String REMOTE_USER =ConfigMapUtil.getValueByKey("remote.user"); private
	 * static final String REMOTE_PORT =ConfigMapUtil.getValueByKey("remote.port");
	 */
	private static final String IS_LOCAL =ConfigMapUtil.getValueByKey("isLocal");
	private static final String IS_SOURCE_DEL =ConfigMapUtil.getValueByKey("source.delete");
	private static final String EXCLUDE_DIR =ConfigMapUtil.getValueByKey("source.dir.exclude");
	private static final String OS = System.getProperty("os.name").toLowerCase(); 
	
	static FTPClient ftpClient;
	
	
	//public static String sourceFtpFileName="";
	//public static String sourceFtpFileName2="";
	/** 监控目录*/
	String localPath = SOURCE_DIR;
	/** 监控事件 */
	int mask = JNotify.FILE_CREATED| JNotify.FILE_DELETED| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;

	/** 是否监控子目录*/
	boolean watchSubtree = true;
	/**监听器Id */
	public int watchID;
	
	static{
		//本地复制和源删除
		if(IS_SOURCE_DEL.equals("0")&&IS_LOCAL.equals("0")) {
			 List<File> list = getFileSort(SOURCE_DIR);
		        for (File file : list) {
		        	startFirstLocal(file.getParent(),file.getPath());
		        }
		}if(IS_LOCAL.equals("1")) {
				List<File> list = getFileSort(SOURCE_DIR);
		        for (File file : list) {
		        	//String getParent=file.getParent().replace("\\", "/");
		        	//String getPath=file.getPath().replace("\\", "/");
		        	ftpRemote(file.getParent(),file.getPath(),0);
		        }	
			}

       
	 }

 
	public static void main(String[] args) {
		new jNotifyFileTool().beginWatch();
	}
 
	/**
	 * 启动监听
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
			while(true) {// jnotify监控线程为守护线程，因此主线程不能终止，否则会导致进程退出
				Thread.sleep(5000);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
    public static String lastsub(String str) {
    	int i=str.lastIndexOf("/");
    	return str.substring(0, i);
    }
    public static boolean isWindows(){  
        return OS.indexOf("windows")>=0;  
    } 
    /**
     * 命令行操作结果
     * @param cmd
     * @return
     */
    public static String getLsof(String[] cmd) {
    	try {
            Process ps = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println("lsof_result:"+result);
            return result;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
           return null;
    }

	/**
	 * 文件占用判断
	 * @param filepath
	 * @return
	 */
	public static  boolean isOccupied(String filePath) {
		try {
		    File file = new File(filePath);
		    if(file.renameTo(file)) {
		    	return false;
		    }else {
		    	return true;
		    }
			
		} catch (Exception e) {
			return true;
		}	
	}
	/**
	 * 拷贝
	 * @param fileName
	 * @param pathRoot
	 */
	public static void  copy(String fileName,String pathRoot ) {
		String sourceFile=pathRoot+"/"+fileName;
		String destFile=DEST_DIR+"/"+fileName;
		System.out.println("source file----"+sourceFile);
		System.out.println("back file------"+destFile);
		File file1 = new File(sourceFile);
	    File file2 = new File(destFile);
	    try {
	    	String childPath=DEST_DIR+"/"+lastsub(fileName);
	    	System.out.println("childPath----"+childPath);
	    	File filePath=new File(childPath);
	    	if  (!filePath .exists()  && !filePath .isDirectory())      
	    	{       
	    	    filePath .mkdirs();    
/*	    	    String mkdir="mkdir -p "+childPath;
	    	    ConfigMapUtil.runShell(mkdir);*/
	    	    System.out.println("创建目录----"+childPath);
	    	}

	    	boolean isWindows=isWindows();
		    if(isWindows==true) {
	        	while(true) {
	        		boolean flag=isOccupied(sourceFile);
	        		if(flag==false) {
	        			JcopyFile(file1, file2);
	        			if(IS_SOURCE_DEL.equals("0")) {
	        				String cmd="cmd /c "+"del "+sourceFile;
	        				try{
	        			        Runtime.getRuntime().exec(cmd);
	        			    }catch(IOException e){
	        			        e.printStackTrace();
	        			    }
	        				
	        			}
						System.out.println("拷贝source file----"+sourceFile);
						System.out.println("拷贝back file------"+destFile);
						break;
	        		}else {
		    			System.out.println(sourceFile+"---被占用继续等待---");
		    			Thread.sleep(1000);
		    		}
	        	}
		    }else {
		    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
		    	while (true) {
		    		String resturl=getLsof(cmd);
		    		if(resturl == null || resturl.length() <= 0) {
						JcopyFile(file1, file2);
						if(IS_SOURCE_DEL.equals("0")) {
							String del="rm -rf "+sourceFile;
		                    ConfigMapUtil.runShell(del);
		                    System.out.println("删除源目录文件-----"+del);
						}
						System.out.println("拷贝source file----"+sourceFile);
						System.out.println("拷贝back file------"+destFile);
						break;
		    		}else {
		    			System.out.println(sourceFile+"---被占用继续等待---");
		    			Thread.sleep(1000);
		    		}
		    		
		    	}
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * ftp传输
	 * @param parentPath
	 * @param path
	 * @param retries
	 */
	 public static void ftpRemote(String parentPath,String localPath , int retries){
		 if(retries > 3) {
			 System.out.println("连接失败：重试次数3次！");
			 return;
		 }
		 if(ftpClient == null) {
			 ftpClient = new FTPClient();
		 }
		 ftpClient.setControlEncoding("utf-8");
		 String getParent=parentPath.replace("\\", "/");
		 String childPath=getParent.substring(SOURCE_DIR.length());
		 System.out.println(childPath+"ftp创建的目录");
		 try {
			 if(!ftpClient.isConnected()) {
				 System.out.println("connecting...ftp服务器:"+FTP_IP+":"+FTP_PORT); 
				 ftpClient.connect(FTP_IP, Integer.valueOf(FTP_PORT).intValue()); //连接ftp服务器
				 ftpClient.login(FTP_USER, FTP_PWD); //登录ftp服务器
				 ftpClient.setKeepAlive(true);
				 int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
	             if(!FTPReply.isPositiveCompletion(replyCode)){
	                 System.out.println("connect failed...ftp服务器:"+FTP_IP+":"+FTP_PORT); 
	             }
	             ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	 			//将客户端设置为被动模式
	             ftpClient.enterLocalPassiveMode();
			 }
			 int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
             if(!FTPReply.isPositiveCompletion(replyCode)){
                 System.out.println("connect failed...ftp服务器:"+FTP_IP+":"+FTP_PORT); 
             }
             if(!ftpClient.changeWorkingDirectory("/")) {
            	 System.out.println("切换根目录失败根目录："+ftpClient.printWorkingDirectory());
             }
             System.out.println("根目录："+ftpClient.printWorkingDirectory());
             //List<String> list = getPathList(childPath);
             String[] list = getPathListSz(childPath);
             //ftp.changeWorkingDirectory("/test");
 			for(int i=0; i<list.length; i++){
 				if(!ftpClient.changeWorkingDirectory(list[i])){//若路径未存在则创建路径
 					if(!ftpClient.makeDirectory(list[i])){//若路径创建失败则不再继续处理
 						System.out.println("create dir fail --> " + list[i]);
 						return;
 					}
 					ftpClient.changeWorkingDirectory(list[i]);
 				}
 			}
	    	 System.out.println("当前目录:"+ftpClient.printWorkingDirectory());
		     boolean isWindows=isWindows();
			 if(isWindows==true) {
				String repath=localPath.replace("/", "\\");
 				String cmd="cmd /c "+"del "+repath;
		         while(true) {
		        	boolean flag=isOccupied(localPath);
		        	if(flag==false) {
		   		      File file = new File(localPath);
				      FileInputStream fis = new FileInputStream(file);
		   		      ftpClient.storeFile(file.getName(), fis);
		   		      fis.close();
				      System.out.println(file.getName()+"上传成功");	
		 			  try{
		 			   Runtime.getRuntime().exec(cmd);
		 			  System.out.println("删除源文件-----"+cmd);
		 			  }catch(IOException e){
		 			        e.printStackTrace();
		 			  }
				      break;
		        	}else {
		    			System.out.println(localPath+"---被占用继续等待---");
		    			Thread.sleep(1000);
		        	}
			    }
			 }
			 else {
				 String del="rm -rf "+localPath;
				 String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+localPath };
				 while (true) {
		        		String resturl=getLsof(cmd);
		        		if(resturl == null || resturl.length() <= 0) {
		        			File file = new File(localPath);
		    		        FileInputStream fis = new FileInputStream(file);
				   		    ftpClient.storeFile(file.getName(), fis);
				   		    fis.close();
				   		    System.out.println(file.getName()+"上传成功");	
				   		    if(IS_SOURCE_DEL.equals("0")){
				   		    ConfigMapUtil.runShell(del);
				            System.out.println("删除源文件-----"+del);		       		          
				   		    }			                  						      
				   		    break;
		        		}else {
		        			System.out.println(localPath+"---被占用继续等待---");
		        			Thread.sleep(1000);
		        		}
			        }
			 }
	         //ftpClient.logout();
			 //System.out.println(ftpClient.changeWorkingDirectory("../"));	     
		 }catch(Exception e) {
			 e.printStackTrace();
			 System.out.println("ftp重新连接。。。。。。第"+retries+"次");
			 ftpClient = new FTPClient();
			 ftpRemote(parentPath, childPath, ++retries);
		 } 
	 }
	
	public static void startFirstLocal(String parentPath,String localPath ){  
		String sourceFile=SOURCE_DIR+localPath.substring(SOURCE_DIR.length());
		String destFile=DEST_DIR+localPath.substring(SOURCE_DIR.length());
        String bakChildPath=DEST_DIR+parentPath.substring(SOURCE_DIR.length());
		File file1 = new File(sourceFile);
	    File file2 = new File(destFile);
        try {
        	File filePath=new File(bakChildPath);
	    	if (!filePath .exists() && !filePath .isDirectory())      
	    	{       
	    	    filePath .mkdirs();    
	    	}
	    	boolean isWindows=isWindows();
	    if(isWindows==true) {
        	while(true) {
        		boolean flag=isOccupied(sourceFile);
        		if(flag==false) {
        			JcopyFile(file1, file2);
					System.out.println("拷贝source file----"+sourceFile);
					System.out.println("拷贝back file------"+destFile);
					break;
        		}else {
	    			System.out.println(sourceFile+"---被占用继续等待---");
	    			Thread.sleep(1000);
	    		}
        	}
	    }else {
	    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
	    	while (true) {
	    		String resturl=getLsof(cmd);
	    		if(resturl == null || resturl.length() <= 0) {
					JcopyFile(file1, file2);
					System.out.println("拷贝source file----"+sourceFile);
					System.out.println("拷贝back file------"+destFile);
					break;
	    		}else {
	    			System.out.println(sourceFile+"---被占用继续等待---");
	    			Thread.sleep(2000);
	    		}
	    		
	    	}
	    }
        	
        }catch(Exception e) {
           e.printStackTrace();
        }
        
        
	}
	
	 public static void JcopyFile(File sourceFile,File targetFile){  
		 if(sourceFile.isDirectory()) {
			 return;
		 }

         FileInputStream input;
         FileOutputStream output;
         BufferedInputStream inBuff;
         BufferedOutputStream outBuff;
		try {
			input = new FileInputStream(sourceFile);
            inBuff=new BufferedInputStream(input);  
			output = new FileOutputStream(targetFile);
			outBuff=new BufferedOutputStream(output);  
         byte[] b = new byte[1024 * 5];  
         int len;  
         while ((len =inBuff.read(b)) != -1) {  
             outBuff.write(b, 0, len);  
         }  
         outBuff.flush();  
         inBuff.close();  
         outBuff.close();  
         output.close();  
         input.close(); 
/*         String cmd="del "+sourceFile.getPath();
         exceCmd(cmd);*/
         
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
			
		} 
}
  
		//分割路径
   public static List<String> getPathList(String path){
			String[] dirs = path.split("/");
			List<String> list = new ArrayList<>();
			String pathname = "";
			for(String str : dirs){
				if(str==null||str.equals("")){
					continue;
				}
				pathname = pathname + "/" + str;
				list.add(pathname);
			}
			return list;
		} 
	//分割路径返回数组
	public static String[] getPathListSz(String path){
		String[] dirs = path.split("/");
		if (dirs.length > 0 && dirs[0].isEmpty()) {
			return Arrays.copyOfRange(dirs, 1, dirs.length);
		}
		return dirs;
	}
	
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
	/**
	 * 监听文件创建方法重写
	 */
	@Override
	public void fileCreated(int wd, String rootPath, String Filename) {
		String filenamecs=Filename.replace("\\", "/");
		if(!Filename.endsWith(".jpg")) {
			return;
		}
		File file=new File(rootPath+"/"+filenamecs);
		String parentPath=lastsub(rootPath+"/"+filenamecs);
		String path=rootPath+"/"+filenamecs;
		if(file.isDirectory()||Filename.contains(EXCLUDE_DIR)) {
			System.out.println("空文件夹或是包含排除目录"+EXCLUDE_DIR);
			return ;
		}
		if(IS_LOCAL.equals("0")) {
			copy(filenamecs,rootPath);
		}
		if(IS_LOCAL.equals("1")) {
			ftpRemote(parentPath,path,0);
		    System.out.println(parentPath+"-------远程传的参数--------"+path);
		}
		System.out.println("fileCreated--rootPath " + rootPath);
		System.out.println("fileCreated--name "  + Filename);
	}

	@Override
	public void fileDeleted(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileModified(int arg0, String rootPath, String name) {
		// TODO Auto-generated method stub

		
		//System.out.println("fileModified, the modified file path is " + rootPath + "/" + name);
        
		//copyfile(sourceFile, tagetFile);
		//renamePic(name,sourePath);
		//System.out.println("????·????"+sourceFile);
		//System.out.println("?????????, ????λ????? " +name);	
	}

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
		String filenamecs=newName.replace("\\", "/");
		if(!newName.endsWith(".jpg")) {
			return;
		}
		File file=new File(rootPath+"/"+filenamecs);
		String parentPath=lastsub(rootPath+"/"+filenamecs);
		String path=rootPath+"/"+filenamecs;
		if(file.isDirectory()||newName.contains(EXCLUDE_DIR)) {
			System.out.println("空文件夹或是包含排除目录"+EXCLUDE_DIR);
			return ;
		}
		if(IS_LOCAL.equals("0")) {
			copy(filenamecs,rootPath);
		}
		if(IS_LOCAL.equals("1")) {
			ftpRemote(parentPath,path,0);
		    System.out.println(parentPath+"-------远程传的参数--------"+path);
		}
		System.out.println("fileCreated--rootPath " + rootPath);
		System.out.println("fileCreated--name "  + newName);
	}
}