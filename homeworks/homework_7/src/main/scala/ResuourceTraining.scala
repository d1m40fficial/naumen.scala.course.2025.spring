package ru.dru

import zio.{IO, Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}


/**
 * Необходимо реализовать функции readData и writeData, записывающие и читающие данные в/из файла соответственно.
 * В реализации следует применять безопасное использование ресурсов ZIO.acquireReleaseWith
 */


object ResuourceTraining extends ZIOAppDefault {

  def readData(filePath: String): IO[Throwable, String] =
    ZIO.acquireReleaseWith(
      ZIO.attempt(new BufferedReader(new FileReader(filePath)))
    ) { reader =>
      ZIO.succeed(reader.close())
    } { reader =>
      ZIO.attempt {
        val content = new StringBuilder
        var line: String = null
        while ({
          line = reader.readLine()
          line != null
        }) {
          content.append(line).append("\n")
        }
        content.toString()
      }
    }

  def writeData(filePath: String, data: String): ZIO[Any, Nothing, Unit] =
    ZIO.acquireReleaseWith(
      ZIO.attempt(new BufferedWriter(new FileWriter(filePath))).catchAll(_ => ZIO.succeed(null))
    ) { writer =>
      ZIO.succeed(if (writer != null) writer.close())
    } { writer =>
      if (writer != null) {
        ZIO.attempt(writer.write(data)).catchAll(_ => ZIO.unit)
      } else {
        ZIO.unit
      }
    }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = ZIO.succeed("Done")
}
