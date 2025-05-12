package domain

import java.util.UUID
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}

final case class Todo(id: String, title: String)

object Todo {
  def apply(title: String): Todo = Todo(UUID.randomUUID().toString, title)

  // Circe encoders and decoders for Scala 3
  given Encoder[Todo] = deriveEncoder[Todo]
  given Decoder[Todo] = deriveDecoder[Todo]
}
