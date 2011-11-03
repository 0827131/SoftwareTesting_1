package at.ticketline.dao.jpa;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import at.ticketline.dao.AbstractDaoTest;
import at.ticketline.dao.interfaces.AuffuehrungDao;
import at.ticketline.dao.interfaces.VeranstaltungDao;

public class DaoTest extends AbstractDaoTest {

	
	@Autowired
	private VeranstaltungDao veranstaltungDao;
	@Autowired
	private AuffuehrungDao auffuehrungDao;
	
	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("Before Transaction");
	}
	
	@AfterTransaction
	public void afterTransaction() {
		System.out.println("After Transaction");
	}
	
	@Before
	public void loadData() {
		// Load data for test execution
	}
	
	@Test
	public void simpleTest() {
		Assert.assertEquals(4, 2 + 2);
		Assert.assertNotNull(this.veranstaltungDao);
		Assert.assertEquals(this.countRowsInTable("veranstaltung"), 3);
		
		Assert.assertEquals(this.veranstaltungDao.findAll().size(), 3);
		
		//Example how to call findBest(ID) method
		//List<Platz> bestplatz = this.auffuehrungDao.findBest(1);
		
	}
	
}
