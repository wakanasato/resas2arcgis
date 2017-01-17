
package test.esrij.rest.jaxrs;

import static org.junit.Assert.*;

import org.junit.Test;

import com.esrij.rest.jaxrs.IndexAction;

public class JAXRSInspectTest {

	@Test
	public void test() {
		IndexAction indexAction = new IndexAction();
		assertEquals(3, indexAction.loadList().size());
	}

}