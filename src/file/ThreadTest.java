package file;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadTest {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

		new Thread(new ProducerThread("生产者1",queue)).start();
		new Thread(new ProducerThread("生产者2",queue)).start();
		new Thread(new ConsumerThread("消费者1",queue)).start();
		new Thread(new ConsumerThread("消费者2",queue)).start();
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
		            System.out.println( this.name + "：线程启动。");
		            while (flag) {
		            	Random r = new Random();
		                String data = "/home/hxct/"+r.nextInt(100)+".jpg";
		                // 将数据存入队列中
		                queue.offer(data);
		                System.out.println(this.name + "：存入" + data + "到队列中。");
		                //等待1秒钟
		                Thread.sleep(1000);
		            }
		        } catch (Exception e) {
		 
		        } finally {
		            System.out.println(this.name + "：退出线程。");
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
			System.out.println( this.name + "：线程启动。");
	        try {
	            while (flag) {
	                System.out.println( this.name + "：正在从队列中获取数据......");
	                String data = queue.poll();
	                System.out.println(this.name + "：拿到队列中的数据：" + data);
	                //等待1秒钟
	                Thread.sleep(1000);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            System.out.println(this.name + "：消退出线程。");
	        }
			
		}
		
	}


