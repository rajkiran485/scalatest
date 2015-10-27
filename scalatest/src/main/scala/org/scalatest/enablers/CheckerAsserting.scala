/*
 * Copyright 2001-2013 Artima, Inc.
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
package org.scalatest.enablers

import org.scalatest.Assertion
import org.scalatest.Succeeded
import org.scalatest.Resources
import org.scalatest.UnquotedString
import org.scalatest.FailureMessages
import FailureMessages.decorateToStringValue
import org.scalacheck.util.Pretty
import org.scalacheck.Prop.Arg
import org.scalacheck.Prop
import org.scalacheck.Test
import org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException
import org.scalatest.exceptions.StackDepthExceptionHelper.getStackDepthFun
import org.scalatest.exceptions.StackDepth

trait CheckerAsserting[T] {
  type Result
  val Singleton: Result

  def doCheck(p: Prop, prms: Test.Parameters, stackDepthFileName: String, stackDepthMethodName: String, argNames: Option[List[String]] = None): Result
}

abstract class LowPriorityCheckerAsserting {

  implicit def assertingNatureOfT[T]: CheckerAsserting[T] { type Result = Unit } = {
    new CheckerAsserting[T] {
      type Result = Unit
      val Singleton = ()

      import CheckerAsserting._

      def doCheck(p: Prop, prms: Test.Parameters, stackDepthFileName: String, stackDepthMethodName: String, argNames: Option[List[String]] = None): Result = {

        val result = Test.check(prms, p)
        if (!result.passed) {

          val (args, labels) = argsAndLabels(result)

          (result.status: @unchecked) match {

            case Test.Exhausted =>

              val failureMsg =
                if (result.succeeded == 1)
                  FailureMessages.propCheckExhaustedAfterOne(result.discarded)
                else
                  FailureMessages.propCheckExhausted(result.succeeded, result.discarded)

              throw new GeneratorDrivenPropertyCheckFailedException(
                sde => failureMsg,
                None,
                getStackDepthFun(stackDepthFileName, stackDepthMethodName),
                // getStackDepth("ScalaCheck.scala", "check"),
                // { val x = getStackDepth("GeneratorDrivenPropertyChecks$class.scala", "forAll"); println("stackDepth:" + x); x},
                None,
                failureMsg,
                args,
                None,
                labels
              )

            case Test.Failed(scalaCheckArgs, scalaCheckLabels) =>

              // SKIP-SCALATESTJS-START
              val stackDepth = 0
              // SKIP-SCALATESTJS-END
              //SCALATESTJS-ONLY val stackDepth = 1

              throw new GeneratorDrivenPropertyCheckFailedException(
                sde => FailureMessages.propertyException(UnquotedString(sde.getClass.getSimpleName)) + "\n" +
                  ( sde.failedCodeFileNameAndLineNumberString match { case Some(s) => " (" + s + ")"; case None => "" }) + "\n" +
                  "  " + FailureMessages.propertyFailed(result.succeeded) + "\n" +
                  (
                    sde match {
                      case sd: StackDepth if sd.failedCodeFileNameAndLineNumberString.isDefined =>
                        "  " + FailureMessages.thrownExceptionsLocation(UnquotedString(sd.failedCodeFileNameAndLineNumberString.get)) + "\n"
                      case _ => ""
                    }
                    ) +
                  "  " + FailureMessages.occurredOnValues + "\n" +
                  prettyArgs(getArgsWithSpecifiedNames(argNames, scalaCheckArgs)) + "\n" +
                  "  )" +
                  getLabelDisplay(scalaCheckLabels),
                None,
                getStackDepthFun(stackDepthFileName, stackDepthMethodName, stackDepth),
                None,
                FailureMessages.propertyFailed(result.succeeded),
                scalaCheckArgs,
                None,
                scalaCheckLabels.toList
              )

            case Test.PropException(scalaCheckArgs, e, scalaCheckLabels) =>

              throw new GeneratorDrivenPropertyCheckFailedException(
                sde => FailureMessages.propertyException(UnquotedString(e.getClass.getSimpleName)) + "\n" +
                  "  " + FailureMessages.thrownExceptionsMessage(if (e.getMessage == null) "None" else UnquotedString(e.getMessage)) + "\n" +
                  (
                    e match {
                      case sd: StackDepth if sd.failedCodeFileNameAndLineNumberString.isDefined =>
                        "  " + FailureMessages.thrownExceptionsLocation(UnquotedString(sd.failedCodeFileNameAndLineNumberString.get)) + "\n"
                      case _ => ""
                    }
                    ) +
                  "  " + FailureMessages.occurredOnValues + "\n" +
                  prettyArgs(getArgsWithSpecifiedNames(argNames, scalaCheckArgs)) + "\n" +
                  "  )" +
                  getLabelDisplay(scalaCheckLabels),
                Some(e),
                getStackDepthFun(stackDepthFileName, stackDepthMethodName),
                None,
                FailureMessages.propertyException(UnquotedString(e.getClass.getName)),
                scalaCheckArgs,
                None,
                scalaCheckLabels.toList
              )
          }
        }
        //else asserting.Singleton
      }
    }
  }
}

abstract class MediumPriorityCheckerAsserting extends LowPriorityCheckerAsserting {
  implicit def assertingNatureOfAssertion: CheckerAsserting[Assertion] { type Result = Assertion } = {
    new CheckerAsserting[Assertion] {
      type Result = Assertion
      val Singleton = Succeeded

      import CheckerAsserting._

      def doCheck(p: Prop, prms: Test.Parameters, stackDepthFileName: String, stackDepthMethodName: String, argNames: Option[List[String]] = None): Result = {

        val result = Test.check(prms, p)
        if (!result.passed) {

          val (args, labels) = argsAndLabels(result)

          (result.status: @unchecked) match {

            case Test.Exhausted =>

              val failureMsg =
                if (result.succeeded == 1)
                  FailureMessages.propCheckExhaustedAfterOne(result.discarded)
                else
                  FailureMessages.propCheckExhausted(result.succeeded, result.discarded)

              throw new GeneratorDrivenPropertyCheckFailedException(
                sde => failureMsg,
                None,
                getStackDepthFun(stackDepthFileName, stackDepthMethodName),
                // getStackDepth("ScalaCheck.scala", "check"),
                // { val x = getStackDepth("GeneratorDrivenPropertyChecks$class.scala", "forAll"); println("stackDepth:" + x); x},
                None,
                failureMsg,
                args,
                None,
                labels
              )

            case Test.Failed(scalaCheckArgs, scalaCheckLabels) =>

              // SKIP-SCALATESTJS-START
              val stackDepth = 2
              // SKIP-SCALATESTJS-END
              //SCALATESTJS-ONLY val stackDepth = 1

              throw new GeneratorDrivenPropertyCheckFailedException(
                sde => FailureMessages.propertyException(UnquotedString(sde.getClass.getSimpleName)) + "\n" +
                  ( sde.failedCodeFileNameAndLineNumberString match { case Some(s) => " (" + s + ")"; case None => "" }) + "\n" +
                  "  " + FailureMessages.propertyFailed(result.succeeded) + "\n" +
                  (
                    sde match {
                      case sd: StackDepth if sd.failedCodeFileNameAndLineNumberString.isDefined =>
                        "  " + FailureMessages.thrownExceptionsLocation(UnquotedString(sd.failedCodeFileNameAndLineNumberString.get)) + "\n"
                      case _ => ""
                    }
                    ) +
                  "  " + FailureMessages.occurredOnValues + "\n" +
                  prettyArgs(getArgsWithSpecifiedNames(argNames, scalaCheckArgs)) + "\n" +
                  "  )" +
                  getLabelDisplay(scalaCheckLabels),
                None,
                getStackDepthFun(stackDepthFileName, stackDepthMethodName, stackDepth),
                None,
                FailureMessages.propertyFailed(result.succeeded),
                scalaCheckArgs,
                None,
                scalaCheckLabels.toList
              )

            case Test.PropException(scalaCheckArgs, e, scalaCheckLabels) =>

              throw new GeneratorDrivenPropertyCheckFailedException(
                sde => FailureMessages.propertyException(UnquotedString(e.getClass.getSimpleName)) + "\n" +
                  "  " + FailureMessages.thrownExceptionsMessage(if (e.getMessage == null) "None" else UnquotedString(e.getMessage)) + "\n" +
                  (
                    e match {
                      case sd: StackDepth if sd.failedCodeFileNameAndLineNumberString.isDefined =>
                        "  " + FailureMessages.thrownExceptionsLocation(UnquotedString(sd.failedCodeFileNameAndLineNumberString.get)) + "\n"
                      case _ => ""
                    }
                    ) +
                  "  " + FailureMessages.occurredOnValues + "\n" +
                  prettyArgs(getArgsWithSpecifiedNames(argNames, scalaCheckArgs)) + "\n" +
                  "  )" +
                  getLabelDisplay(scalaCheckLabels),
                Some(e),
                getStackDepthFun(stackDepthFileName, stackDepthMethodName),
                None,
                FailureMessages.propertyException(UnquotedString(e.getClass.getName)),
                scalaCheckArgs,
                None,
                scalaCheckLabels.toList
              )
          }
        }
        else Succeeded
      }
    }
  }

/*
  implicit def assertingNatureOfExpectation: CheckerAsserting[Expectation] { type Result = Expectation } = {
    new CheckerAsserting[Expectation] {
      type Result = Expectation
    }
  }
*/
}

