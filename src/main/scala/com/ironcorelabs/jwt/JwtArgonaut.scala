package com.ironcorelabs.jwt

import argonaut._, Argonaut._

import pdi.jwt.exceptions.JwtNonStringException
import pdi.jwt.{JwtJsonCommon, JwtClaim, JwtAlgorithm, JwtHeader}

/**
 * Implementation of `JwtCore` using `Json` from Argonaut.
 */
object JwtArgonaut extends JwtJsonCommon[Json, JwtHeader, JwtClaim] {
  protected def parse(value: String): Json = Parse.parseOption(value).get
  protected def parseClaim(claim: String): JwtClaim = {
    val cursor = parse(claim).hcursor
    val contentCursor = List("iss", "sub", "aud", "exp", "nbf", "iat", "jti").foldLeft(cursor) { (cursor, field) =>
      cursor.downField(field).delete.success match {
        case Some(newCursor) => newCursor
        case None => cursor
      }
    }
    JwtClaim(
      content = contentCursor.asJson.nospaces,
      issuer = cursor.get[String]("iss").toOption,
      subject = cursor.get[String]("sub").toOption,
      audience = (cursor.get[Set[String]]("aud") ||| (cursor.get[String]("aud").map(s => Set(s)))).toOption,
      expiration = cursor.get[Long]("exp").toOption,
      notBefore = cursor.get[Long]("nbf").toOption,
      issuedAt = cursor.get[Long]("iat").toOption,
      jwtId = cursor.get[String]("jti").toOption
    )
  }

  protected def stringify(value: Json): String = value.asJson.nospaces

  private def getAlg(cursor: HCursor): Option[JwtAlgorithm] = {
    cursor.get[String]("alg").toOption.flatMap {
      case "none" => None
      case s: String => for {
        nonNullString <- Option(s)
        result <- Option(JwtAlgorithm.fromString(s))
      } yield result
      case _ => throw new JwtNonStringException("alg")
    }
  }

  protected def parseHeader(header: String): JwtHeader = {
    val cursor = parse(header).hcursor
    JwtHeader(
      algorithm = getAlg(cursor),
      typ = cursor.get[String]("typ").toOption,
      contentType = cursor.get[String]("cty").toOption
    )
  }
  protected def getAlgorithm(header: Json): Option[JwtAlgorithm] = getAlg(header.hcursor)
}
