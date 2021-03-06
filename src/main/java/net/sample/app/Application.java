package net.sample.app;

import java.io.File;
import java.io.IOException;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.deploy.ApplicationListener;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Application {
	
	private final static String ROOT = "/";
	private final static String SERVLET = "appServlet";
	
	private final static Tomcat tomcat = new Tomcat();
	
	private final static Log log = LogFactory.getLog( Application.class );
	
	public static void main(String[] args) throws Exception {
		final File base = createBaseDirectory();
		log.info( "Using base folder: " + base.getAbsolutePath() );
		
		tomcat.setPort(8080);
		tomcat.setBaseDir(".");
		Connector connector = tomcat.getConnector();
		connector.setProtocol("org.apache.coyote.http11.Http11NioProtocol");
		connector.setXpoweredBy(false);
		connector.setAttribute("useComet", false);
		connector.setAttribute("socket.directBuffer", true);
		connector.setAttribute("pollerThreadCount", 2);
		connector.setAttribute("maxThreads", 800);
		connector.setAttribute("processorCache", 800);
		
		Host host = tomcat.getHost();
		host.setAppBase(base.getAbsolutePath());
		host.setAutoDeploy(true);
		host.setDeployOnStartup(true);
		
		//web.config 구현
		Context context = tomcat.addContext(ROOT, base.getAbsolutePath());
		context.setLoader(new WebappLoader(Thread.currentThread().getContextClassLoader()));
		
		//Spring 리스너 및 AppServlet 구성
		context.addApplicationListener(new ApplicationListener(ContextLoaderListener.class.getName(), false));
		tomcat.addServlet(ROOT, SERVLET, new DispatcherServlet(new AnnotationConfigWebApplicationContext(){{
			register(AppConfig.class);
		}})).setLoadOnStartup(1);
		context.addServletMapping("/", SERVLET);
		
		//???
		context.addParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
		
		//톰캣 시작
		tomcat.start();
		tomcat.getServer().await();
	}

	private static File createBaseDirectory() throws IOException {
		final File base = File.createTempFile("tmp-", "");

		if (!base.delete()) {
			throw new IOException("Cannot (re)create base folder: "
					+ base.getAbsolutePath());
		}

		if (!base.mkdir()) {
			throw new IOException("Cannot create base folder: "
					+ base.getAbsolutePath());
		}

		return base;
	}
}