object CheckerAsserting extends MediumPriorityCheckerAsserting {

/*
  implicit def assertingNatureOfNothing: CheckerAsserting[Nothing] { type Result = Nothing } = {
    new CheckerAsserting[Nothing] {
      type Result = Nothing
      val Singleton = throw new NoSuchElementException
    }
  }
*/
  /*implicit def assertingNatureOfString: CheckerAsserting[String] { type Result = Unit } = {
    new CheckerAsserting[String] {
      type Result = Unit
      val Singleton = ()
    }
  }*/

  private[enablers] def getArgsWithSpecifiedNames(argNames: Option[List[String]], scalaCheckArgs: List[Arg[Any]]) = {
    if (argNames.isDefined) {
      // length of scalaCheckArgs should equal length of argNames
      val zipped = argNames.get zip scalaCheckArgs
      zipped map { case (argName, arg) => arg.copy(label = argName) }
    }
    else
      scalaCheckArgs
  }

  private[enablers] def getLabelDisplay(labels: Set[String]): String =
    if (labels.size > 0)
      "\n  " + (if (labels.size == 1) Resources.propCheckLabel else Resources.propCheckLabels) + "\n" + labels.map("    " + _).mkString("\n")
    else
      ""

  private[enablers] def argsAndLabels(result: Test.Result): (List[Any], List[String]) = {

    val (scalaCheckArgs, scalaCheckLabels) =
      result.status match {
        case Test.Proved(args) => (args.toList, List())
        case Test.Failed(args, labels) => (args.toList, labels.toList)
        case Test.PropException(args, _, labels) => (args.toList, labels.toList)
        case _ => (List(), List())
      }

    val args: List[Any] = for (scalaCheckArg <- scalaCheckArgs.toList) yield scalaCheckArg.arg

    // scalaCheckLabels is a Set[String], I think
    val labels: List[String] = for (scalaCheckLabel <- scalaCheckLabels.iterator.toList) yield scalaCheckLabel

    (args, labels)
  }

