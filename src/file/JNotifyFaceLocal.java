package file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;



public class JNotifyFaceLocal implements  JNotifyListener {
	private static final String REQUEST_BASE_PATH =ConfigMapUtil.getValueByKey("base.path");
	private static final String BASE_BAK_PATH =ConfigMapUtil.getValueByKey("base.bakpath");
	private static final String OS = System.getProperty("os.name").toLowerCase();  
	public static Map<String, String> sourceFtpFileName = new HashMap<String, String>();
	/**
	 * 本地原文件路径
	 */
	String path = REQUEST_BASE_PATH;
	/** 
	 *监控文件被执行的操作:创建，删除，修改，重命名 
	 */
	int mask = JNotify.FILE_CREATED| JNotify.FILE_DELETED| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
 ;
	/** 是否监控子文件夹 */
	boolean watchSubtree = true;
	/** 监听器Id */
	public int watchID;
	
	static{
        List<File> list = getFileSort(REQUEST_BASE_PATH);
        for (File file : list) {
        	boolean isWindows=isWindows();
        	if(isWindows==true) {
        		startFirstLocal(file.getParent().replace("\\", "/"),file.getName());
        	}else {
        		startFirstLocal(file.getParent(),file.getName());
        	}
        	System.err.println(file.getParent());
        	System.err.println(file.getName());
        }
	 }

 
	public static void main(String[] args) {
		new JNotifyFaceLocal().beginWatch();
	}
 
	/**
	 * 启动监听器
	 * 
	 * @return
	 */
	public void beginWatch() {
		try {
			this.watchID = JNotify.addWatch(REQUEST_BASE_PATH, mask, watchSubtree, (JNotifyListener) this);
			System.err.println("jnotify -----------已启动-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
	}
	
	
	

	/**
	 * 文件拷贝
	 * @param sourceFile
	 * @param targetFile
	 */
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
			System.err.println(e.getMessage());
			
		} 
}
	
