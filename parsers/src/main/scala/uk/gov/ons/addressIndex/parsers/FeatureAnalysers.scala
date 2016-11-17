package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfFeatureAnalyser, _}
import uk.gov.ons.addressIndex.parsers.Implicits._
import Tokens._

/**
  * FeatureAnalyser implementations for the AddressParser
  */
object FeatureAnalysers {

  /**
    * Predefined items
    */
  object Predef {

    /**
      * @return all of the predefined features
      */
    def all() : Features = {
      Features(
        Feature[String](digits)(digitsAnalyser()),
        Feature[Boolean](word)(wordAnalyser()),
        Feature[String](length)(lengthAnalyser()),
        Feature[Boolean](endsInPunctuation)(endsInPunctuationAnalyser()),
        Feature[Boolean](directional)(directionalAnalyser()),
        Feature[Boolean](outcode)(outcodeAnalyser()),
        Feature[Boolean](postTown)(postTownAnalyser()),
        Feature[Boolean](hasVowels)(hasVowelsAnalyser()),
        Feature[Boolean](flat)(flatAnalyser()),
        Feature[Boolean](company)(companyAnalyser()),
        Feature[Boolean](road)(roadAnalyser()),
        Feature[Boolean](residential)(residentialAnalyser()),
        Feature[Boolean](business)(businessAnalyser()),
        Feature[Boolean](locational)(locationalAnalyser()),
        Feature[Int](hyphenations)(hyphenationsAnalyser())
      )
    }

    val word : FeatureName = "word"
    /**
      * @return true if the string is all digits, false if not
      */
    def wordAnalyser() : CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](_.allDigits[Boolean](!_))

    val length : FeatureName = "length"
    /**
      * @return the length of the string as a string
      */
    def lengthAnalyser() : CrfFeatureAnalyser[String] = CrfFeatureAnalyser[String](_.length.toString)

    val endsInPunctuation : FeatureName = "endsinpunc"
    /**
      * @return true if the last character of the string is a '.', false if not
      */
    def endsInPunctuationAnalyser() : CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](_.last == '.')

    val hasVowels : FeatureName = "has.vowels"
    /**
      * @return true if the string is in the Tokens.postTown collection, false if not
      */
    def hasVowelsAnalyser() : CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](_.containsVowels[Boolean](identity))

    val directional : FeatureName = "directional"
    /**
      * @return true if the string is in the Tokens.directions collection, false if not
      */
    def directionalAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.directions)

    val outcode : FeatureName = "outcode"
    /**
      * @return true if the string is in the Tokens.outcodes collection, false if not
      */
    def outcodeAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.outcodes)

    val postTown : FeatureName = "posttown"
    /**
      * @return true if the string is in the Tokens.postTown collection, false if not
      */
    def postTownAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.postTown)

    val flat : FeatureName = "flat"
    /**
      * @return true if the string is in the Tokens.flat collection, false if not
      */
    def flatAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.flat)

    val company : FeatureName = "company"
    /**
      * @return true if the string is in the Tokens.company collection, false if not
      */
    def companyAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.company)

    val road : FeatureName = "road"
    /**
      * @return true if the string is in the Tokens.road collection, false if not
      */
    def roadAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.road)

    val residential : FeatureName = "residential"
    /**
      * @return true if the string is in the Tokens.residential collection, false if not
      */
    def residentialAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.residential)

    val business : FeatureName = "business"
    /**
      * @return true if the string is in the Tokens.business collection, false if not
      */
    def businessAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.business)

    val locational : FeatureName = "locational"
    /**
      * @return true if the string is in the Tokens.locational collection, false if not
      */
    def locationalAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.locational)

    val ordinal : FeatureName = "ordinal"
    /**
      * @return true if the string is in the Tokens.ordinal collection, false if not
      */
    def ordinalAnalyser() : CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ordinal)

    val hyphenations : FeatureName = "hyphenations"
    /**
      * @return the count of characters which are '-'
      */
    def hyphenationsAnalyser(): CrfFeatureAnalyser[Int] = CrfFeatureAnalyser[Int](_ count(_ == '-'))

    val digits : FeatureName = "digits"
    /**
      * @return a DigitLiteral String, which indicates if the string has all digits, contains digits or no digits
      */
    def digitsAnalyser() : CrfFeatureAnalyser[String] = {
      import DigitsLiteral._
      CrfFeatureAnalyser[String] { str =>
        str.allDigits[String] { rs =>
          if(rs) {
            allDigits
          } else {
            str.containsDigits[String] { rs =>
              if(rs) {
                containsDigits
              } else {
                noDigits
              }
            }
          }
        }
      }
    }

    /**
      * ref digitsAnalyser
      */
    object DigitsLiteral {
      val allDigits = "all_digits"
      val containsDigits = "contains_digits"
      val noDigits = "no_digits"
    }
  }

  /**
    * Helper FeatureAnalyser implementation
    * Use this analyser for using contains on a Sequence
    *
    * Eg:
    *     ContainsAnalyser(Seq("oneThingToLookFor", "AnotherThingToLookFor"))
    */
  object ContainsAnalyser {
    def apply(tis : Seq[TokenIndicator]) : CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](tis contains _)
  }
}