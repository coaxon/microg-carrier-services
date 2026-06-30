/**
 * @name Sensitive data leak to log
 * @description Writing sensitive data to a log file can expose it to unauthorized users.
 * @kind path-problem
 * @problem.severity error
 * @id java/sensitive-log-leak
 */

import java
import semmle.code.java.dataflow.TaintTracking
import DataFlow::PathGraph

class SensitiveDataSource extends DataFlow::Node {
  SensitiveDataSource() {
    this.asExpr().(MethodAccess).getMethod().hasName("getIccAuthentication") or
    this.asExpr().(VariableAccess).getVariable().getName().regexpMatch("(?i).*(ck|ik|res|response|rootKey).*")
  }
}

class LogSink extends DataFlow::Node {
  LogSink() {
    exists(MethodAccess ma |
      ma.getMethod().getDeclaringType().hasQualifiedName("android.util", "Log") and
      this.asExpr() = ma.getAnArgument()
    )
  }
}

class SensitiveToLogConfig extends TaintTracking::Configuration {
  SensitiveToLogConfig() { this = "SensitiveToLogConfig" }
  
  override predicate isSource(DataFlow::Node source) {
    source instanceof SensitiveDataSource
  }
  
  override predicate isSink(DataFlow::Node sink) {
    sink instanceof LogSink
  }
}

from SensitiveToLogConfig config, DataFlow::PathNode source, DataFlow::PathNode sink
where config.hasFlowPath(source, sink)
select sink.getNode(), source, sink, "Sensitive data leaks to log from $@.", source.getNode(), "this source"