	public  static void exceCmd(String cmd1){
		String cmd = "cmd /c "+cmd1;
	    
	    try{
	        Runtime.getRuntime().exec(cmd);
	        System.err.println("cmd:"+cmd);
	        
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	}

	
   public static String lastsub(String str) {
	    	int i=str.lastIndexOf("/");
	    	return str.substring(0, i);
	}
    public static String lastsub2(String str) {
    	int i=str.lastIndexOf("_");
    	return str.substring(0, i);
    }

    /**
     * 文件排序：根据修改时间，保证文件配对的正确性
     * @param path
     * @return
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
   
   /**
    * 初始化
    * @param parentPath
    * @param fileName
    */
   public static void startFirstLocal(String parentPath,String fileName ){  
		boolean status = fileName.contains("SNAP");
		boolean status1 = fileName.contains("BACK");
		if(status) {
			String targetFileName = lastsub2(fileName);
			sourceFtpFileName.put(targetFileName.substring(0,targetFileName.indexOf("_")), targetFileName);
			String sourceFile=parentPath+"/"+fileName;
			
			String bakFile=BASE_BAK_PATH+parentPath.substring(REQUEST_BASE_PATH.length())+"/"+fileName;

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
					String repath=sourceFile.replace("/", "\\");
	 				String cmd="cmd /c "+"del "+repath;
			       	while(true) {
			       		boolean flag=iszhanyong(sourceFile);
			       		if(flag==false) {
			       			JcopyFile(file1, file2);
			       			try{
					 			  Runtime.getRuntime().exec(cmd);
					 			  System.err.println("删除源文件-----"+cmd);
					 		   }catch(IOException e){
					 			        e.printStackTrace();
					 		}
			    			System.err.println("SNAP--"+sourceFile);
			    			System.err.println("SNAP--"+bakFile);
								break;
			       		}else {
				    			System.out.println(sourceFile+"---被占用继续等待---");
				    			Thread.sleep(1000);
				    		}
			       	}
			    }else {
			    	String del="rm -rf "+sourceFile;
			    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
			    	while (true) {
			    		String resturl=getLsof(cmd);
			    		if(resturl == null || resturl.length() <= 0) {
							JcopyFile(file1, file2);
			                ConfigMapUtil.runShell(del);
			                System.err.println("删除源文件-----"+del);
			    			System.err.println("SNAP--"+sourceFile);
			    			System.err.println("SNAP--"+bakFile);
							break;
			    		}else {
			    			System.out.println(sourceFile+"---被占用继续等待---");
			    			Thread.sleep(1000);
			    		}
			    		
			    	}
			    }
		       	
		       }catch(Exception e) {
		          e.printStackTrace();
		       }
		    
		}
		if(status1) {
			String sourceFile=parentPath+"/"+fileName;
			if(!sourceFtpFileName.containsKey(fileName.substring(0,fileName.indexOf("_")))) {
				return;
			}
			String newFileName=sourceFtpFileName.get(fileName.substring(0,fileName.indexOf("_")))+"_BACKGROUND.jpg";
			String bakFile=BASE_BAK_PATH+parentPath.substring(REQUEST_BASE_PATH.length())+"/"+newFileName;
			System.out.println("target:"+BASE_BAK_PATH+"-----"+bakFile);
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
					String repath=sourceFile.replace("/", "\\");
	 				String cmd="cmd /c "+"del "+repath;
			       	while(true) {
			       		boolean flag=iszhanyong(sourceFile);
			       		if(flag==false) {
			       			JcopyFile(file1, file2);
			       			try{
					 			  Runtime.getRuntime().exec(cmd);
					 			  System.err.println("删除源文件-----"+cmd);
					 		   }catch(IOException e){
					 			        e.printStackTrace();
					 		}
							System.err.println("BACK--"+sourceFile);
							System.err.println("BACK--"+bakFile);
								break;
			       		}else {
				    			System.out.println(sourceFile+"---被占用继续等待---");
				    			Thread.sleep(1000);
				    		}
			       	}
			    }else {
			    	String del="rm -rf "+sourceFile;
			    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
			    	while (true) {
			    		String resturl=getLsof(cmd);
			    		if(resturl == null || resturl.length() <= 0) {
							JcopyFile(file1, file2);
			                ConfigMapUtil.runShell(del);
			                System.err.println("删除源目录文件-----"+del);
							System.err.println("BACK--"+sourceFile);
							System.err.println("BACK--"+bakFile);
							break;
			    		}else {
			    			System.out.println(sourceFile+"---被占用继续等待---");
			    			Thread.sleep(1000);
			    		}
			    		
			    	}
			    }
		       	
		       }catch(Exception e) {
		          e.printStackTrace();
		       }
			
			
		}
       
	}
   
   
   public static void  copyBak(String fileName,String pathRoot ) {
		boolean status = fileName.contains("SNAP");
		boolean status1 = fileName.contains("BACK");
		if(status) {
			String targetFileName = lastsub2(fileName);
			sourceFtpFileName.put(targetFileName.substring(0,targetFileName.indexOf("_")), targetFileName);
			String sourceFile=pathRoot+"/"+fileName;
			String bakFile=BASE_BAK_PATH+"/"+fileName;
			System.err.println("source file----"+sourceFile);
			System.err.println("back file------"+bakFile);
			File file1 = new File(sourceFile);
		    File file2 = new File(bakFile);
		    try {
		    	String childPath=BASE_BAK_PATH+"/"+pathRoot.substring(pathRoot.indexOf(REQUEST_BASE_PATH)+REQUEST_BASE_PATH.length())+fileName.substring(0,fileName.lastIndexOf("/"));
		    	System.err.println("childPath----"+childPath);
		    	File filePath=new File(childPath);
		    	if  (!filePath .exists()  && !filePath .isDirectory())      
		    	{       
	/*	    	    String mkdir="mkdir -p "+childPath;
		    	    ConfigMapUtil.runShell(mkdir);*/
		    	    System.err.println("创建目录----"+childPath+filePath .mkdirs());
		    	}

		    	boolean isWindows=isWindows();
			    if(isWindows==true) {
					String repath=sourceFile.replace("/", "\\");
	 				String cmd="cmd /c "+"del "+repath;
		        	while(true) {
		        		boolean flag=iszhanyong(sourceFile);
		        		if(flag==false) {
		        			JcopyFile(file1, file2);
			       			try{
					 			  Runtime.getRuntime().exec(cmd);
					 			  System.err.println("删除源文件-----"+cmd);
					 		   }catch(IOException e){
					 			        e.printStackTrace();
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
			    	String del="rm -rf "+sourceFile;
			    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
			    	while (true) {
			    		String resturl=getLsof(cmd);
			    		if(resturl == null || resturl.length() <= 0) {
							JcopyFile(file1, file2);
			                ConfigMapUtil.runShell(del);
			                System.err.println("删除源目录文件-----"+del);
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
		if(status1) {
			String sourceFile=pathRoot+"/"+fileName;
			if(!sourceFtpFileName.containsKey(fileName.substring(0,fileName.indexOf("_")))) {
				return;
			}
			String tagetFile=BASE_BAK_PATH+"/"+sourceFtpFileName.get(fileName.substring(0,fileName.indexOf("_")))+"_BACKGROUND.jpg";
			System.out.println("target:"+BASE_BAK_PATH+"-----"+tagetFile);
			File file1 = new File(sourceFile);
			File file2 = new File(tagetFile);
			 try {
			    	String childPath=BASE_BAK_PATH+"/"+pathRoot.substring(pathRoot.indexOf(REQUEST_BASE_PATH)+REQUEST_BASE_PATH.length())+fileName.substring(0,fileName.lastIndexOf("/"));
			    	System.err.println("childPath----"+childPath);
			    	File filePath=new File(childPath);
			    	if  (!filePath .exists()  && !filePath .isDirectory())      
			    	{       
			    	      
		/*	    	    String mkdir="mkdir -p "+childPath;
			    	    ConfigMapUtil.runShell(mkdir);*/
			    	    System.err.println("创建目录----"+childPath+filePath .mkdirs());
			    	}

			    	boolean isWindows=isWindows();
				    if(isWindows==true) {
						String repath=sourceFile.replace("/", "\\");
		 				String cmd="cmd /c "+"del "+repath;
			        	while(true) {
			        		boolean flag=iszhanyong(sourceFile);
			        		if(flag==false) {
			        			JcopyFile(file1, file2);
				       			try{
						 			  Runtime.getRuntime().exec(cmd);
						 			  System.err.println("删除源文件-----"+cmd);
						 		   }catch(IOException e){
						 			        e.printStackTrace();
						 		}
								System.err.println("拷贝source file----"+sourceFile);
								System.err.println("拷贝back file------"+tagetFile);
								break;
			        		}else {
				    			System.out.println(sourceFile+"---被占用继续等待---");
				    			Thread.sleep(1000);
				    		}
			        	}
				    }else {
				    	String del="rm -rf "+sourceFile;
				    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
				    	while (true) {
				    		String resturl=getLsof(cmd);
				    		if(resturl == null || resturl.length() <= 0) {
								JcopyFile(file1, file2);
				                ConfigMapUtil.runShell(del);
				                System.err.println("删除源目录文件-----"+del);
								System.err.println("拷贝source file----"+sourceFile);
								System.err.println("拷贝back file------"+tagetFile);
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
		if(file.isDirectory()) {
			System.out.println("空文件夹");
			return ;
		}
	    copyBak(filenamecs,rootPath);
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
