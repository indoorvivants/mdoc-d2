/*
 * Copyright 2023 Anton Sviridov
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

package com.indoorvivants.mdoc_d2

import java.nio.file.Path
import java.util.Base64

import scala.meta.Input

import com.indoorvivants.yank._
import mdoc.Reporter

class D2Modifier extends mdoc.StringModifier {
  override val name = "d2"

  lazy val cli = tools.D2.bootstrap(tools.D2.Config(version = "0.4.1"))
  lazy val encoder = Base64.getEncoder()

  def create(path: Path, text: String, reporter: Reporter): String = {
    val extraArgs =
      text.linesIterator.filter(_.trim.startsWith("#!")).flatMap { l =>
        val splits = l.split("=").toList

        splits match {
          case key :: value :: Nil =>
            List(s"--${key.trim().drop(2)}", value.trim())
          case flag :: Nil =>
            List(s"--${flag.trim()}")
          case _ =>
            throw new Exception(
              s"D2(mdoc): Configuration line `$l` couldn't be parsed as either flag or a `key=value`"
            )
        }
      }
    val args = List(path.toString()) ++ extraArgs ++ List("-", "-")
    val build = new java.lang.ProcessBuilder(args: _*).start()

    val is = build.getInputStream()
    val os = build.getOutputStream()

    os.write(text.getBytes())
    os.close()

    val exitCode = build.waitFor()

    scala.io.Source.fromInputStream(build.getErrorStream()).getLines().foreach {
      l =>
        if (exitCode == 0) reporter.info("D2: " + l)
        else reporter.error("D2: " + l)
    }

    assert(exitCode == 0, s"D2 process exited with ${exitCode}")

    val lines = scala.io.Source
      .fromInputStream(is)
      .getLines()
      .mkString(System.lineSeparator())

    lines
  }

  override def process(
      info: String,
      code: Input,
      reporter: Reporter
  ): String = {
    val source = code.text
    val raw = create(cli, source, reporter)
    val encoded = new String(encoder.encode(raw.getBytes()))

    s"""
    <img alt="" src="data:image/svg+xml;base64,$encoded">
    """.trim

  }

}
