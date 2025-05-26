package ru.dru

import zio.CanFail.canFailAmbiguous1
import zio.{Duration, Exit, Fiber, Scope, ZIO, ZIOApp, ZIOAppArgs, ZIOAppDefault, durationInt}

import java.time.LocalDateTime
import scala.concurrent.TimeoutException

case class SaladInfoTime(tomatoTime: Duration, cucumberTime: Duration)


object Breakfast extends ZIOAppDefault {

  /**
   * Функция должна эмулировать приготовление завтрака. Продолжительные операции необходимо эмулировать через ZIO.sleep.
   * Правила приготовления следующие:
   *  1. Нобходимо вскипятить воду (время кипячения waterBoilingTime)
   *  2. Параллельно с этим нужно жарить яичницу eggsFiringTime
   *  3. Параллельно с этим готовим салат:
   *    * сначала режим  огурцы
   *    * после этого режим помидоры
   *    * после этого добавляем в салат сметану
   *  4. После того, как закипит вода необходимо заварить чай, время заваривания чая teaBrewingTime
   *  5. После того, как всё готово, можно завтракать
   *
   * @param eggsFiringTime время жарки яичницы
   * @param waterBoilingTime время кипячения воды
   * @param saladInfoTime информация о времени для приготовления салата
   * @param teaBrewingTime время заваривания чая
   * @return Мапу с информацией о том, когда завершился очередной этап (eggs, water, saladWithSourCream, tea)
   */
  def makeBreakfast(
                     eggsFiringTime: Duration,
                     waterBoilingTime: Duration,
                     saladInfoTime: SaladInfoTime,
                     teaBrewingTime: Duration
                   ): ZIO[Any, Throwable, Map[String, LocalDateTime]] = {
    for {
      startTime <- ZIO.succeed(LocalDateTime.now)

      // Start all parallel processes
      waterFiber <- ZIO.sleep(waterBoilingTime).as("water").fork
      eggsFiber <- ZIO.sleep(eggsFiringTime).as("eggs").fork

      // Prepare salad sequentially
      saladFiber <- (for {
        _ <- ZIO.sleep(saladInfoTime.cucumberTime)
        _ <- ZIO.sleep(saladInfoTime.tomatoTime)
      } yield "saladWithSourCream").fork

      // Wait for water to boil before making tea
      teaFiber <- (waterFiber.join *> ZIO.sleep(teaBrewingTime)).as("tea").fork

      // Wait for all to complete
      eggs <- eggsFiber.join
      eggsTime <- ZIO.succeed(LocalDateTime.now)

      water <- waterFiber.join
      waterTime <- ZIO.succeed(LocalDateTime.now)

      salad <- saladFiber.join
      saladTime <- ZIO.succeed(LocalDateTime.now)

      tea <- teaFiber.join
      teaTime <- ZIO.succeed(LocalDateTime.now)
    } yield Map(
      "eggs" -> eggsTime,
      "water" -> waterTime,
      "saladWithSourCream" -> saladTime,
      "tea" -> teaTime
    )
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    makeBreakfast(
      eggsFiringTime = 5.seconds,
      waterBoilingTime = 3.seconds,
      saladInfoTime = SaladInfoTime(tomatoTime = 2.seconds, cucumberTime = 1.seconds),
      teaBrewingTime = 1.seconds
    ).flatMap(result => ZIO.succeed(println(result)))
}
