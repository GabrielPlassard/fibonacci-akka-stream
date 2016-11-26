package fr.gplassard.fibonaccistream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global

object App {

  case class Fibonacci(value: Int, previous: Int, previousPrevious: Int) {
    def next() = Fibonacci(value + previous, value, previous)
  }

  object Fibonacci {
    val one = Fibonacci(1, 1, 0)

    val source: Source[Fibonacci, NotUsed] = Source.single(Fibonacci.one)

  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    Fibonacci.source
      .takeWhile(_.value < 50)
      .runForeach(println)
      .onComplete( _ => system.terminate())
  }
}
