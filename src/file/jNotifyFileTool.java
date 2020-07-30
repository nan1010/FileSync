package file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import net.contentobjects.jnotify.JNotify;

import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.JNotifyException;



public class jNotifyFileTool implements  JNotifyListener {
	private static final String REQUEST_BASE_PATH =ConfigMapUtil.getValueByKey("base.path");
	private static final String BASE_BAK_PATH =ConfigMapUtil.getValueByKey("base.bakpath");
	private static final String TARGET_BASE_PATH =ConfigMapUtil.getValueByKey("target.base.path");
	private static final String FTP_USER =ConfigMapUtil.getValueByKey("ftp.user");
	private static final String FTP_PWD =ConfigMapUtil.getValueByKey("ftp.pwd");
	private static final String FTP_IP =ConfigMapUtil.getValueByKey("ftp.ip");
	private static final String FTP_PORT =ConfigMapUtil.getValueByKey("ftp.port");
	private static final String REMOTE_IP =ConfigMapUtil.getValueByKey("remote.ip");
	private static final String REMOTE_PWD =ConfigMapUtil.getValueByKey("remote.pwd");
	private static final String REMOTE_USER =ConfigMapUtil.getValueByKey("remote.user");
	private static final String REMOTE_PORT =ConfigMapUtil.getValueByKey("remote.port");
	private static final String IS_LOCAL =ConfigMapUtil.getValueByKey("islocal");
	private static final String IS_SOURCE_DEL =ConfigMapUtil.getValueByKey("issourcedel");
	private static final String EXCELUDE_FOLDER =ConfigMapUtil.getValueByKey("exclude.folder");
	private static final String OS = System.getProperty("os.name").toLowerCase();  
	
	
	//public static String sourceFtpFileName="";
	//public static String sourceFtpFileName2="";
	/** 监控目录*/
	String path = REQUEST_BASE_PATH;
	/** ��עĿ¼���¼� */
	int mask = JNotify.FILE_CREATED| JNotify.FILE_DELETED| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
 ;
	/** 是否监控子目录*/
	boolean watchSubtree = true;
	/** ��������Id */
	public int watchID;
	
	static{
		boolean isWindows=isWindows();
		//本地复制和源删除
		if(IS_SOURCE_DEL.equals("1")&&IS_LOCAL.equals("0")) {
			 List<File> list = getFileSort(REQUEST_BASE_PATH);
		        for (File file : list) {
		        	startFirstLocal(file.getParent(),file.getPath());
		        }
		}if(IS_LOCAL.equals("1")) {
			if(isWindows==true) {
				List<File> list = getFileSort(REQUEST_BASE_PATH);
		        for (File file : list) {
		        	startFirstWinRemote(file.getParent(),file.getPath());
		        }	
			}else {
				List<File> list = getFileSort(REQUEST_BASE_PATH);
		        for (File file : list) {
		        	startFirstLinuxRemote(file.getParent(),file.getPath());
		        }
			}
		}
       
	 }

 
	public static void main(String[] args) {
		new jNotifyFileTool().beginWatch();
	}
 
	/**
	 * ��������ʱ�������ӳ���
	 * 
	 * @return
	 */
	public void beginWatch() {
		/** ��ӵ����Ӷ����� */
		try {
			this.watchID = JNotify.addWatch(REQUEST_BASE_PATH, mask, watchSubtree, (JNotifyListener) this);
			System.err.println("jnotify -----------已启动-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {// ignore it
			}
		}
	}
    public static String lastsub(String str) {
    	int i=str.lastIndexOf("/");
    	return str.substring(0, i);
    }
    public static boolean isWindows(){  
        return OS.indexOf("windows")>=0;  
    } 
    
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
    
    public static  boolean iszhanyong(String filepath) {
		try {
		    File file = new File(filepath);
		    if(file.renameTo(file)) {
		    	return false;
		    }else {
		    	return true;
		    }
			
		} catch (Exception e) {
			return true;
		}
		
	}
    
    
	
