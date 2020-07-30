package file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testfilemonit {

	private static final String FTP_USER =ConfigMapUtil.getValueByKey("ftp.user");
	private static final String FTP_PWD = "123456";
	private static final String FTP_IP_PORT = "10.129.75.10:21";
	private static final String TARGET_BASE_PATH = "E:\\ppt2";
	static{
		  
		//uploadFirst();
		  
	 }
	public void copyfile(String sourceFile,String targetFile){
	    String cmd = "cmd /c copy "+sourceFile+" "+targetFile;
	    try{
	        Runtime.getRuntime().exec(cmd);
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	}
	
	 public static void uploadFirst() {
		 System.out.println("static block invoked!");
	 }
	
	public String test01(String str){
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
	
	 public static void showDirFisrt(File dir) {
	        if(dir.exists()){
	            //抽象路径名数组，这些路径名表示此抽象路径名表示的目录中的文件和目录。
	            File[] files = dir.listFiles();
	            if(null!=files){
	                for (int i = 0; i < files.length; i++) {
	                    if (files[i].isDirectory()) {
	                    	showDirFisrt(files[i]);
	                    	//System.out.println(files[i].toString());
	                        //System.out.println(lastsub(subpath(files[i].toString())));
	                    	
	                    } else  {
	        
	                        System.out.println(files[i].toString());
	                        System.out.println(lastsub(subpath(files[i].toString())));
	                    	
	    
	                    }
	                }
	            }
	        }else{
	            System.out.println("文件不存在！");
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
    	int i=str.lastIndexOf("/");
    	return str.substring(0, i);
    }
    
	 public static String subpath1(String str){
			if(str.split("\\\\").length>1){
				//这里是获取"\\"符号的位置 
		        Matcher slashMatcher = Pattern.compile("\\\\").matcher(str);
		        int mIdx = 0;
				while(slashMatcher.find()) {
				    mIdx++;
				    //当"\\"符号第三次出现的位置  
				    if(mIdx == 1){
				        break;
				    }
				}
		        return str.substring(slashMatcher.start());
			}
			return str;
		}
    /* 返回指定字符出现的第一次位置截取*/
	 public static String subpath3(String str){
			if(str.split("/").length>1){
				//这里是获取"\\"符号的位置 
		        Matcher slashMatcher = Pattern.compile("/").matcher(str);
		        int mIdx = 0;
				while(slashMatcher.find()) {
				    mIdx++;
				    //当"\\"符号第三次出现的位置  
				    if(mIdx == 3){
				        break;
				    }
				}
		        return str.substring(slashMatcher.start());
			}
			return str;
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
    public static boolean iszhanyou() {
    	try {
    		File file = new File("E:\\ppt\\testttt.txt");
        	RandomAccessFile raf = new RandomAccessFile(file,"rws");
        	FileChannel fc = raf.getChannel();
        	while(true) {
        		FileLock fl = fc.tryLock(0, Long.MAX_VALUE, false); // true表示是共享锁，false则是独享锁定
        		  if(fl!=null) {
        			  return true;
        		  }
				else
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
	    
    		
    	}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
        

			//ConfigMapUtil.runShell("sshpass -p 1234Qwer,./ scp /home/wh/textsf.txt root@172.29.1.18:/home/wh/textsf.txt");
			
        	//ConfigMapUtil.runShell("sshpass -p 1234Qwer,./ ssh root@172.29.1.18 [ -d /var/test5 ] || mkdir -p /var/test5");
/*			List <String> commandArr=new ArrayList<>();
			commandArr.add("sshpass -p '1234Qwer,./' ssh root@172.29.1.18 \\\"[ -d /var/test3 ] || mkdir -p /var/test3\\\" ");
			String result=ConfigMapUtil.run(commandArr.toArray(new String [commandArr.size()]));
			System.out.println(result+"------执行结果-------");*/
        	//System.out.println(result+"------执行结果-------");
        	//String str="/upload_xxt_copy/test1/aa";
        	//String str1="/upload_xxt_copy";
        	//System.out.print(lastsub(str));
        	
        /*	List<File> list = getFileSort("/home/wh1");
            for (File file : list) {
               System.out.println("getName--"+file.getName().toString());
               System.out.println("getAbsolutePath"+file.getAbsolutePath().toString());
               System.out.println("getCanonicalPath"+file.getCanonicalPath().toString());
               System.out.println("getParent"+file.getParent().toString());
               System.out.println("getPath"+file.getPath().toString());
            }*/
        	//System.out.println(subpath3(str));
     /*   	int benginindex=str1.length();
        	System.out.println(benginindex);
        	System.out.println(str.substring(benginindex));
        	*/
        	
        	
        		/*File file = new File("E:\\ppt\\testttt.txt");
            	RandomAccessFile raf = new RandomAccessFile(file,"rws");
            	FileChannel fc = raf.getChannel();
            	while(true) {
            		FileLock fl = fc.tryLock(0, Long.MAX_VALUE, false); // true表示是共享锁，false则是独享锁定
            		  if(fl!=null) {
            			  //return true;
            		  }
					else
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            		}*/
        		File file1 = new File("E:\\ppt\\test2.txt");
        		FileWriter fw =null;
        		BufferedWriter bw=null;
                try {
                	  fw = new FileWriter(file1);
                      bw= new BufferedWriter(fw);
                     while (true){
                    	bw.write("hello"+"\n");
                        bw.flush();
                    }
               
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                   try {
					fw.close();
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                   
                }
                
		/* String str = "333.jpg";  
	        boolean status = str.contains("/");  
	        if(status){  
	            System.out.println("包含");  
	        }else{  
	            System.out.println("不包含");  
	        }  */
	      
                
                
           



        	
	
}

	}     	
        	
        
    

	


