package routes

import service.TodoService
import repository.TodoRepository
import domain.Todo

import java.net.{ServerSocket, Socket}
import java.io.{BufferedReader, IOException, InputStreamReader, PrintWriter}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TodoSocketHandler(todoService: TodoService) {
  def handleClient(clientSocket: Socket): Unit = {
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    val out = new PrintWriter(clientSocket.getOutputStream, true)

    try {
      val requestLine = in.readLine()
      if (requestLine != null) {
        // NOTE: フレームワークならいい感じにやってくれそうな処理
        val parts = requestLine.split(" ")
        if (parts.length >= 2) {
          val method = parts(0)
          val path = parts(1)

          var line = in.readLine()
          while (line != null && line.nonEmpty) {
            line = in.readLine()
          }

          try {
            handle(out, method, path)
          } catch {
            case e: Exception =>
              e.printStackTrace()
              sendResponse(out, 500, s"""{"error":"Internal server error: ${e.getMessage}"}""")
          }
        }
      }
    } catch {
      case e: IOException =>
        println(s"Error handling client: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      try {
        clientSocket.close()
      } catch {
        case _: IOException =>
      }
    }
  }

  private def handle(out: PrintWriter, method: String, path: String): Unit = {
    method match {
      case "GET" =>
        path match {
          case "/todos" =>
            val todos = todoService.getAllTodos()
            val response = Todo.toJsonArray(todos)
            sendResponse(out, 200, response)
          case p if p.startsWith("/todos/") =>
            val id = p.substring("/todos/".length)
            todoService.getTodoById(id) match {
              case Some(todo) =>
                sendResponse(out, 200, todo.toJson)
              case None =>
                sendResponse(out, 404, s"""{"error":"Todo with id $id not found"}""")
            }
          case _ =>
            sendResponse(out, 405, """{"error":"Method not allowed"}""")
        }
      case "POST" =>
        path match {
          case "/todos" =>
            val todo = todoService.createTodo("New Todo")
            sendResponse(out, 201, todo.toJson)
        }
      case _ =>
        sendResponse(out, 405, """{"error":"Method not allowed"}""")
    }
  }
  
  private def sendResponse(out: PrintWriter, statusCode: Int, body: String): Unit = {
    val statusText = statusCode match {
      case 200 => "OK"
      case 201 => "Created"
      case 404 => "Not Found"
      case 405 => "Method Not Allowed"
      case 500 => "Internal Server Error"
      case _ => "Unknown Status"
    }
    
    out.println(s"HTTP/1.1 $statusCode $statusText")
    out.println("Content-Type: application/json")
    out.println(s"Content-Length: ${body.getBytes.length}")
    out.println()
    out.println(body)
    out.flush()
  }
}

// NOTE: 一般的にはルーティングをまとめて処理する層になるはずでTodoRoutesと命名しないはず？
object TodoRoutes {
  def startServer(port: Int = 8080): ServerSocket = {
    val repository = TodoRepository.inMemory()
    val service = TodoService(repository)
    val handler = new TodoSocketHandler(service)

    val serverSocket = new ServerSocket(port)

    val serverThread = new Thread(() => {
      while (!serverSocket.isClosed) {
        try {
          val clientSocket = serverSocket.accept()
          val clientThread = new Thread(() => handler.handleClient(clientSocket))
          clientThread.setDaemon(true)
          clientThread.start()
        } catch {
          case e: IOException if !serverSocket.isClosed =>
            println(s"Error accepting client connection: ${e.getMessage}")
            e.printStackTrace()
          case _: IOException =>
        }
      }
    })
    serverThread.setDaemon(true)
    serverThread.start()

    serverSocket
  }
}
