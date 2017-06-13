package cn.itcast.ssh.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.itcast.ssh.utils.DynamicBpmn;

public class TestCase {

	@Test
	public void testContext(){
		ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
		System.out.println(ac);
		DynamicBpmn db = ac.getBean("dynamicBpmn", DynamicBpmn.class);
		try {
			db.test();
			System.out.println("success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
