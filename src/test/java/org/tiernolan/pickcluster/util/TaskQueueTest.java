package org.tiernolan.pickcluster.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TaskQueueTest {

	@Test
	public void test() {
		TaskQueue queue = new TaskQueue();

		Runnable r = new Runnable() {
			@Override
			public void run() {
			}
		};

		assertTrue(queue.add(r));

		assertEquals(1, queue.size());

		assertTrue(queue.add(r));

		assertEquals(2, queue.size());
	}

}
