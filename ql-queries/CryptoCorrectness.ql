/**
 * @name HMAC parameter check and Constants
 * @description Checking if HMAC-SHA1 initialization parameters are correctly used and FIPS constants are correct.
 * @kind problem
 * @problem.severity warning
 * @id java/crypto-correctness
 */

import java

from Expr e, string msg
where
  (
    exists(MethodAccess ma |
      ma.getMethod().getDeclaringType().hasQualifiedName("javax.crypto", "Mac") and
      ma.getMethod().hasName("init") and
      e = ma
    ) and msg = "Mac.init() call found. Verify key and data order."
  ) or
  (
    exists(StringLiteral sl |
      sl.getEnclosingCallable().getDeclaringType().hasName("EapAkaCrypto") and
      e = sl
    ) and msg = "String constant in EapAkaCrypto: " + e.(StringLiteral).getValue()
  ) or
  (
    exists(Field f |
      f.getDeclaringType().hasName("EapAkaCrypto") and
      f.isStatic() and f.isFinal() and
      e = f.getAnAccess()
    ) and msg = "Constant field access in EapAkaCrypto: " + e.toString()
  )
select e, msg
