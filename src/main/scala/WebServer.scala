import java.io.{File, FileInputStream, InputStream}
import java.security.{KeyStore, SecureRandom}
import java.util.Properties
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.{LazyLogging, Logger}
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import org.slf4j.LoggerFactory

import scala.collection.immutable.Seq


object WebServer extends JsonSupport with AppInfoService with LazyLogging{
  def main(args: Array[String]) {

    val prop = new Properties();
    prop.load(new FileInputStream(new File(System.getProperty("user.dir")+"/src/main/resources/catalina.properties")));

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatcher

    val route = appInfoServiceRoute
    val host = prop.getProperty("host","127.0.0.1");
    val port = Integer.valueOf(prop.getProperty("port","8080"));

    val https: HttpsConnectionContext = new HttpsConnectionContext(
      sslContext = createSSLContext()
    )

    Http().setDefaultServerHttpContext(https)
    Http().bindAndHandle(route, host, 9090,connectionContext = https)
    Http().bindAndHandle(route, host, port)


    val logger = Logger(LoggerFactory.getLogger("my-logger"))
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
