package file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.contentobjects.jnotify.JNotify;

import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.JNotifyException;



public class JNotifyLinuxLocaLsof implements  JNotifyListener {
	private static final String REQUEST_BASE_PATH =ConfigMapUtil.getValueByKey("base.path");
	private static final String BASE_BAK_PATH =ConfigMapUtil.getValueByKey("base.bakpath");
	//public static String sourceFtpFileName="";
	//public static String sourceFtpFileName2="";
	/** 被监视的目录 */
	String path = REQUEST_BASE_PATH;
	/** 关注目录的事件 */
	int mask = JNotify.FILE_CREATED| JNotify.FILE_DELETED| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
 ;
	/** 是否监视子目录，即级联监视 */
	boolean watchSubtree = true;
	/** 监听程序Id */
	public int watchID;
	
	/*static{
        List<File> list = getFileSort(REQUEST_BASE_PATH);
        for (File file : list) {
           //copyPicFirst(file.getName().toString());
        }
	 }*/

 
	public static void main(String[] args) {
		new JNotifyLinuxLocaLsof().beginWatch();
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
    public static String lastsub(String str) {
    	int i=str.lastIndexOf("/");
    	return str.substring(0, i);
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
	    	    System.err.println("是否创建childPath----"+childPath);
	    	}
	    	//String mkdir="mkdir -p "+childPath;
	    	//System.err.println("mkdir----"+mkdir);
	    	//ConfigMapUtil.runShell(mkdir);
			/*boolean isfinsh=checkFileWritingOn(file1);
			if(isfinsh==true) {
				JcopyFile(file1, file2);
				System.err.println("拷贝source file----"+sourceFile);
				System.err.println("拷贝back file------"+bakFile);
			}*/
	    	String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
	    	while (true) {
	    		String resturl=getLsof(cmd);
	    		if(resturl == null || resturl.length() <= 0) {
					JcopyFile(file1, file2);
					System.err.println("拷贝source file----"+sourceFile);
					System.err.println("拷贝back file------"+bakFile);
					break;
	    		}else {
	    			System.out.println("被占用继续等。。。。");
	    			Thread.sleep(2000);
	    		}
	    		
	    	}
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	 public static boolean checkFileWritingOn(File file) throws Exception{
		    long oldLen = 0;
		    long newLen = 0;
		    while(true){
		      newLen = file.length();
		      if ((newLen - oldLen) > 0||newLen==0) {
		        oldLen = newLen;
		        //System.out.println(file.length());
		        System.err.println(file.getName() + " being transferred");
		        Thread.sleep(2000);
		      } else {
		        //System.out.println("done");
		    	 System.err.println(file.getName() + " transmit complete");
		        return true;
		      }
		    }
}

	
	
	 public static void JcopyFile(File sourceFile,File targetFile){  
         // 新建文件输入流并对它进行缓冲   
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
         // 新建文件输出流并对它进行缓冲   
         
			output = new FileOutputStream(targetFile);
			outBuff=new BufferedOutputStream(output);  
		  
         // 缓冲数组   
         byte[] b = new byte[1024 * 5];  
         int len;  
         while ((len =inBuff.read(b)) != -1) {  
             outBuff.write(b, 0, len);  
         }  
         // 刷新此缓冲的输出流   
         outBuff.flush();  
           
         //关闭流   
         inBuff.close();  
         outBuff.close();  
         output.close();  
         input.close(); 
/*         String cmd="del "+sourceFile.getPath();
         exceCmd(cmd);*/
         
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println(e.getMessage());
/*			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			}
			JcopyFile(sourceFile, targetFile);*/
			
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
	public void fileCreated(int wd, String rootPath, String Filename) {
		File file=new File(rootPath+"/"+Filename);
		if(file.isDirectory()) {
			return ;
		}
		copyBak(Filename,rootPath);
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
		//System.out.println("源文件路径："+sourceFile);
		//System.err.println("文件被创建, 创建位置为： " +name);


	    

		
		
	}

	@Override
	public void fileRenamed(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}
	
	



}
