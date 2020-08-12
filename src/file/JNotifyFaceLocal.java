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
	private static final String SOURCE_DIR =ConfigMapUtil.getValueByKey("source.dir");
	private static final String DEST_DIR =ConfigMapUtil.getValueByKey("dest.dir");
	private static final String IS_SOURCE_DEL =ConfigMapUtil.getValueByKey("source.delete");
	private static final String OS = System.getProperty("os.name").toLowerCase();  
	public static Map<String, String> sourceFtpFileName = new HashMap<String, String>();
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
        List<File> list = getFileSort(SOURCE_DIR);
        for (File file : list) {
        	boolean isWindows=isWindows();
        	if(isWindows==true) {
        		startFirstLocal(file.getParent().replace("\\", "/"),file.getName());
        	}else {
        		startFirstLocal(file.getParent(),file.getName());
        	}
        	System.out.println(file.getParent());
        	System.out.println(file.getName());
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
			this.watchID = JNotify.addWatch(SOURCE_DIR, mask, watchSubtree, (JNotifyListener) this);
			System.out.println("jnotify -----------已启动-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		} try {
			while(true) {// jnotify监控线程为守护线程，因此主线程不能终止，否则会导致进程退出
				Thread.sleep(5000);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	

	/**
	 * 文件拷贝
	 * @param sourceFile
	 * @param destFile
	 */
	public static void JcopyFile(File sourceFile,File destFile){  
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
           output = new FileOutputStream(destFile);
           outBuff=new BufferedOutputStream(output);  
	       byte[] b = new byte[1024 * 4];
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
	        System.out.println("cmd:?"+cmd);
	        
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
    * 初始化
    * @param parentPath
    * @param fileName
    */
   public static void startFirstLocal(String parentPath,String fileName ){  
		boolean status = fileName.contains("SNAP");
		boolean status1 = fileName.contains("BACK");
		if(status) {
			String destFileName = lastsub2(fileName);
			sourceFtpFileName.put(destFileName.toString(), destFileName);
			String sourceFile=parentPath+"/"+fileName;
			
			String destFile=DEST_DIR+parentPath.substring(SOURCE_DIR.length())+"/"+fileName;

			String destChildPath=DEST_DIR+parentPath.substring(SOURCE_DIR.length());
			File file1 = new File(sourceFile);
		    File file2 = new File(destFile);
		    try {
		       	File filePath=new File(destChildPath);
			    if (!filePath .exists() && !filePath .isDirectory())      
			    {       
			    	filePath .mkdirs();    
			    }
			    boolean isWindows=isWindows();
			    if(isWindows==true) {
					String repath=sourceFile.replace("/", "\\");
	 				String cmd="cmd /c "+"del "+repath;
			       	while(true) {
			       		boolean flag=isOccupied(sourceFile);
			       		if(flag==false) {
			       			JcopyFile(file1, file2);
			       			if(IS_SOURCE_DEL.equals("0")) {
			       				try{
					 			  Runtime.getRuntime().exec(cmd);
					 			  System.out.println("删除源文件-----"+cmd);
			       				}catch(IOException e){
					 			        e.printStackTrace();
			       				}
			       			}
			       			
			    			System.out.println("SNAP--"+sourceFile);
			    			System.out.println("SNAP--"+destFile);
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
							if(IS_SOURCE_DEL.equals("0")) {
								 ConfigMapUtil.runShell(del);
					                System.out.println("删除源文件-----"+del);
				    		}			               
			    			System.out.println("SNAP--"+sourceFile);
			    			System.out.println("SNAP--"+destFile);
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
			String destFileName = lastsub2(fileName);
			sourceFtpFileName.put(destFileName.toString(), destFileName);
			if(!sourceFtpFileName.containsKey(fileName.substring(0,fileName.indexOf("_")))) {
				return;
			}
			String newFileName=sourceFtpFileName.get(fileName.substring(0,fileName.indexOf("_")))+"_BACKGROUND.jpg";
			String destFile=DEST_DIR+parentPath.substring(SOURCE_DIR.length())+"/"+ newFileName;
			System.out.println("target:"+DEST_DIR+"-----"+destFile);
			String destChildPath=DEST_DIR+parentPath.substring(SOURCE_DIR.length());
			File file1 = new File(sourceFile);
		    File file2 = new File(destFile);
		    try {
		       	File filePath=new File(destChildPath);
			    if (!filePath .exists() && !filePath .isDirectory())      
			    {       
			    	filePath .mkdirs();    
			    }
			    boolean isWindows=isWindows();
			    if(isWindows==true) {
					String repath=sourceFile.replace("/", "\\");
	 				String cmd="cmd /c "+"del "+repath;
			       	while(true) {
			       		boolean flag=isOccupied(sourceFile);
			       		if(flag==false) {
			       			JcopyFile(file1, file2);
			       			if(IS_SOURCE_DEL.equals("0")) {
			       				try{
						 			  Runtime.getRuntime().exec(cmd);
						 			  System.out.println("删除源文件-----"+cmd);
						 		   }catch(IOException e){
						 			        e.printStackTrace();
						 		}
			       			}			       			
							System.out.println("BACK--"+sourceFile);
							System.out.println("BACK--"+destFile);
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
							if(IS_SOURCE_DEL.equals("0")) {
								ConfigMapUtil.runShell(del);
				                System.out.println("删除源目录文件-----"+del);
							}	                
							System.out.println("BACK--"+sourceFile);
							System.out.println("BACK--"+destFile);
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

	/**
	 * 文件拷贝
	 * @param fileName
	 * @param pathRoot
	 */
	public static void  copy(String fileName,String pathRoot) {
		boolean status = fileName.contains("SNAP");
		boolean status1 = fileName.contains("BACK");
		if(status) {
			String destFileName = lastsub2(fileName);
			sourceFtpFileName.put(destFileName.toString(), destFileName);
			String sourceFile=pathRoot+"/"+fileName;
			String destFile=DEST_DIR+"/"+fileName;
			System.out.println("source file----"+sourceFile);
			System.out.println("back file------"+destFile);
			File file1 = new File(sourceFile);
		    File file2 = new File(destFile);
		    try {
		    	String childPath=DEST_DIR+"/"+pathRoot.substring(pathRoot.indexOf(SOURCE_DIR)+SOURCE_DIR.length())+fileName.substring(0,fileName.lastIndexOf("/"));
		    	System.out.println("childPath----"+childPath);
		    	File filePath=new File(childPath);
		    	if  (!filePath .exists()  && !filePath .isDirectory())      
		    	{       
	/*	    	    String mkdir="mkdir -p "+childPath;
		    	    ConfigMapUtil.runShell(mkdir);*/
		    	    System.out.println("创建目录----"+childPath+filePath .mkdirs());
		    	}

		    	boolean isWindows=isWindows();
			    if(isWindows==true) {
					String repath=sourceFile.replace("/", "\\");
	 				String cmd="cmd /c "+"del "+repath;
		        	while(true) {
		        		boolean flag=isOccupied(sourceFile);
		        		if(flag==false) {
		        			JcopyFile(file1, file2);
		        			if(IS_SOURCE_DEL.equals("0")) {
		        				try{
						 			  Runtime.getRuntime().exec(cmd);
						 			  System.out.println("删除源文件-----"+cmd);
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
			    	String del="rm -rf "+sourceFile;
			    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
			    	while (true) {
			    		String resturl=getLsof(cmd);
			    		if(resturl == null || resturl.length() <= 0) {
							JcopyFile(file1, file2);
							if(IS_SOURCE_DEL.equals(0)) {
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
		if(status1) {
			String sourceFile=pathRoot+"/"+fileName;
			if(!sourceFtpFileName.containsKey(fileName.substring(0,fileName.indexOf("_")))) {
				return;
			}
			String destFile=DEST_DIR+"/"+sourceFtpFileName.get(fileName.substring(0,fileName.indexOf("_")))+"_BACKGROUND.jpg";
			System.out.println("destination:"+DEST_DIR+"-----"+destFile);
			File file1 = new File(sourceFile);
			File file2 = new File(destFile);
			 try {
			    	String childPath=DEST_DIR+"/"+pathRoot.substring(pathRoot.indexOf(SOURCE_DIR)+SOURCE_DIR.length())+fileName.substring(0,fileName.lastIndexOf("/"));
			    	System.err.println("childPath----"+childPath);
			    	File filePath=new File(childPath);
			    	if  (!filePath .exists()  && !filePath .isDirectory())      
			    	{       
			    	      
		/*	    	    String mkdir="mkdir -p "+childPath;
			    	    ConfigMapUtil.runShell(mkdir);*/
			    	    System.out.println("创建目录----"+childPath+filePath .mkdirs());
			    	}

			    	boolean isWindows=isWindows();
				    if(isWindows==true) {
						String repath=sourceFile.replace("/", "\\");
		 				String cmd="cmd /c "+"del "+repath;
			        	while(true) {
			        		boolean flag=isOccupied(sourceFile);
			        		if(flag==false) {
			        			JcopyFile(file1, file2);
			        			if(IS_SOURCE_DEL.equals("0")) {
			        				try{
							 			  Runtime.getRuntime().exec(cmd);
							 			  System.out.println("删除源文件-----"+cmd);
							 		   }catch(IOException e){
							 			        e.printStackTrace();
							 		}
			        			}				       			
								System.out.println("拷贝source file----"+sourceFile);
								System.out.println("拷贝backups file------"+destFile);
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
								if(IS_SOURCE_DEL.equals("0")) {
									ConfigMapUtil.runShell(del);
					                System.out.println("删除源目录文件-----"+del);
								}				                
								System.out.println("拷贝source file----"+sourceFile);
								System.out.println("拷贝backups file------"+destFile);
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


   public static List<File> getFiles(String realPath, List<File> files) {

       File realFile = new File(realPath);
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
	public void fileCreated(int wd, String rootPath, String fileName) {
		String filenamecs=fileName.replace("\\", "/");
		File file=new File(rootPath+"/"+filenamecs);
		if(file.isDirectory()) {
			System.out.println("空文件夹");
			return ;
		}
		//新建文件肯定没有SNAP/BACK，不必运行此函数
	    copy(filenamecs,rootPath);
		System.out.println("fileCreated--rootPath " + rootPath);
		System.out.println("fileCreated--name "  + fileName);	

	}

	@Override
	public void fileDeleted(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileModified(int arg0, String rootPath, String name) {
		// TODO Auto-generated method stub

		
		//System.err.println("fileModified, the modified file path is " + rootPath + "/" + name);
        
		//copyfile(sourceFile, destFile);
		//renamePic(name,sourePath);
		//System.out.println("????·????"+sourceFile);
		//System.err.println("?????????, ????λ????? " +name);


	    

		
		
	}

	@Override
	public void fileRenamed(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}
	
	



}
