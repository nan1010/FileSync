package file;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


public class ftptest {
	
	//分割路径
	public static String[] getPathList(String path){
		String[] dirs = path.split("/");
		if (dirs.length > 0 && dirs[0].isEmpty())
			return Arrays.copyOfRange(dirs, 1, dirs.length);
		return dirs;
		
		/*List<String> list = new ArrayList<>();
		String pathname = "";
		for(String str : dirs){
			if(str==null||str.equals("")){
				continue;
			}
			pathname = pathname + "/" + str;
			list.add(pathname);
		}
		return list;*/
	}
    


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FTPClient ftp = new FTPClient();
        try {
            ftp.connect("219.159.71.175",21);//���õ�ַ�Ͷ˿ں�
            ftp.login("ZZPT", "1234Qwer+");//�û���������
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);//�ϴ��ļ����� �������ļ�
            int reply = ftp.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)){//��������Ƿ���Ч
                System.out.println("error");
                return;
            }

            String str2="/test/test3";
            String[] list = getPathList(str2);
            //ftp.changeWorkingDirectory("/test");
			for(int i=0; i<list.length; i++){
				if(!ftp.changeWorkingDirectory(list[i])){//若路径未存在则创建路径
					if(!ftp.makeDirectory(list[i])){//若路径创建失败则不再继续处理
						System.out.println("create dir fail --> " + list[i]);
						return;
					}
					ftp.changeWorkingDirectory(list[i]);
				}
			}

            //ftp.makeDirectory("/test13");
            ftp.changeWorkingDirectory(str2);
            File file = new File("E:\\ppt\\test\\888.txt");
            
            FileInputStream fis = new FileInputStream(file);

            ftp.storeFile(file.getName(), fis);//�ؼ�����,�����־û���Ӳ����
            fis.close();
            ftp.logout();
            
            //String str44="test\\pom6.xml";

            //System.out.println(str44.replace("/", "\\\\"));
          
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

	}

}
