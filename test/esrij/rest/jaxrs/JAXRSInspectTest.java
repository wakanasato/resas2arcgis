package test.esrij.rest.jaxrs;

import com.esrij.rest.jaxrs.IndexAction;

public class JAXRSInspectTest {

	@Test
	public void test() {
		IndexAction indexAction = new IndexAction();
		assertEquals(3, indexAction.loadList().size());
	}

}