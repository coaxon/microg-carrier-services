/**
 * @name Cross-module dependency check
 * @description Find dependencies from ts43 package to android.* packages.
 * @kind problem
 * @problem.severity warning
 * @id java/ts43-android-dependency
 */

import java

from RefType source, RefType target
where
  source.getPackage().getName().matches("%com.google.android.ims.ts43%") and
  source.dependsOn(target) and
  target.getPackage().getName().matches("android.%")
select source, "TS.43 module depends on Android system API: " + target.getQualifiedName()
