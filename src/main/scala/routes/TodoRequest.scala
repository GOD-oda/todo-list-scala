package routes

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, HCursor, DecodingFailure}

final case class TodoRequest(title: String)

object TodoRequest {
  given Encoder[TodoRequest] = deriveEncoder[TodoRequest]

  // TODO: 動いてないっぽい
  given Decoder[TodoRequest] = new Decoder[TodoRequest] {
    final def apply(c: HCursor): Decoder.Result[TodoRequest] = 
      c.downField("title").as[String] match {
        case Right(title) => Right(TodoRequest(title))
        case Left(_) => Left(DecodingFailure("Missing required field: title", c.history))
      }
  }
}
