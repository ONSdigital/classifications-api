package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.jni.{CrfScalaJni, CrfScalaJniImpl}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.parsers.Tokens.Token
import scala.util.control.NonFatal

/**
  * scala wrapper of crfsuite
  *
  * todo describe this more
  */
object CrfScala {

  type Input = String
  type FeatureName = String
  type CrfJniInput = String
  type FeaturesResult = Map[FeatureName, _]
  type CrfFeatureAnalyser[T] = (Input => T)
  type CrfParserResults = Seq[CrfParserResult]

  case class CrfParserResult(originalInput: Token, crfLabel: String)

  case class CrfTokenResult(token: Token, results: FeaturesResult) {
    def toCrfJniInput(): CrfJniInput = {
      ""
    }
  }

  object CrfFeatureAnalyser {
    /**
      * Helper apply method for better syntax.
      * Constructs a function.
      * Eg:
      *
      *    FeatureAnalyser[String]("SplitOnSpaceCountAsStr") { str =>
      *       str.split(" ").length.toString
      *    }
      *
      * Or:
      *
      *    FeatureAnalyser[Int]("lengthOfString") { str =>
      *       str.length
      *    }
      *
      */
    def apply[T](analyser : CrfFeatureAnalyser[T]) : CrfFeatureAnalyser[T] = analyser
  }

  //todo scaladoc
  trait CrfType[T] {
    def value : T
  }

  //TODO scaladoc
  trait CrfParser {
    //TODO scaladoc
    def parse(i : Input, fas : CrfFeatures): CrfParserResults = {
      val tokens = Tokens(i)
      val preprocessedTokens = Tokens normalise tokens

      //TODO
      val x = preprocessedTokens map fas.analyse
      x
      val crfJniInput = ""
      val tokenResults = new CrfScalaJniImpl tag crfJniInput split CrfScalaJni.newLine
      tokenResults map { tr => CrfParserResult(tr, tr)}
    }
  }


  /**
    * scala wrapper of third_party.org.chokkan.crfsuite.Item
    */
  trait CrfFeatures {

    /**
      * @return all the features
      */
    def all : Seq[CrfFeature[_]]

    def toCrfJniInput(input: Token, next: Option[Token] = None, previous: Option[Token] = None): CrfJniInput = {
      all map(_.toCrfJniInput(input, next, previous)) mkString CrfScalaJni.lineEnd
    }

    /**
      * @param i the token to run against all feature analysers
      * @return the token and its results, as a pair
      */
    def analyse(i : Token) : CrfTokenResult = {
      CrfTokenResult(
        token = i,
        results = all.map(f => f.name -> f.analyse(i)).toMap
      )
    }
  }

  /**
    * scala wrapper of third_party.org.chokkan.crfsuite.Attribute
    *
    * @tparam T the return type of the FeatureAnalyser
    */
  trait CrfFeature[T] {

    /**
      * @return a function which returns an instance of T
      */
    def analyser() : CrfFeatureAnalyser[T]

    /**
      * @return name
      */
    def name() : String

    /**
      * @param i input
      * @return apply the analyser to i
      */
    def analyse(i : Input) : T = analyser apply i

    //TODO scaladoc
    /**
      *
      * @param input
      * @param next
      * @param previous
      * @return
      */
    def toCrfJniInput(input: Token, next: Option[Token] = None, previous: Option[Token] = None): CrfJniInput = {
      new StringBuilder()
        .append(CrfScalaJni.lineStart)
        .append(
          createCrfJniInput(
            prefix = name,
            someValue = analyse(input)
          )
        )
        .append(
          next map { next =>
            createCrfJniInput(
              prefix = CrfScalaJni.next,
              someValue = analyse(next)
            )
          } getOrElse ""
        )
        .append(
          previous map { previous =>
            createCrfJniInput(
              prefix = CrfScalaJni.previous,
              someValue = analyse(previous)
            )
          } getOrElse ""
        )
        .append(CrfScalaJni.lineEnd)
        .toString
    }

    //TODO scaladoc
    /**
      *
      * @param prefix
      * @param someValue
      * @return
      */
    def createCrfJniInput(prefix: String, someValue: Any): CrfJniInput = {
      def qualify(str: String): String = str.replace(":", "\\:")

      val qName = qualify(name)
      someValue match {
        case _: String =>
          s"$qName\\:${qualify(someValue.asInstanceOf[String])}:1.0"

        case _: Int =>
          s"$qName:$someValue.0"

        case _: Double =>
          s"$qName:$someValue"

        case _: Boolean =>
          s"$qName:${if(someValue.asInstanceOf[Boolean]) "1.0" else "0.0"}"

        case t : CrfType[_] =>
          createCrfJniInput(prefix, t.value)

        case NonFatal(e) =>
          throw e

        case _ =>
          throw new UnsupportedOperationException(
            s"Unsupported input to CrfJniInput: ${someValue.getClass.toString} or Feature with name: $name"
          )
      }
    }
  }
}