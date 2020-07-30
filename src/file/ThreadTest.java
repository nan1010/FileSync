package file;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadTest {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

		new Thread(new ProducerThread("������1",queue)).start();
		new Thread(new ProducerThread("������2",queue)).start();
		new Thread(new ConsumerThread("������1",queue)).start();
		new Thread(new ConsumerThread("������2",queue)).start();
	}


}
	
	
class ProducerThread implements Runnable {
		 private String name;
		 private ConcurrentLinkedQueue<String> queue;
		 private volatile boolean flag = true;
		 public ProducerThread(String name, ConcurrentLinkedQueue<String> queue) {
		        this.name = name;
		        this.queue = queue;
		 }

		 @Override
		 public void run() {
			// TODO Auto-generated method stub
		       try {
		            System.out.println( this.name + "���߳�������");
		            while (flag) {
		            	Random r = new Random();
		                String data = "/home/hxct/"+r.nextInt(100)+".jpg";
		                // �����ݴ��������
		                queue.offer(data);
		                System.out.println(this.name + "������" + data + "�������С�");
		                //�ȴ�1����
		                Thread.sleep(1000);
		            }
		        } catch (Exception e) {
		 
		        } finally {
		            System.out.println(this.name + "���˳��̡߳�");
		        }
		}
		
	}
	
class ConsumerThread implements Runnable {
		 private String name;
		 private ConcurrentLinkedQueue<String> queue;
		 private volatile boolean flag = true;
		 public ConsumerThread(String name, ConcurrentLinkedQueue<String> queue) {
		        this.name = name;
		        this.queue = queue;
		 }

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println( this.name + "���߳�������");
	        try {
	            while (flag) {
	                System.out.println( this.name + "�����ڴӶ����л�ȡ����......");
	                String data = queue.poll();
	                System.out.println(this.name + "���õ������е����ݣ�" + data);
	                //�ȴ�1����
	                Thread.sleep(1000);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            System.out.println(this.name + "�����˳��̡߳�");
	        }
			
		}
		
	}


