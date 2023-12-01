package com.build
class Utilities {
  static def mvn(script,home,args) {
    script.sh "${home}/bin/mvn -o ${args}"
  }
}