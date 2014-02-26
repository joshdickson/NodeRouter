package noderouter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class RouterTest {

	/**
	 * Test router launches subprocess
	 * @throws IOException 
	 */
	@Test
	public void testRouterStarts() throws IOException {
		Router router = new Router("testproxy.js");
		assertNotNull(router);
	}
	
	/**
	 * Test opening and closing the router
	 * @throws IOException
	 */
	@Test
	public void testRepeatOpensAndCloses() throws IOException {
		Router router = new Router("testproxy.js");
		router.bind();
		router.close();
	}
	
	/**
	 * Test that the router returns an echo call
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testEchoToRouterReturnsBack() throws IOException, InterruptedException {
		Router router = new Router("testproxy.js");
		router.bind();
		
		// Write several messages to the router
		router.send("Test1");
		router.send("Test2");
		
		// Allow the messages to move through the router
		TimeUnit.MILLISECONDS.sleep(1000);
		
		// assert that there's received data
		assertEquals(router.getQueue().size(), 2);
		
		assertEquals(router.getQueue().take(), "Test1");
		assertEquals(router.getQueue().take(), "Test2");
		
		router.close();
	}

}
