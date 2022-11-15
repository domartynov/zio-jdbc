/*
 * Copyright 2022 John A. De Goes and the ZIO Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zio.jdbc

import zio._

/**
 * Configuration data for a connection pool.
 */
final case class ZConnectionPoolConfig(
  minConnections: Int,
  maxConnections: Int,
  retryPolicy: Schedule[Any, Throwable, Any],
  timeToLive: Duration
)
object ZConnectionPoolConfig {
  import zio.schema._
  import zio.config._

  import Schema.Field

  lazy val default: ZConnectionPoolConfig = ZConnectionPoolConfig(8, 32, defaultRetryPolicy, 300.seconds)

  lazy val defaultRetryPolicy: Schedule.WithState[Long, Any, Any, Duration] = Schedule.exponential(10.millis)

  implicit val configDescriptor: ConfigDescriptor[ZConnectionPoolConfig] =
    (ConfigDescriptor.int("minConnections") zip
      ConfigDescriptor.int("maxConnections") zip
      ConfigDescriptor.zioDuration("timeToLive")).transform(
      { case (min, max, ttl) => ZConnectionPoolConfig(min, max, defaultRetryPolicy, ttl) },
      cfg => (cfg.minConnections, cfg.maxConnections, cfg.timeToLive)
    )

  implicit val schema: Schema.CaseClass3[Int, Int, Duration, ZConnectionPoolConfig] =
    Schema.CaseClass3[Int, Int, Duration, ZConnectionPoolConfig](
      TypeId.parse(classOf[ZConnectionPoolConfig].getName),
      Field("minConnections", Schema[Int]),
      Field("maxConnections", Schema[Int]),
      Field("timeToLive", Schema.Primitive(StandardType.DurationType)),
      (min, max, ttl) => ZConnectionPoolConfig(min, max, defaultRetryPolicy, ttl),
      _.minConnections,
      _.maxConnections,
      _.timeToLive
    )
}