  // TODO: Internationalize these, and make them consistent with FailureMessages stuff (only strings get quotes around them, etc.)
  private[enablers] def prettyTestStats(result: Test.Result) = result.status match {

    case Test.Proved(args) =>
      "OK, proved property:                   \n" + prettyArgs(args)

    case Test.Passed =>
      "OK, passed " + result.succeeded + " tests."

    case Test.Failed(args, labels) =>
      "Falsified after " + result.succeeded + " passed tests:\n" + prettyLabels(labels) + prettyArgs(args)

    case Test.Exhausted =>
      "Gave up after only " + result.succeeded + " passed tests. " +
        result.discarded + " tests were discarded."

    case Test.PropException(args, e, labels) =>
      FailureMessages.propertyException(UnquotedString(e.getClass.getSimpleName)) + "\n" + prettyLabels(labels) + prettyArgs(args)
  }

  private[enablers] def prettyLabels(labels: Set[String]) = {
    if (labels.isEmpty) ""
    else if (labels.size == 1) "Label of failing property: " + labels.iterator.next + "\n"
    else "Labels of failing property: " + labels.mkString("\n") + "\n"
  }

  //
  // If scalacheck arg contains a type that
  // decorateToStringValue processes, then let
  // decorateToStringValue handle it.  Otherwise use its
  // prettyArg method to generate the display string.
  //
  // Passes 0 as verbosity value to prettyArg function.
  //
  private[enablers] def decorateArgToStringValue(arg: Arg[_]): String =
    arg.arg match {
      case null         => decorateToStringValue(arg.arg)
      case _: Unit      => decorateToStringValue(arg.arg)
      case _: String    => decorateToStringValue(arg.arg)
      case _: Char      => decorateToStringValue(arg.arg)
      case _: Array[_]  => decorateToStringValue(arg.arg)
      case _            => arg.prettyArg(new Pretty.Params(0))
    }

  private[enablers] def prettyArgs(args: List[Arg[_]]) = {
    val strs = for((a, i) <- args.zipWithIndex) yield (
      "    " +
        (if (a.label == "") "arg" + i else a.label) +
        " = " + decorateArgToStringValue(a) + (if (i < args.length - 1) "," else "") +
        (if (a.shrinks > 0) " // " + a.shrinks + (if (a.shrinks == 1) " shrink" else " shrinks") else "")
      )
    strs.mkString("\n")
  }
}

