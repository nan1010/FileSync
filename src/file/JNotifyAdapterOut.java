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
	
	/** �����ӵ�Ŀ¼ */
	String path = REQUEST_BASE_PATH;
	/** ��עĿ¼���¼� */
	int mask = JNotify.FILE_CREATED ;
	/** �Ƿ������Ŀ¼������������ */
	boolean watchSubtree = true;
	/** ��������Id */
	public int watchID;
	
	static{
		File file=new File(REQUEST_BASE_PATH);
		showDirFisrt(file);
		  
	 }
	

 
	public static void main(String[] args) {
		new JNotifyAdapterOut().beginWatch();
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
	
	public static void showDirFisrt(File dir) {
        if(dir.exists()){
            //����·�������飬��Щ·������ʾ�˳���·������ʾ��Ŀ¼�е��ļ���Ŀ¼��
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
            System.out.println("�ļ������ڣ�");
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
	    System.err.print("���"+execcmd);
	    try{
	        Runtime.getRuntime().exec(execcmd);
	    }catch(IOException e){
	        e.printStackTrace();
	    }
		
		
	}
	public static String subpath(String str){
		if(str.split("\\\\").length>2){
			//�����ǻ�ȡ"\\"���ŵ�λ�� 
	        Matcher slashMatcher = Pattern.compile("\\\\").matcher(str);
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
	
    public static String lastsub(String str) {
    	int i=str.lastIndexOf("\\");
    	return str.substring(0, i);
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
	public void fileCreated(int wd, String rootPath, String name) {
		String sourePath= rootPath.replaceAll("/", "\\");
		String sourceFile= sourePath + "\\" + name;
		uplaodfile(sourceFile,lastsub(subpath(sourceFile)));
		System.err.println("�ļ�������, ����λ��Ϊ�� " + rootPath + "/" + name);

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
