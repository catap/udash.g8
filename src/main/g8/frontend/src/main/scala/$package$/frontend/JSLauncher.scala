package $package$.frontend

import io.udash.logging.CrossLogging
import io.udash.wrappers.jquery._
import org.scalajs.dom.Element

import scala.scalajs.js.annotation.JSExport

import scala.util.{Failure, Success}

object JSLauncher extends CrossLogging {
  @JSExport
  def main(args: Array[String]): Unit = {
    jQ((jThis: Element) => {
      // Select #application element from index.html as root of whole app
      val appRoot = jQ("#application").get(0)
      if (appRoot.isEmpty) {
        logger.error("Application root element not found! Check your index.html file!")
      } else {
        import scala.concurrent.ExecutionContext.Implicits.global
        ApplicationContext.workerService.ping("Some message", null) andThen {
          case Success((response, _)) =>
            logger.info(s"Worker response: \$response")
          case Failure(exception) =>
            logger.error(s"Worker failed: \${exception.getMessage}", exception)
        }
        ApplicationContext.application.run(appRoot.get)
      }
    })
  }
}