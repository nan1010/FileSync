package file;

import java.io.File;


public class testzhanyong {
	private static final String OS = System.getProperty("os.name").toLowerCase();  
	private static final String REQUEST_BASE_PATH =ConfigMapUtil.getValueByKey("base.path");
	
	public static  boolean iszhanyong() {
		try {
		    File file = new File("E:\\ppt\\test2.txt");
		    if(file.renameTo(file)) {
		    	return false;
		    }else {
		    	return true;
		    }
			
		} catch (Exception e) {
			return true;
		}
		
	}
    public static boolean isWindows(){  
        return OS.indexOf("windows")>=0;  
    }  

	public static void main(String[] args) {
		String str="E:\\ppt\\test\\test2\\test2.txt";
		String str1=str.substring(REQUEST_BASE_PATH.length());
		System.out.println(str1);
		
		// TODO Auto-generated method stub
		System.out.println("===========os.name:"+System.getProperties().getProperty("os.name")); 
		boolean f=isWindows();
		System.out.println(f);
		while (true) {
			boolean flag =iszhanyong();
			if(flag==false) {
				System.out.println("ûռ��"+flag);
			    break;
			}else {
				try {
					Thread.sleep(1000);
					System.out.println("ռ��"+flag);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
         
	}

}
