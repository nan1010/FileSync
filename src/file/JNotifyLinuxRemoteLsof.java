package file;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.JNotifyException;



public class JNotifyLinuxRemoteLsof implements  JNotifyListener {
	private static final String BASE_BAK_PATH =ConfigMapUtil.getValueByKey("base.bakpath");
	private static final String TARGET_BASE_PATH =ConfigMapUtil.getValueByKey("target.base.path");
	private static final String REMOTE_IP =ConfigMapUtil.getValueByKey("remote.ip");
	private static final String REMOTE_PWD =ConfigMapUtil.getValueByKey("remote.pwd");
	private static final String REMOTE_USER =ConfigMapUtil.getValueByKey("remote.user");
	private static final String REMOTE_PORT =ConfigMapUtil.getValueByKey("remote.port");
	//public static String sourceFtpFileName="";
	//public static String sourceFtpFileName2="";
	/** �����ӵ�Ŀ¼ */
	String path = BASE_BAK_PATH;
	/** ��עĿ¼���¼� */
	int mask = JNotify.FILE_CREATED| JNotify.FILE_DELETED| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
 ;
	/** �Ƿ������Ŀ¼������������ */
	boolean watchSubtree = true;
	/** ��������Id */
	public int watchID;
	
	static{
        List<File> list = getFileSort(BASE_BAK_PATH);
        for (File file : list) {
        	startFirstRemote(file.getParent(),file.getPath());
        	
        }
	 }

 
	public static void main(String[] args) {
		new JNotifyLinuxRemoteLsof().beginWatch();
	}
 
	/**
	 * ��������ʱ�������ӳ���
	 * 
	 * @return
	 */
	public void beginWatch() {
		/** ��ӵ����Ӷ����� */
		try {
			this.watchID = JNotify.addWatch(BASE_BAK_PATH, mask, watchSubtree, (JNotifyListener) this);
			System.err.println("jnotify -----------�����ɹ�-----------");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
		// ��ѭ�����߳�һֱִ�У�����һ���Ӻ����ִ�У���Ҫ��Ϊ�������߳�һֱִ��
		// ����ʱ��ͼ���ļ�������Ч���޹أ�����˵���Ǽ���Ŀ¼�ļ��ı�һ���Ӻ�ż�⵽����⼸����ʵʱ�ģ����ñ���ϵͳ�⣩
		while (true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {// ignore it
			}
		}
	}
	
	public static String subpath3(String str){
		if(str.split("/").length>1){
			//�����ǻ�ȡ"\\"���ŵ�λ�� 
	        Matcher slashMatcher = Pattern.compile("/").matcher(str);
	        int mIdx = 0;
			while(slashMatcher.find()) {
			    mIdx++;
			    //��"\\"���ŵ����γ��ֵ�λ��  
			    if(mIdx == 2){
			        break;
			    }
			}
	        return str.substring(slashMatcher.start());
		}
		return str;
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
    
	public static void startFirstRemote(String parentPath,String path ){  
		String sourceFile=BASE_BAK_PATH+path.substring(BASE_BAK_PATH.length());
		String remoteFile=TARGET_BASE_PATH+path.substring(BASE_BAK_PATH.length());
        String tagteChildPath=TARGET_BASE_PATH+parentPath.substring(BASE_BAK_PATH.length());
        String mkdir="sshpass -p "+REMOTE_PWD+" ssh "+"-p "+REMOTE_PORT+" "+REMOTE_USER+"@"+REMOTE_IP+" [ -d "+tagteChildPath+" ]"
        		+" || mkdir -p "+tagteChildPath;
		StringBuffer buf=new StringBuffer();
        buf.append("sshpass -p ").append(REMOTE_PWD).append(" ").append("scp ").append("-P ").append(REMOTE_PORT).append(" ")
        .append(sourceFile).append(" ").append(REMOTE_USER).append("@").append(REMOTE_IP)
        .append(":").append(remoteFile);
		String del="rm -rf "+sourceFile;
        try {
			ConfigMapUtil.runShell(mkdir);
			System.err.println("��������ʱ������Զ�����ļ�������-----"+mkdir);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
    	try {
    		while (true) {
        		String resturl=getLsof(cmd);
        		if(resturl == null || resturl.length() <= 0) {
                    ConfigMapUtil.runShell(buf.toString());
                    System.err.println("��������ʱ��������Զ��Ŀ¼����-----"+buf.toString());
                    ConfigMapUtil.runShell(del);
                    System.err.println("��������ʱ��ɾ��bakĿ¼�ļ�����-----"+del);
    				break;
        		}else {
        			System.out.println("��ռ�ü����ȡ�������");
        			Thread.sleep(2000);
        		}
        		
        	}
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
	}
	
	public static void copyRemote(String fileName){  
		String sourceFile=BASE_BAK_PATH+"/"+fileName;
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
			System.err.println("����Զ�����ļ�������-----"+mkdir);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
        String[] cmd = new String[] { "/bin/sh", "-c", " lsof  "+sourceFile };
    	try {
    		while (true) {
        		String resturl=getLsof(cmd);
        		if(resturl == null || resturl.length() <= 0) {
                    ConfigMapUtil.runShell(buf.toString());
                    System.err.println("������Զ��Ŀ¼����-----"+buf.toString());
                    ConfigMapUtil.runShell(del);
                    System.err.println("ɾ��bakĿ¼�ļ�����-----"+del);
    				break;
        		}else {
        			System.out.println("��ռ�ü����ȡ�������");
        			Thread.sleep(2000);
        		}
        		
        	}
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
	}
	
    public static String lastsub(String str) {
    	int i=str.lastIndexOf("/");
    	return str.substring(0, i);
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
		System.err.println("fileCreated--rootPath " + rootPath);
		System.err.println("fileCreated--name "  + Filename);
		File file=new File(rootPath+"/"+Filename);
		if(file.isDirectory()) {
			return ;
		}
		copyRemote(Filename);
		
		

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
