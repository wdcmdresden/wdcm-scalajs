package de.knutwalker.wdcm

import akka.http.scaladsl.server.Directives

trait StaticRoutes extends Directives {

  def appSettings: AppSettings

  def statics =
    pathSingleSlash {
      get {
        getFromResource("web/index.html")
      }
    } ~
    path("wdcm-frontend-fastopt.js") {
      get {
        if (appSettings.fullOpt) {
          getFromResource("wdcm-frontend-opt.js")
        } else {
          getFromResource("wdcm-frontend-fastopt.js")
        }
      }
    } ~
    path("wdcm-frontend-jsdeps.js") {
      get {
        if (appSettings.fullOpt) {
          getFromResource("wdcm-frontend-jsdeps.min.js")
        } else {
          getFromResource("wdcm-frontend-jsdeps.js")
        }
      }
    } ~
    path("webjars" / Segments) { path ⇒
      get {
        getFromResource(s"META-INF/resources/webjars/${path.mkString("/")}")
      }
    } ~
    path("js" / raw"(.+)\.js".r) { file ⇒
      get {
        if (appSettings.fullOpt) {
          getFromResource(s"web/js/$file.min.js")
        } else {
          getFromResource(s"web/js/$file.js")
        }
      }
    } ~
    getFromResourceDirectory("web") ~
    getFromResourceDirectory("")
}
