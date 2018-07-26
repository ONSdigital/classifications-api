package uk.gov.ons.addressIndex.server.utils.impl

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import uk.gov.ons.addressIndex.server.modules.ConfigModule
import uk.gov.ons.addressIndex.server.utils.Overload

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.concurrent.duration._

@Singleton
class OverloadProtector @Inject()(conf: ConfigModule)(implicit ec: ExecutionContext) extends Overload {
  private val logger = Logger("address-index-server:OverloadProtector")

  private val esConf = conf.config.elasticSearch
  private val system: ActorSystem = ActorSystem("ONS")
  private val circuitBreakerMaxFailures: Int = esConf.circuitBreakerMaxFailures
  private val circuitBreakerCallTimeout: Int = esConf.circuitBreakerCallTimeout
  private val circuitBreakerResetTimeout: Int = esConf.circuitBreakerResetTimeout

  val breaker: CircuitBreaker =
    new CircuitBreaker(
      system.scheduler,
      maxFailures = circuitBreakerMaxFailures,
      callTimeout = circuitBreakerCallTimeout.milliseconds,
      resetTimeout = circuitBreakerResetTimeout milliseconds)
      .onOpen(logger.warn("Circuit breaker is now open"))
      .onClose(logger.warn("circuit breaker is now closed"))
      .onHalfOpen(logger.warn("Circuit breaker is now half-open"))
}
