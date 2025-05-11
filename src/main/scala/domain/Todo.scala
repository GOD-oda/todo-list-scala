package domain

import java.util.UUID

final case class Todo(id: String, title: String) {
  def toJson: String = s"""{"id":"$id","title":"$title"}"""
}

object Todo {
  def apply(title: String): Todo = Todo(UUID.randomUUID().toString, title)

  def toJsonArray(todos: List[Todo]): String = {
    val todoJsons = todos.map(_.toJson).mkString(",")
    s"[$todoJsons]"
  }
}
