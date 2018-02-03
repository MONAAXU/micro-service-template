import java.io.{File, FileInputStream}
import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.slf4j.LoggerFactory


object WebServer extends JsonSupport with AppInfoService with LazyLogging{
  def main(args: Array[String]) {

    val prop = new Properties();
    prop.load(new FileInputStream(new File(System.getProperty("user.dir")+"/src/main/scala/catalina.properties")));

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatcher

    val route = appInfoServiceRoute
    val host = prop.getProperty("host","0.0.0.0");
    val port = Integer.valueOf(prop.getProperty("port","8080"));

    val bindingFuture = Http().bindAndHandle(route, host, port)

    val logger = Logger(LoggerFactory.getLogger("my-logger"))
    logger.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  }
}