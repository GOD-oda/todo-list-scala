package routes

import service.TodoService
import repository.TodoRepository
import cats.effect.{IO, Resource}
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import com.comcast.ip4s.{Host, Port, ipv4}
import domain.Todo
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

class TodoRoutes(todoService: TodoService) {

  given EntityDecoder[IO, Todo] = jsonOf[IO, Todo]
  given EntityDecoder[IO, TodoRequest] = jsonOf[IO, TodoRequest]

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "todos" =>
      val todos = todoService.getAllTodos()
      Ok(todos.asJson)

    case GET -> Root / "todos" / id =>
      todoService.getTodoById(id) match {
        case Some(todo) => Ok(todo.asJson)
        case None => NotFound(ApiError(s"Todo with id $id not found").asJson)
      }

    case req @ POST -> Root / "todos" =>
      req.as[TodoRequest].flatMap { todoRequest =>
        val todo = todoService.createTodo(todoRequest.title)
        Created(todo.asJson)
      }.handleErrorWith { error =>
        BadRequest(ApiError(error.getMessage).asJson)
      }

    case req @ PUT -> Root / "todos" / id =>
      req.as[TodoRequest].flatMap { todoRequest =>
        todoService.updateTodo(id = id, title = todoRequest.title) match {
          case Some(todo) => Ok(todo.asJson)
          case None => NotFound(ApiError(s"Todo with id $id not found").asJson)
        }
      }

    case req @ DELETE -> Root / "todos" / id =>
      todoService.getTodoById(id = id) match {
        case None => BadRequest(ApiError(s"Todo with id $id not found").asJson)
        case Some(_) =>
          todoService.deleteTodo(id = id) match {
            case true => NoContent()
            case false => InternalServerError(ApiError(s"Failed to delete the Todo with id $id").asJson)
          }
      }
  }
}

object TodoRoutes {
  def startServer(port: Int = 8080): Resource[IO, Server] = {
    val repository = TodoRepository.inMemory()
    val service = TodoService(repository)
    val todoRoutes = new TodoRoutes(service)

    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("0.0.0.0").get)
      .withPort(Port.fromInt(port).get)
      .withHttpApp(todoRoutes.routes.orNotFound)
      .build
  }
}
