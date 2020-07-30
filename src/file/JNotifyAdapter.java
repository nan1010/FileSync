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
	/** �����ӵ�Ŀ¼ */
	String path = REQUEST_BASE_PATH;
	/** ��עĿ¼���¼� */
	int mask = JNotify.FILE_CREATED ;
	/** �Ƿ������Ŀ¼������������ */
	boolean watchSubtree = true;
	/** ��������Id */
	public int watchID;

 
	public static void main(String[] args) {
		new JNotifyAdapter().beginWatch();
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
	
	public void copyfile(String sourceFile,String targetFile){
	    String cmd = "cmd /c echo F|xcopy "+sourceFile+" "+targetFile+" /D/Y";
	    System.err.print("�������"+cmd);
	    try{
	        Runtime.getRuntime().exec(cmd);
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	}
	
	public String subpath(String str){
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
		String tagetFile=TARGET_BASE_PATH+subpath(sourceFile);
		copyfile(sourceFile, tagetFile);
		System.out.println("Դ�ļ�·����"+sourceFile);
		System.out.println("Ŀ���ļ�·����"+tagetFile);
		System.err.println("�ļ�������, ����λ��Ϊ�� " + sourePath + "\\" + name);
		

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
