package de.knutwalker.wdcm

import com.typesafe.config.Config

case class AppSettings(config: Config) {
  import config._

  val fullOpt = getBoolean("app.use-full-opt")

  val presenterPort = getInt("app.presenter.port")
  val presenterInterface = getString("app.presenter.interface")

  val handoutPort = getInt("app.handout.port")
  val handoutInterface = getString("app.handout.interface")
}