	public static void  copyBak(String fileName,String pathRoot ) {
		String sourceFile=pathRoot+"/"+fileName;
		String bakFile=BASE_BAK_PATH+"/"+fileName;
		System.err.println("source file----"+sourceFile);
		System.err.println("back file------"+bakFile);
		File file1 = new File(sourceFile);
	    File file2 = new File(bakFile);
	    try {
	    	String childPath=BASE_BAK_PATH+"/"+lastsub(fileName);
	    	System.err.println("childPath----"+childPath);
	    	File filePath=new File(childPath);
	    	if  (!filePath .exists()  && !filePath .isDirectory())      
	    	{       
	    	    filePath .mkdirs();    
/*	    	    String mkdir="mkdir -p "+childPath;
	    	    ConfigMapUtil.runShell(mkdir);*/
	    	    System.err.println("创建目录----"+childPath);
	    	}

	    	boolean isWindows=isWindows();
		    if(isWindows==true) {
	        	boolean flag=iszhanyong(sourceFile);
	        	while(true) {
	        		if(flag==false) {
	        			JcopyFile(file1, file2);
	        			if(IS_SOURCE_DEL.equals("1")) {
	        				String cmd="cmd /c "+"del "+sourceFile;
	        				try{
	        			        Runtime.getRuntime().exec(cmd);
	        			    }catch(IOException e){
	        			        e.printStackTrace();
	        			    }
	        				
	        			}
						System.err.println("拷贝source file----"+sourceFile);
						System.err.println("拷贝back file------"+bakFile);
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
						if(IS_SOURCE_DEL.equals("1")) {
							String del="rm -rf "+sourceFile;
		                    ConfigMapUtil.runShell(del);
		                    System.err.println("删除源目录文件-----"+del);
						}
						System.err.println("拷贝source file----"+sourceFile);
						System.err.println("拷贝back file------"+bakFile);
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
	
	public static void copyRemote(String fileName){  
		String sourceFile=REQUEST_BASE_PATH+"/"+fileName;
		String remoteFile=TARGET_BASE_PATH+"/"+fileName;
        String childPath=lastsub(fileName);
        String tagteChildPath=TARGET_BASE_PATH+"/"+childPath;
        String mkdir="sshpass -p "+REMOTE_PWD+" ssh "+"-p "+REMOTE_PORT+" "+REMOTE_USER+"@"+REMOTE_IP+" [ -d "+tagteChildPath+" ]"
        		+" || mkdir -p "+tagteChildPath;
		StringBuffer buf=new StringBuffer();
        buf.append("sshpass -p ").append(REMOTE_PWD).append(" ").append("scp ").append("-P ").append(REMOTE_PORT).append(" ")
        .append(sourceFile).append(" ").append(REMOTE_USER).append("@").append(REMOTE_IP)
        .append(":").append(remoteFile);
		String del="rm -rf "+sourceFile;
        try {
			ConfigMapUtil.runShell(mkdir);
			System.err.println("创建子目录-----"+mkdir);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
    	try {
    		while (true) {
        		String resturl=getLsof(cmd);
        		if(resturl == null || resturl.length() <= 0) {
                    ConfigMapUtil.runShell(buf.toString());
                    System.err.println("远程拷贝----"+buf.toString());
                    ConfigMapUtil.runShell(del);
                    System.err.println("删除源文件-----"+del);
    				break;
        		}else {
        			System.out.println("文件被占用");
        			Thread.sleep(2000);
        		}
        		
        	}
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
	}
	
	 
	 public static void startFirstWinRemote(String parentPath,String path ){  
		 FTPClient ftpClient = new FTPClient();
		 ftpClient.setControlEncoding("utf-8");
		 String childPath=parentPath.substring(REQUEST_BASE_PATH.length());
		 System.out.println(childPath+"ftp创建的目录");
		 try {
			 System.out.println("connecting...ftp服务器:"+FTP_IP+":"+FTP_PORT); 
             ftpClient.connect(FTP_IP, Integer.valueOf(FTP_PORT).intValue()); //连接ftp服务器
             ftpClient.login(FTP_USER, FTP_PWD); //登录ftp服务器
             int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
             if(!FTPReply.isPositiveCompletion(replyCode)){
                 System.out.println("connect failed...ftp服务器:"+FTP_IP+":"+FTP_PORT); 
             }
		     FTPFile[] ftpFileArr = ftpClient.listFiles(childPath);
		     if(ftpFileArr.length<=0) {
		    	 ftpClient.makeDirectory(childPath);
		     }
	    	 ftpClient.changeWorkingDirectory(childPath);
		     File file = new File(path);
		     FileInputStream fis = new FileInputStream(file);
		     ftpClient.storeFile(file.getName(), fis);
		     System.out.println(file.getName()+"上传成功");
	         fis.close();
	         ftpClient.logout();
		     
		 }catch(Exception e) {
			 e.printStackTrace();
		 } 
	 }
	 
	 public static void startFirstLinuxRemote(String parentPath,String path ){  
			String sourceFile=REQUEST_BASE_PATH+path.substring(REQUEST_BASE_PATH.length());
			String remoteFile=TARGET_BASE_PATH+path.substring(REQUEST_BASE_PATH.length());
	        String tagteChildPath=TARGET_BASE_PATH+parentPath.substring(REQUEST_BASE_PATH.length());
	        String mkdir="sshpass -p "+REMOTE_PWD+" ssh "+"-p "+REMOTE_PORT+" "+REMOTE_USER+"@"+REMOTE_IP+" [ -d "+tagteChildPath+" ]"
	        		+" || mkdir -p "+tagteChildPath;
			StringBuffer buf=new StringBuffer();
	        buf.append("sshpass -p ").append(REMOTE_PWD).append(" ").append("scp ").append("-P ").append(REMOTE_PORT).append(" ")
	        .append(sourceFile).append(" ").append(REMOTE_USER).append("@").append(REMOTE_IP)
	        .append(":").append(remoteFile);
			String del="rm -rf "+sourceFile;
	        try {
				ConfigMapUtil.runShell(mkdir);
				System.err.println("Linux远程创建目录：-----"+mkdir);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
	        String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
	    	try {
	    		while (true) {
	        		String resturl=getLsof(cmd);
	        		if(resturl == null || resturl.length() <= 0) {
	                    ConfigMapUtil.runShell(buf.toString());
	                    System.err.println("远程上传-----"+buf.toString());
	                    ConfigMapUtil.runShell(del);
	                    System.err.println("删除bak目录文件-----"+del);
	    				break;
	        		}else {
	        			System.out.println("--文件被占用--");
	        			Thread.sleep(2000);
	        		}
	        		
	        	}
	        } catch (Exception e) {
	        	System.err.println(e.getMessage());
	        }
		}
    
	
	public static void startFirstLocal(String parentPath,String path ){  
		String sourceFile=REQUEST_BASE_PATH+path.substring(REQUEST_BASE_PATH.length());
		String bakFile=BASE_BAK_PATH+path.substring(REQUEST_BASE_PATH.length());
        String bakChildPath=BASE_BAK_PATH+parentPath.substring(REQUEST_BASE_PATH.length());
		File file1 = new File(sourceFile);
	    File file2 = new File(bakFile);
        try {
        	File filePath=new File(bakChildPath);
	    	if (!filePath .exists() && !filePath .isDirectory())      
	    	{       
	    	    filePath .mkdirs();    
	    	}
	    	boolean isWindows=isWindows();
	    if(isWindows==true) {
        	boolean flag=iszhanyong(sourceFile);
        	while(true) {
        		if(flag==false) {
        			JcopyFile(file1, file2);
					System.err.println("拷贝source file----"+sourceFile);
					System.err.println("拷贝back file------"+bakFile);
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
					System.err.println("拷贝source file----"+sourceFile);
					System.err.println("拷贝back file------"+bakFile);
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
         // �½��ļ����������������л���   
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
         // �½��ļ���������������л���   
         
			output = new FileOutputStream(targetFile);
			outBuff=new BufferedOutputStream(output);  
		  
         // ��������   
         byte[] b = new byte[1024 * 5];  
         int len;  
         while ((len =inBuff.read(b)) != -1) {  
             outBuff.write(b, 0, len);  
         }  
         // ˢ�´˻���������   
         outBuff.flush();  
           
         //�ر���   
         inBuff.close();  
         outBuff.close();  
         output.close();  
         input.close(); 
/*         String cmd="del "+sourceFile.getPath();
         exceCmd(cmd);*/
         
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println(e.getMessage());
			
		} 
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
	 * ������Ŀ¼��һ�����µ��ļ����������򼴴������¼�
	 * 
	 * @param wd
	 *            �����߳�id
	 * @param rootPath
	 *            ����Ŀ¼
	 * @param name
	 *            �ļ�����
	 */
	@Override
	public void fileCreated(int wd, String rootPath, String Filename) {
		String filenamecs=Filename.replace("\\", "/");
		File file=new File(rootPath+"/"+filenamecs);
		String parentPath=lastsub(rootPath+"/"+filenamecs);
		String path=rootPath+"/"+filenamecs;
		if(file.isDirectory()||Filename.contains(EXCELUDE_FOLDER)) {
			System.out.println("空文件夹或是包含排除目录"+EXCELUDE_FOLDER);
			return ;
		}
		if(IS_LOCAL.equals("0")) {
			copyBak(filenamecs,rootPath);
		}
		if(IS_LOCAL.equals("1")) {
			boolean isWindows=isWindows();
		    if(isWindows==true) {
		    	startFirstWinRemote(parentPath,path);
		    	System.out.println(parentPath+"-------win远程传的参数--------"+path);
		    }else {
				copyRemote(filenamecs);
		    }
		}
		System.err.println("fileCreated--rootPath " + rootPath);
		System.err.println("fileCreated--name "  + Filename);

		

	}

	@Override
	public void fileDeleted(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileModified(int arg0, String rootPath, String name) {
		// TODO Auto-generated method stub

		
		//System.err.println("fileModified, the modified file path is " + rootPath + "/" + name);
        
		//copyfile(sourceFile, tagetFile);
		//renamePic(name,sourePath);
		//System.out.println("Դ�ļ�·����"+sourceFile);
		//System.err.println("�ļ�������, ����λ��Ϊ�� " +name);


	    

		
		
	}

	@Override
	public void fileRenamed(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}
	
	



}
