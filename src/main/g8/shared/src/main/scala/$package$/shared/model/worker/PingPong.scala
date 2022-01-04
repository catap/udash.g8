package $package$.shared.model.worker

import com.avsystem.commons.serialization.HasGenCodec

object PingPong extends WorkerMessage {
  override val method: String = "PingPong"

  case class Request(payload: String)
  object Request extends HasGenCodec[Request]

  case class Response(payload: String)
  object Response extends HasGenCodec[Response]
}
