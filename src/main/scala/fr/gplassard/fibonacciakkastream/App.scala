package fr.gplassard.fibonacciakkastream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, MergePreferred, Source}
import akka.stream.{ActorMaterializer, SourceShape}

import scala.concurrent.ExecutionContext.Implicits.global

object App {

  case class Fibonacci(value: Int, previous: Int, previousPrevious: Int) {
    def next() = Fibonacci(value + previous, value, previous)
  }

  object Fibonacci {
    val one = Fibonacci(1, 1, 0)

    val source: Source[Fibonacci, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val seed = Source.single(Fibonacci.one)

      val next = Flow[Fibonacci]
        .map(_.next())

      val bcast = builder.add(Broadcast[Fibonacci](2))
      val merge = builder.add(MergePreferred[Fibonacci](1))


      seed ~> merge           ~>         bcast
              merge.preferred <~ next <~ bcast

      SourceShape(bcast.out(1))
    })
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    Fibonacci.source
      .takeWhile(_.value < 500)
      .runForeach(println)
      .onComplete(_ => system.terminate())
  }
}
