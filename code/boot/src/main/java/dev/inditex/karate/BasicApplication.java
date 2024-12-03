package dev.inditex.karate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.lifecycle.DockerComposeServicesReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * The Class BasicApplication.
 */
@SpringBootApplication(exclude = {
    MongoAutoConfiguration.class
})
public class BasicApplication {

  /** The logger. */
  protected static Logger logger = LoggerFactory.getLogger(BasicApplication.class);

  /** The running services. */
  protected static List<RunningService> runningServices;

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    final SpringApplication application = new SpringApplication(BasicApplication.class);
    application.addListeners(new DockerComposeServicesReadyEventListener());
    application.run(args);
  }

  /**
   * Gets the running services.
   *
   * @return the running services
   */
  public static List<RunningService> getRunningServices() {
    return runningServices;
  }

  /**
   * Sets the running services.
   *
   * @param runningServices the new running services
   */
  public static void setRunningServices(final List<RunningService> runningServices) {
    BasicApplication.runningServices = runningServices;
  }

  /**
   * The listener interface for receiving dockerComposeServicesReadyEvent events.
   * The class that is interested in processing a dockerComposeServicesReadyEvent
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addDockerComposeServicesReadyEventListener</code> method. When
   * the dockerComposeServicesReadyEvent event occurs, that object's appropriate
   * method is invoked.
   */
  public static class DockerComposeServicesReadyEventListener implements ApplicationListener<DockerComposeServicesReadyEvent> {

    /**
     * Instantiates a new docker compose services ready event listener.
     */
    public DockerComposeServicesReadyEventListener() {
      logger.info("DockerComposeServicesReadyEventListener()");
    }

    /**
     * On application event.
     *
     * @param event the event
     */
    @Override
    public void onApplicationEvent(final DockerComposeServicesReadyEvent event) {
      logger.info("DockerComposeServicesReadyEventListener.onApplicationEvent[{}] ", event);
      BasicApplication.setRunningServices(event.getRunningServices());
    }
  }
}
