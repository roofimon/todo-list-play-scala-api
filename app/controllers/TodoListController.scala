package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import models._

@Singleton
class TodoListController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  //New code
  private val todoList = new ListBuffer[TodoItem]()
  todoList += TodoItem(1, "Buy new laptop", true)
  todoList += TodoItem(2, "Call mom", false)
  //End new code
  implicit val todoItemJsonFormat = Json.format[TodoItem]
  implicit val newTodoItemJsonFormat = Json.format[NewTodoItem]

  def done(itemId: Int) = Action {
    val result = todoList.find(_.id == itemId)
    result match {
      case Some(item) =>
        val newItem = item.copy(status = true)
        todoList.dropWhileInPlace(_.id == itemId)
        todoList += newItem
        Accepted(Json.toJson(newItem))
      case None => NotFound
    }
  }

  def newItem() = Action { implicit request =>
    val jsonObject = request.body.asJson
    val newTodoItem: Option[NewTodoItem] =
      jsonObject.flatMap(Json.fromJson[NewTodoItem](_).asOpt)

    newTodoItem match {
      case Some(newItem) =>
        val nextId = todoList.map(_.id).max + 1
        val toBeAdded = TodoItem(nextId, newItem.description, false)
        todoList += toBeAdded
        Created(Json.toJson(toBeAdded))
      case None =>
        BadRequest
    }
  }

  def byId(itemId: Int) = Action {
    val result = todoList.find(_.id == itemId)
    result match {
      case Some(item) => Ok(Json.toJson(item))
      case None       => NotFound
    }
  }

  def all() = Action {
    if (todoList.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(todoList))
    }
  }
}
