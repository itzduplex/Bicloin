package pt.tecnico.rec;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	protected static RecFrontend frontend;
	protected int seq;
	
	@BeforeAll
	public static void oneTimeSetup () throws IOException, ZKNamingException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			frontend = new RecFrontend(testProps.getProperty("server.host"), testProps.getProperty("server.port"), testProps.getProperty("server.path"));
			System.out.println("Test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
	}
	
	@AfterAll
	public static void cleanup() {
		frontend.shutdown();
	}

}
