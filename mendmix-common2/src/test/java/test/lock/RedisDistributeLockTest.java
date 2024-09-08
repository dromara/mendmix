/*
 * Copyright 2016-2022 dromara.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomUtils;

import org.dromara.mendmix.common.lock.redis.RedisDistributeLock;

public class RedisDistributeLockTest {

	private static CountDownLatch latch;
	
	public static void main(String[] args) throws Exception {
		

		int taskcount = 21;
		latch = new CountDownLatch(taskcount);
		ExecutorService threadPool = Executors.newFixedThreadPool(taskcount);
		
		for (int i = 0; i < taskcount; i++) {
			threadPool.execute(new LockWorker("worker-"+i));
		}
		
		latch.await();
		threadPool.shutdown();
	}
	
	static class LockWorker implements Runnable{

		private String id;
		
		public LockWorker(String id) {
			super();
			this.id = id;
		}

		@Override
		public void run() {
			RedisDistributeLock lock = new RedisDistributeLock("test",60);
			try {				
				lock.lock();
			} catch (Exception e) {
				latch.countDown();
				System.out.println("LockWorker[" + id + "] get lock error->"+e.getMessage());
				return;
			}
			try {Thread.sleep(RandomUtils.nextLong(100, 1000));} catch (Exception e) {}
			lock.unlock();
			latch.countDown();
			System.out.println("LockWorker[" + id + "] release lock,done");
		}
		
	}

}
