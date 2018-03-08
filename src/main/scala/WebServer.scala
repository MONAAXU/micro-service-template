import java.io.{File, FileInputStream, InputStream}
import java.security.{KeyStore, SecureRandom}
import java.util.Properties
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.ActorSystem
import akka.http.scaladsl.{ Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.slf4j.LoggerFactory


object WebServer extends JsonSupport with AppInfoService with LazyLogging{
  def main(args: Array[String]) {

    val prop = new Properties();
    prop.load(new FileInputStream(new File(System.getProperty("user.dir")+"/src/main/resources/catalina.properties")));

    val host = prop.getProperty("host","0.0.0.0");
    val port = prop.getProperty("port","8080").toInt;
    val akkaSystemName = prop.getProperty("akka-system-name");
    val loggerName = prop.getProperty("logger-name")
    val zookeeperEnabled = prop.getProperty("zookeeper-enabled").toBoolean

    if (zookeeperEnabled) {
      ZookeeperClient.init();
    }

    implicit val system = ActorSystem(akkaSystemName)
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatcher

    val route = appInfoServiceRoute

    val https: HttpsConnectionContext = new HttpsConnectionContext(
      sslContext = createSSLContext()
    )

    Http().setDefaultServerHttpContext(https)
    Http().bindAndHandle(route, host, 9090,connectionContext = https)
    Http().bindAndHandle(route, host, port)


    val logger = Logger(LoggerFactory.getLogger(loggerName))
    logger.info(s"Server online at http://localhost:8080/\nPress Ctrl+C to stop...")

  }
  def createSSLContext(): SSLContext ={
    val password: Array[Char] = "123456789".toCharArray // do not store passwords in code, read them from somewhere safe!

    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    val keystore: InputStream = new FileInputStream(new File(System.getProperty("user.dir")+"/scripts/localhost.p12"))

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom())
    sslContext
  }

}
